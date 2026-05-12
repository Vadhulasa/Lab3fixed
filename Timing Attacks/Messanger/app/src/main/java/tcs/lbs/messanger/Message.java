package tcs.lbs.messanger;

import java.time.LocalTime;

public class Message
{
    final static int MESSAGE_STATUS_SENT = 1;
    final static int MESSAGE_STATUS_SERVER_RECEIVED = 2;
    final static int MESSAGE_STATUS_RECEIVER_RECEIVED = 3;

    String text;
    int message_ID;

    int type;

    int status;

    LocalTime time;


    public Message(String _text, int _message_ID, int _type, int _status, LocalTime _time)
    {
        this.text = _text;
        this.message_ID = _message_ID;
        this.type = _type;
        this.status = _status;
        this.time = _time;
    }

    public String getText()
    {
        return text;
    }

    public int getID()
    {
        return message_ID;
    }

    public int getType() {return type;}

    public void setStatus(int _status)
    {
        this.status = _status;
    }

    public int getStatus()
    {
        return status;
    }

    public LocalTime getTime()
    {
        return time;
    }
}
