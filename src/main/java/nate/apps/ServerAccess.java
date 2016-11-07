package nate.apps;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import nate.apps.fragmap.R;

/**
 * Created by nathan on 27/10/2016.
 * an intent service to handle interacting with server and send response back to activity
 */
public class ServerAccess extends IntentService {
    public static final String RECIEVER = "reciever";
    public static final String RESULT = "result";

    public ServerAccess() {
        super("server_access");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.v("outcome","started");
        String receiver = intent.getStringExtra(RECIEVER);
        attemptconnection(receiver);
    }

    private void attemptconnection(String receiver) {
        String result = null;
        try {
            //get input stream from resource file, would expect this to be input stream from httprequest normally
            InputStream is = getResources().openRawResource(R.raw.json);
            String line;
            //buffered reader to hanlde input stream
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            //build data into string
            StringBuilder total = new StringBuilder();
            while ((line = reader.readLine()) != null) {
            total.append(line);
            }
            result = total.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //send broadcast back to Activity with json result data
        Intent intentUpdate = new Intent();
        intentUpdate.setAction(receiver);
        intentUpdate.addCategory(Intent.CATEGORY_DEFAULT);
        intentUpdate.putExtra(RESULT, result);
        sendBroadcast(intentUpdate);
    }
}
