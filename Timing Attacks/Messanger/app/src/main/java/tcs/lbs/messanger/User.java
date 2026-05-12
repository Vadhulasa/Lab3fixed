package tcs.lbs.messanger;

import android.graphics.drawable.Drawable;

import java.io.Serializable;
import java.time.LocalTime;

public class User implements Serializable
{
    String name, id;
    int avatar;

    public User(String _id, String _name, int _avatar)
    {
        this.id = _id;
        this.name = _name;
        this.avatar = _avatar;
    }

    public String getUserID()
    {
        return id;
    }
    public String getUserName()
    {
        return name;
    }
    public int getUserAvatar()
    {
        return avatar;
    }

}
