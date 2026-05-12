package tcs.lbs.notes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

// just some helper functions that directly interact with the Database
public class DataBaseHelper extends SQLiteOpenHelper
{
    private static final int DATABASE_VERSION = 1;
    private static final String NOTES_TABLE = "notes_table";
    private static final String DATABASE_NAME = "notes.db";

    private static final String KEY_ID = "_id";
    private static final String KEY_NOTE = "note_text";

    private static final String NOTES_TABLE_CREATE =
            "CREATE TABLE " + NOTES_TABLE + " (" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " + KEY_NOTE + " TEXT)";


    private SQLiteDatabase mWritableDB;
    private SQLiteDatabase mReadableDB;

    public DataBaseHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL(NOTES_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        Log.w("onUpgrade","Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + NOTES_TABLE);
        onCreate(db);
    }


    protected String getText(String id)
    {
        String text = "";

        String query = "SELECT * FROM " + NOTES_TABLE + " WHERE " + KEY_ID + " = " + id;

        Cursor cursor = null;
        try
        {
            if (mReadableDB == null)
            {
                mReadableDB = getReadableDatabase();
            }

            cursor = mReadableDB.rawQuery(query, null);
        }
        catch (Exception e)
        {
            Log.d("getText", "getText EXCEPTION! " + e);
        }
        finally
        {
            for(cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext())
            {
                text = cursor.getString(cursor.getColumnIndex(KEY_NOTE));
            }
            cursor.close();
        }

        return text;
    }


    protected void remove(String noteID)
    {
        try
        {
            if (mWritableDB == null)
            {
                mWritableDB = getWritableDatabase();
            }

            mWritableDB.delete(NOTES_TABLE, KEY_ID + "=" + noteID, null);
        }
        catch (Exception e)
        {
            Log.d("Delete", "DELETE EXCEPTION! " + e.getMessage());
        }
    }


    protected void update(String noteID, String noteText)
    {
        ContentValues values = new ContentValues();
        values.put(KEY_NOTE, noteText);

        try
        {
            if (mWritableDB == null)
            {
                mWritableDB = getWritableDatabase();
            }
            mWritableDB.update(NOTES_TABLE, values, KEY_ID + "=" + noteID, null);
        }
        catch (Exception e)
        {
            Log.d("Insert", "UPDATE EXCEPTION! " + e.getMessage());
        }
    }

    protected String insert(String noteText)
    {
        long rowID = 0;
        ContentValues values = new ContentValues();
        values.put(KEY_NOTE, noteText);

        try
        {
            if (mWritableDB == null)
            {
                mWritableDB = getWritableDatabase();
            }
            rowID = mWritableDB.insert(NOTES_TABLE, null, values);
        }
        catch (Exception e)
        {
            Log.d("Insert", "INSERT EXCEPTION! " + e.getMessage());
        }

        return String.valueOf(rowID);
    }


    protected ArrayList<NoteItem> getAllNotes()
    {
        ArrayList<NoteItem> notes = new ArrayList<>();

        String query = "SELECT * FROM " + NOTES_TABLE;
        Cursor cursor = null;
        try
        {
            if (mReadableDB == null)
            {
                mReadableDB = getReadableDatabase();
            }

            cursor = mReadableDB.rawQuery(query, null);
        }
        catch (Exception e)
        {
            Log.d("getAllNotes", "getAllNotes EXCEPTION! " + e);
        }
        finally
        {
            for(cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext())
            {
                String ID = cursor.getString(cursor.getColumnIndex(KEY_ID));
                String Text = cursor.getString(cursor.getColumnIndex(KEY_NOTE));

                notes.add(new NoteItem(ID, Text));
            }
            cursor.close();
        }

        return notes;
    }

}
