package tcs.lbs.messanger;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements UsersListAdapter.ItemClickListener
{
    private RecyclerView usersRecycler;
    private UsersListAdapter usersAdapter;
    List<User> userList = new ArrayList<User>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.users_toolbar);
        setSupportActionBar(myToolbar);

        User u1 = new User("001", "Eadric", R.drawable.ava_1);
        User u2 = new User("002", "Nalena", R.drawable.ava_2);
        User u3 = new User("003", "Kaelen", R.drawable.ava_3);
        userList.add(u1);
        userList.add(u2);
        userList.add(u3);

        usersRecycler = (RecyclerView) findViewById(R.id.recycler_users);
        usersAdapter = new UsersListAdapter(this, userList);
        usersAdapter.setClickListener(this);
        usersRecycler.setLayoutManager(new LinearLayoutManager(this));
        usersRecycler.setAdapter(usersAdapter);
    }

    // When clicking on a User, open the conversation activity.
    @Override
    public void onUserClick(View view, int position)
    {
        Intent i = new Intent(MainActivity.this, ConversationActivity.class);
        i.putExtra("USER_ID", usersAdapter.getItem(position).getUserID());
        i.putExtra("USER_NAME", usersAdapter.getItem(position).getUserName());
        i.putExtra("USER_AVATAR", usersAdapter.getItem(position).getUserAvatar());
        startActivity(i);
    }

}