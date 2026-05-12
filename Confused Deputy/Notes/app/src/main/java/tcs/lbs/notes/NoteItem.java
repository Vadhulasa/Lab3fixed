package tcs.lbs.notes;

import android.os.Parcel;
import android.os.Parcelable;

// A parcelable class for every note item, which contains the note's ID and TEXT
public class NoteItem implements Parcelable
{
    private String id;
    private String text;

    public NoteItem(String _id, String _text)
    {
        this.id = _id;
        this.text = _text;
    }

    public String getID()
    {
        return id;
    }

    public String getText()
    {
        return text;
    }


    public NoteItem(Parcel in)
    {
        super();
        readFromParcel(in);
    }

    public static final Parcelable.Creator<NoteItem> CREATOR = new Parcelable.Creator<NoteItem>()
    {
        public NoteItem createFromParcel(Parcel in)
        {
            return new NoteItem(in);
        }

        public NoteItem[] newArray(int size)
        {
            return new NoteItem[size];
        }

    };

    public void readFromParcel(Parcel in)
    {
        id = in.readString();
        text = in.readString();
    }

    public int describeContents()
    {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(id);
        dest.writeString(text);
    }
}
