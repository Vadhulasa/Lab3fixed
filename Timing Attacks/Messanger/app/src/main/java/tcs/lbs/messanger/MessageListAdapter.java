package tcs.lbs.messanger;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class MessageListAdapter extends RecyclerView.Adapter
{
    final static int VIEW_TYPE_MESSAGE_SENT = 0;
    final static int VIEW_TYPE_MESSAGE_RECEIVED = 1;

    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm");


    private Context mContext;
    private List<Message> mMessageList;

    public MessageListAdapter(Context context, List<Message> messageList)
    {
        mContext = context;
        mMessageList = messageList;
    }

    // Determines the appropriate ViewType according to the sender of the message.
    @Override
    public int getItemViewType(int position)
    {
        Message message = (Message) mMessageList.get(position);

        return message.getType();
    }

    // Inflates the appropriate layout according to the ViewType.
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view;

        if (viewType == VIEW_TYPE_MESSAGE_SENT)
        {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_chat_me, parent, false);
            return new SentMessageHolder(view);
        }
        else if (viewType == VIEW_TYPE_MESSAGE_RECEIVED)
        {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_chat_other, parent, false);
            return new ReceivedMessageHolder(view);
        }

        return null;
    }

    // Passes the message object to a ViewHolder so that the contents can be bound to UI.
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Message message = (Message) mMessageList.get(position);

        switch (holder.getItemViewType())
        {
            case VIEW_TYPE_MESSAGE_SENT:
                ((SentMessageHolder) holder).bind(message);
                break;
            case VIEW_TYPE_MESSAGE_RECEIVED:
                ((ReceivedMessageHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount()
    {
        return mMessageList.size();
    }

    private class SentMessageHolder extends RecyclerView.ViewHolder
    {
        TextView messageText, timeText;
        ImageView deliveyImageView;

        SentMessageHolder(View itemView)
        {
            super(itemView);

            messageText = (TextView) itemView.findViewById(R.id.text_message_me);
            timeText = (TextView) itemView.findViewById(R.id.text_timestamp_me);
            deliveyImageView = (ImageView) itemView.findViewById(R.id.delivery_status_imageView);
        }

        void bind(Message message)
        {
            messageText.setText(message.getText());
            timeText.setText(message.getTime().format(dtf));

            switch(message.getStatus())
            {
                case Message.MESSAGE_STATUS_SENT:
                    deliveyImageView.setImageResource(R.mipmap.ic_delivery_status_sending);
                    break;
                case Message.MESSAGE_STATUS_SERVER_RECEIVED:
                    deliveyImageView.setImageResource(R.mipmap.ic_delivery_status_sent);
                    break;
                case Message.MESSAGE_STATUS_RECEIVER_RECEIVED:
                    deliveyImageView.setImageResource(R.mipmap.ic_delivery_status_delivered);
                    break;
                default:
                    deliveyImageView.setImageResource(R.mipmap.ic_error);
            }


        }
    }

    private class ReceivedMessageHolder extends RecyclerView.ViewHolder
    {
        TextView messageText, timeText;

        ReceivedMessageHolder(View itemView)
        {
            super(itemView);

            messageText = (TextView) itemView.findViewById(R.id.text_message_other);
            timeText = (TextView) itemView.findViewById(R.id.text_timestamp_other);
        }

        void bind(Message message)
        {
            messageText.setText(message.getText());
            timeText.setText(message.getTime().format(dtf));
        }
    }
}