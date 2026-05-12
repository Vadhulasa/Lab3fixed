package tcs.lbs.notes;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import java.util.ArrayList;


// This activity does not have a GUI and only works as a helper to update the database
public class DataBaseActivity extends AppCompatActivity
{
    public static final String ACTION_NAME = "ACTION_NAME";

    public static final String ACTION_GetItemText = "GET_ITEM_TEXT";
    public static final String ACTION_DeleteItem = "DELETE_ITEM";
    public static final String ACTION_SaveItem = "SAVE_ITEM";
    public static final String ACTION_GetAllItems = "GET_ALL_ITEMS";


    public static final String ITEM_TEXT = "ITEM_TEXT";
    public static final String ALL_ITEMS = "ALL_ITEMS";
    public static final String NEW_ID = "NEW_ID";
    public static final String NOTE_ID = "NOTE_ID";
    public static final String NOTE_TEXT = "NOTE_TEXT";

    private DataBaseHelper dataBaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        dataBaseHelper = new DataBaseHelper(getApplicationContext());

        // Switches functionality based on the Action defined in the Intent that started this activity
        switch (getIntent().getStringExtra(ACTION_NAME))
        {
            case ACTION_SaveItem:
                Intent i = getIntent();
                String id = i.getStringExtra(NOTE_ID);
                String text = i.getStringExtra(NOTE_TEXT);

                if (id.equals(NEW_ID))
                {
                    dataBaseHelper.insert(text);
                }
                else
                {
                    dataBaseHelper.update(id, text);
                }

                setResult(RESULT_OK);
                finish();
                break;

            case ACTION_DeleteItem:
                Intent deleteIntent = getIntent();
                String deleteID = deleteIntent.getStringExtra(NOTE_ID);

                if (!deleteID.equals(NEW_ID))
                {
                    dataBaseHelper.remove(deleteID);
                }

                setResult(RESULT_OK);
                finish();
                break;

            case ACTION_GetItemText:
                String noteText = dataBaseHelper.getText(getIntent().getStringExtra(NOTE_ID));
                Intent noteTextData = new Intent();
                noteTextData.putExtra(ITEM_TEXT, noteText);
                setResult(RESULT_OK, noteTextData);
                finish();
                break;

            case ACTION_GetAllItems:
                ArrayList<NoteItem> newItems = dataBaseHelper.getAllNotes();
                Intent noteItemsData = new Intent();
                noteItemsData.putParcelableArrayListExtra(ALL_ITEMS, newItems);
                setResult(RESULT_OK, noteItemsData);
                finish();
                break;

        }
    }
}
