package tcs.lbs.notes;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.File;

// a ContentProvider to facilitate sharing the txt file of a note
public class FileProvider extends ContentProvider
{
    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode)
    {
        String path = uri.getPath();

        if (path == null)
        {
            return null;
        }

        try
        {
            File f = new File(getContext().getCacheDir().getCanonicalPath() + path);

            return ParcelFileDescriptor.open(f, ParcelFileDescriptor.MODE_READ_ONLY);
        }
        catch (Exception e)
        {
            return null;
        }
    }





    // We are not using content providers for database interactions
    // So these functions won't be used.
    @Override
    public boolean onCreate()
    {
        // Implement this to initialize content provider on startup.
        return false;
    }

    @Override
    public String getType(Uri uri)
    {
        return(null);
    }

    @Override
    public Uri insert(Uri uri, ContentValues values)
    {
        // Implement this to handle requests to insert a new row.
        throw new UnsupportedOperationException("Operation is not supported");
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder)
    {
        // Implement this to handle query requests from clients.
        throw new UnsupportedOperationException("Operation is not supported");
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs)
    {
        // Implement this to handle requests to update one or more rows.
        throw new UnsupportedOperationException("Operation is not supported");
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs)
    {
        // Implement this to handle requests to delete one or more rows.
        throw new UnsupportedOperationException("Operation is not supported");
    }
}
