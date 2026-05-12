package tcs.lbs.messanger;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NetworkingHelper
{
    private final OkHttpClient client = new OkHttpClient();
    private final String serverAddress = "http://10.0.2.2:12345";

    public String sendData(JSONObject jsonObject)
    {
        try
        {
            MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
            RequestBody postBody = RequestBody.create(jsonObject.toString(), mediaType);

            Request request = new Request.Builder()
                    .addHeader("Connection", "close")
                    .url(serverAddress)
                    .post(postBody)
                    .build();

            Response response = client.newCall(request).execute();
            return response.body().string();
        }
        catch (IOException e)
        {
            return "Error: " + e.getMessage();
        }
    }
}


