package tcs.lbs.messanger;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class ConversationActivity extends AppCompatActivity
{
    private RecyclerView mMessageRecycler;
    private MessageListAdapter mMessageAdapter;
    List<Message> messageList = new ArrayList<Message>();
    EditText messageBodyEditText;

    String USER_ID;

    int counter = 1;

    private TCPClient mClient;
    private ConnectionTask conctTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.conversation_toolbar);
        setSupportActionBar(myToolbar);

        String username = getIntent().getStringExtra("USER_NAME");
        TextView user_tv = findViewById(R.id.conv_name_textView);
        user_tv.setText(username);

        int user_ava_id = getIntent().getIntExtra("USER_AVATAR", 0);
        ImageView user_iv = findViewById(R.id.conv_avatar_ImageView);
        user_iv.setImageResource(user_ava_id);

        USER_ID = getIntent().getStringExtra("USER_ID");

        conctTask = new ConnectionTask();
        conctTask.execute();

        messageBodyEditText = (EditText) findViewById(R.id.edit_gchat_message);

        mMessageRecycler = (RecyclerView) findViewById(R.id.recycler_chat);
        mMessageAdapter = new MessageListAdapter(this, messageList);
        mMessageRecycler.setLayoutManager(new LinearLayoutManager(this));
        mMessageRecycler.setAdapter(mMessageAdapter);
    }

    public void sendMessage (View v)
    {
        JSONObject messageJSONObject = new JSONObject();
        try
        {
            messageJSONObject.put("Receiver_ID", USER_ID);
            messageJSONObject.put("MessageBody", messageBodyEditText.getText().toString());
            messageJSONObject.put("MessageType", "new_msg");
            messageJSONObject.put("MessageID", counter);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        if (mClient != null)
        {
            new Thread(() -> mClient.sendMessage(messageJSONObject.toString())).start();
        }

        Message m = new Message(
                messageBodyEditText.getText().toString(),
                counter,
                MessageListAdapter.VIEW_TYPE_MESSAGE_SENT,
                Message.MESSAGE_STATUS_SENT,
                LocalTime.now());

        messageList.add(m);
        mMessageAdapter.notifyDataSetChanged();
        messageBodyEditText.setText("");
        mMessageRecycler.smoothScrollToPosition(mMessageAdapter.getItemCount());
        counter++;

        Log.d("AppLog", "User:"+ USER_ID +" ; Type:new_msg ; Message:" + m.getText() + " ; MessageID:"+ m.getID() + " ; Time:" + m.getTime());
    }

    private class ConnectionTask extends AsyncTask<Void, Void, TCPClient>
    {
        @Override
        protected TCPClient doInBackground(Void... voids)
        {
            //create a TCPClient object and
            mClient = new TCPClient(new TCPClient.OnMessageReceived()
            {
                @Override
                //here the messageReceived method is implemented
                public void messageReceived(String message)
                {
                    int id = 0;
                    LocalTime rcvTime = LocalTime.now();
                    // receiving a response from the server.
                    try
                    {
                        JSONObject messageJSONObject = new JSONObject(message);

                        switch(messageJSONObject.getString("MessageType"))
                        {
                            case "new_msg" :
                                Message m = new Message(
                                        messageJSONObject.getString("MessageBody"),
                                        counter,
                                        MessageListAdapter.VIEW_TYPE_MESSAGE_RECEIVED,
                                        -1, // Received
                                        LocalTime.now());
                                messageList.add(m);
                                runOnUiThread(new Runnable()
                                {
                                    @Override
                                    public void run() {
                                        mMessageAdapter.notifyDataSetChanged();
                                        mMessageRecycler.smoothScrollToPosition(mMessageAdapter.getItemCount());
                                    }
                                });
                                counter++;
                                break;
                            case "server_ack" :
                                id = messageJSONObject.getInt("MessageID");
                                for (Message msg : messageList)
                                {
                                    if (msg.getID() == id)
                                    {
                                        msg.setStatus(Message.MESSAGE_STATUS_SERVER_RECEIVED);
                                        Log.d("AppLog", "User:"+ USER_ID +" ; Type:server_ack ; Message:" + msg.getText() + " ; MessageID:"+ msg.getID() + " ; Time:" + rcvTime);
                                        runOnUiThread(new Runnable()
                                        {
                                            @Override
                                            public void run() {
                                                mMessageAdapter.notifyItemChanged(messageList.indexOf(msg));
                                            }
                                        });
                                        break;
                                    }
                                }
                                break;
                            case "receiver_ack" :
                                id = messageJSONObject.getInt("MessageID");
                                for (Message msg : messageList)
                                {
                                    if (msg.getID() == id)
                                    {
                                        msg.setStatus(Message.MESSAGE_STATUS_RECEIVER_RECEIVED);

                                        Log.d("AppLog", "User:"+ USER_ID +" ; Type:receiver_ack ; Message:" + msg.getText() + " ; MessageID:"+ msg.getID() + " ; Time:" + rcvTime);
                                        runOnUiThread(new Runnable()
                                        {
                                            @Override
                                            public void run() {
                                                mMessageAdapter.notifyItemChanged(messageList.indexOf(msg));
                                            }
                                        });
                                        break;
                                    }
                                }
                                break;
                            default:
                                id = messageJSONObject.getInt("MessageID");
                                for (Message msg : messageList)
                                {
                                    if (msg.getID() == id)
                                    {
                                        msg.setStatus(0); // Error
                                        runOnUiThread(new Runnable()
                                        {
                                            @Override
                                            public void run() {
                                                mMessageAdapter.notifyItemChanged(messageList.indexOf(msg));
                                            }
                                        });
                                        break;
                                    }
                                }
                                break;
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            });
            mClient.run();

            return null;
        }
    }

    @Override
    protected void onDestroy()
    {
        try
        {
            conctTask.cancel(true);
            mClient.stopClient();
            mClient = null;
            conctTask = null;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        super.onDestroy();
    }
}
