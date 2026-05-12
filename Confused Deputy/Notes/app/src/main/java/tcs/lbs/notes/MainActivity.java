package tcs.lbs.notes;


import android.content.Intent;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements NotesAdapter.ItemClickListener
{
    private ArrayList<NoteItem> noteItems = new ArrayList<>();
    private NotesAdapter notesAdapter;

    public static final int REQUEST_CODE_DeleteItem = 1;
    public static final int REQUEST_CODE_AddOrUpdate = 2;
    public static final int REQUEST_CODE_GetAllItems = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // creating a RecyclerView to show all the notes
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        notesAdapter = new NotesAdapter(this, noteItems);
        notesAdapter.setClickListener(this);

        recyclerView.setAdapter(notesAdapter);

        // updating the list of notes
        UpdateList();

        // adding onClick functionality to the floating button which adds new note
        FloatingActionButton fab = findViewById(R.id.fab_new);
        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent noteIntent = new Intent(MainActivity.this, NoteActivity.class);
                noteIntent.putExtra(NoteActivity.ACTION_NAME, NoteActivity.ACTION_AddNote);
                noteIntent.putExtra(DataBaseActivity.NOTE_ID, DataBaseActivity.NEW_ID);
                startActivityForResult(noteIntent, REQUEST_CODE_AddOrUpdate);
            }
        });


        ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT)
        {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target)
            {
                return false;
            }

            // swiping an item deletes it.
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction)
            {
                // use a DataBaseActivity intent to remove the item and update the database
                Intent noteIntent = new Intent(MainActivity.this, DataBaseActivity.class);
                noteIntent.putExtra(DataBaseActivity.ACTION_NAME, DataBaseActivity.ACTION_DeleteItem);
                noteIntent.putExtra(DataBaseActivity.NOTE_ID, noteItems.get(viewHolder.getAdapterPosition()).getID());
                startActivityForResult(noteIntent, REQUEST_CODE_DeleteItem);
            }
        });
        helper.attachToRecyclerView(recyclerView);
    }


    // requests DataBaseActivity for a new list of all the notes stored in DataBase
    protected void UpdateList()
    {
        Intent noteIntent = new Intent(MainActivity.this, DataBaseActivity.class);
        noteIntent.putExtra(DataBaseActivity.ACTION_NAME, DataBaseActivity.ACTION_GetAllItems);
        startActivityForResult(noteIntent, REQUEST_CODE_GetAllItems);
    }

    // When clicking on an Item, open the note activity to show the contents of that item.
    @Override
    public void onItemClick(View view, int position)
    {
        Intent noteIntent = new Intent(MainActivity.this, NoteActivity.class);
        noteIntent.putExtra(NoteActivity.ACTION_NAME, NoteActivity.ACTION_UpdateNote);
        noteIntent.putExtra(DataBaseActivity.NOTE_ID, notesAdapter.getItem(position).getID());
        startActivityForResult(noteIntent, REQUEST_CODE_AddOrUpdate);
    }


    // When using startActivityForResult to start an intent
    // this method is invoked when that intent returns a result.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK)
        {
            switch (requestCode)
            {
                case REQUEST_CODE_DeleteItem:
                    UpdateList();
                    Toast.makeText(MainActivity.this, "Note Deleted", Toast.LENGTH_SHORT).show();
                    break;

                case REQUEST_CODE_AddOrUpdate:
                    UpdateList();
                    break;

                case REQUEST_CODE_GetAllItems:
                    ArrayList<NoteItem> receivedItems = data.getParcelableArrayListExtra(DataBaseActivity.ALL_ITEMS);
                    noteItems.clear();
                    noteItems.addAll(receivedItems);
                    notesAdapter.notifyDataSetChanged();
                    break;
            }

        }
    }
}
