package tcs.lbs.notes;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

// An Activity to show a single note or create a new one
public class NoteActivity extends AppCompatActivity
{
    static final int PERMISSIONS_REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 1;
    boolean HasWritePermission = false;
    EditText editText;

    static final String PROVIDER_NAME = "tcs.lbs.notes";
    static final String URL = "content://" + PROVIDER_NAME + "/SharedFile.txt";
    static final Uri CONTENT_URI = Uri.parse(URL);

    public static final String ACTION_NAME = "ACTION_NAME";
    public static final String ACTION_AddNote = "ADD_NOTE";
    public static final String ACTION_UpdateNote = "UPDATE_NOTE";

    public static final int REQUEST_SaveData = 1;
    public static final int REQUEST_GetItemText = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        editText = findViewById(R.id.editText_NoteTaking);

        // check to see if this app has Read and Write access to External Storage
        checkWriteExternalPermission();

        // If this activity was lunched to show a note, use DataBaseActivity to get the text of it.
        if (getIntent().getStringExtra(ACTION_NAME).equals(ACTION_UpdateNote))
        {
            Intent noteIntent = new Intent(NoteActivity.this, DataBaseActivity.class);
            noteIntent.putExtra(DataBaseActivity.NOTE_ID, getIntent().getStringExtra(DataBaseActivity.NOTE_ID));
            noteIntent.putExtra(DataBaseActivity.ACTION_NAME, DataBaseActivity.ACTION_GetItemText);
            startActivityForResult(noteIntent, REQUEST_GetItemText);
        }


        // Functionality to save the note when floating save button is clicked
        FloatingActionButton fab = findViewById(R.id.fab_save);
        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent noteIntent = new Intent(NoteActivity.this, DataBaseActivity.class);
                noteIntent.putExtra(DataBaseActivity.ACTION_NAME, DataBaseActivity.ACTION_SaveItem);
                noteIntent.putExtra(DataBaseActivity.NOTE_ID, getIntent().getStringExtra(DataBaseActivity.NOTE_ID));
                noteIntent.putExtra(DataBaseActivity.NOTE_TEXT, editText.getText().toString());
                startActivityForResult(noteIntent, REQUEST_SaveData);
            }
        });
    }

    // Returned result from startActivityForResult
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK)
        {
            switch (requestCode)
            {
                case REQUEST_SaveData:
                    Toast.makeText(NoteActivity.this, "Note Saved", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                    break;

                case REQUEST_GetItemText:
                    String text = data.getStringExtra(DataBaseActivity.ITEM_TEXT);
                    editText.setText(text);
                    break;
            }

        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    // Functionality for when Share, Import, or Export buttons on the MenuBar are clicked
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        // When share button is clicked
        if (id == R.id.action_share)
        {
            try
            {
                EditText editText = findViewById(R.id.editText_NoteTaking);
                String text = editText.getText().toString();

                // Creates a file by the name of "SharedFile.txt" in the Cache Directory of the App
                File cacheFile = new File(getApplicationContext().getCacheDir() + "/SharedFile.txt");

                if (!cacheFile.exists())
                {
                    cacheFile.createNewFile();
                }

                FileWriter writer = new FileWriter(cacheFile);
                writer.write(text);
                writer.flush();
                writer.close();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            // Creates an ACTION_SEND intent to share the created file
            // And passes the address of that file through a URI
            // Other apps can use this URI and the contentProvider to access to file.
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_STREAM, CONTENT_URI);
            shareIntent.setType("text/plain");
            startActivity(Intent.createChooser(shareIntent, getResources().getText(R.string.send_to)));

            return true;
        }


        if (id == R.id.action_import)
        {
            importText();
            return true;
        }

        if (id == R.id.action_export)
        {
            exportText();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    void importText()
    {
        // Just some dummy procedure to justify having an import functionality and access to SDCard
        // It only reads the contents of "DemoTextFile.txt" file in the SDCard.

        if (HasWritePermission)
        {
            try
            {
                File sdcard = Environment.getExternalStorageDirectory();
                File file = new File(sdcard,"DemoTextFile.txt");
                StringBuilder text = new StringBuilder();

                BufferedReader br = new BufferedReader(new FileReader(file));
                String line;

                while ((line = br.readLine()) != null)
                {
                    text.append(line);
                    text.append('\n');
                }
                br.close();

                EditText editText = (EditText) findViewById(R.id.editText_NoteTaking);
                editText.setText(text);

                Toast.makeText(this, "File Imported !", Toast.LENGTH_SHORT).show();

            }
            catch (Exception e)
            {
                Log.d("ImportException", e.toString());
            }
        }
    }

    void exportText()
    {
        // Just some dummy procedure to justify having an export functionality and access to SDCard
        // It only writes to "DemoTextFile.txt" file in the SDCard.

        if (HasWritePermission)
        {
            try
            {
                EditText editText = (EditText) findViewById(R.id.editText_NoteTaking);
                String text = editText.getText().toString();

                File sdcard = Environment.getExternalStorageDirectory();
                File file = new File(sdcard,"DemoTextFile.txt");

                FileWriter writer = new FileWriter(file);
                writer.write(text);
                writer.flush();
                writer.close();

                Toast.makeText(this, "File Exported !", Toast.LENGTH_SHORT).show();
            }
            catch (Exception e)
            {
                Log.d("ExportException", e.toString());
            }
        }
    }


    void checkWriteExternalPermission()
    {
        // Checking if the app has External Memory READ and WRITE permission.
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            HasWritePermission = false;
            ActivityCompat.requestPermissions(NoteActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE_WRITE_EXTERNAL_STORAGE);

            // PERMISSIONS_REQUEST_CODE_WRITE_EXTERNAL_STORAGE is an
            // app-defined int constant. The callback method gets the
            // result of the request.
        }
        else
        {
            // Permission has already been granted
            HasWritePermission = true;
        }
    }


    // This function handles the permissions request response
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        switch (requestCode)
        {
            case PERMISSIONS_REQUEST_CODE_WRITE_EXTERNAL_STORAGE:
            {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    // permission was granted, yay!
                    HasWritePermission = true;
                }
                else
                {
                    // permission denied, boo!
                    HasWritePermission = false;
                    Toast.makeText(NoteActivity.this, "Permission denied to write to your External storage", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            // other 'case' lines to check for other permissions this app might request
        }
    }
}
