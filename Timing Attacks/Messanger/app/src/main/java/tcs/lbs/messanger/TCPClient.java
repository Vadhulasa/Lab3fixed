package tcs.lbs.messanger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import android.util.Log;

public class TCPClient
{
    private final static String SERVERADDRESS = "10.0.2.2";
    private final static int SERVERPORT = 12345;
    private String serverMessage;
    private Socket socket;
    private OnMessageReceived mMessageListener = null;
    private boolean mRun = false;
    private PrintWriter out = null;
    private BufferedReader in = null;

    List<Message> messageListToSend = new ArrayList<Message>();

    /**
     *  Constructor of the class. OnMessagedReceived listens for the messages received from server
     */
    public TCPClient(final OnMessageReceived listener)
    {
        mMessageListener = listener;
    }

    /**
     * Sends the message entered by client to the server
     * @param message text entered by client
     */
    public void sendMessage(String message)
    {
        if (this.out != null && !this.out.checkError())
        {
            synchronized(this.out)
            {
                this.out.println(message);
                this.out.flush();
            }
        }
    }

    public void stopClient()
    {
        try
        {
            socket.close();
            mRun = false;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void run()
    {
        mRun = true;
        try
        {
            //here you must put your computer's IP address.
            InetAddress serverAddr = InetAddress.getByName(SERVERADDRESS);

            //create a socket to make the connection with the server
            socket = new Socket(serverAddr, SERVERPORT);

            try
            {
                //send the message to the server
                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                //receive the message which the server sends back
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                //in this while the client listens for the messages sent by the server
                while (mRun)
                {
                    serverMessage = in.readLine();
                    if (serverMessage != null && mMessageListener != null)
                    {
                        //call the method messageReceived from ConversationActivity class
                        mMessageListener.messageReceived(serverMessage);
                    }
                    serverMessage = null;
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            finally
            {
                //the socket must be closed. It is not possible to reconnect to this socket
                // after it is closed, which means a new socket instance has to be created.
                socket.close();
            }

        } catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    //Declare the interface. The method messageReceived(String message) will must be implemented in the MyActivity
    //class at on asynckTask doInBackground
    public interface OnMessageReceived
    {
        public void messageReceived(String message);
    }
}
