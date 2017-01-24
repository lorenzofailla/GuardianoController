package com.apps.lore_f.guardianocontroller;

import android.app.DownloadManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DeviceControlActivity extends AppCompatActivity {

    private static final String TAG = "->DeviceControlActivity";
    private String deviceName;
    private String deviceToken;
    OkHttpClient okHttpClient = new OkHttpClient();
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    public static final String FCM_MESSAGE_URL = "https://fcm.googleapis.com/fcm/send";

    private String messagingToken;

    /*
    private class GetMessagingToken extends AsyncTask<String, Void, String> {
        // The system calls this to perform work in a worker thread and
        // delivers it the parameters given to AsyncTask.execute()
        protected String doInBackground(String... tokens) {

            try {

                Log.d(TAG, "initiated messaging token request");
                return FirebaseInstanceId.getInstance().getToken(tokens[0], "FCM");


            } catch (IOException e) {

                Log.d(TAG, "failed obtaining messaging token");
                return "";
            }

        }

        // The system calls this to perform work in the UI thread and delivers
        // the result from doInBackground()

        protected void onPostExecute(String result) {

            messagingToken = result;
            Log.d(TAG, "obtained messaging token:" + result);
            updateUI();

        }

    }
    */


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_control);

        // ottiene il nome e il token del dispositivo remoto
        Bundle extras = getIntent().getExtras();
        if (extras != null) {

            deviceName = extras.getString("_device-name");
            deviceToken = extras.getString("_device-token");

        } else {

            finish();
            return;

        }

        Button takeShotButton = (Button) findViewById(R.id.BTN___DEVICECONTROLACTIVITY___REQUESTSHOTBUTTON);
        takeShotButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                requestShot();

            }

        });

        messagingToken="AIzaSyAM-GVRMIkYoUuEiU-8PvVjw-zaws8h1xw";
        //new GetMessagingToken().execute(FirebaseInstanceId.getInstance().getId());
        updateUI();

    }

    private void updateUI(){

        TextView deviceNameTextView = (TextView) findViewById(R.id.TXV___DEVICECONTROLACTIVITY___DEVICENAMELABEL);
        deviceNameTextView.setText(deviceName);

        Button takeShotButton = (Button) findViewById(R.id.BTN___DEVICECONTROLACTIVITY___REQUESTSHOTBUTTON);
        if(messagingToken!=""){

            takeShotButton.setEnabled(true);

        } else {

            takeShotButton.setEnabled(false);

        }

    }

    private void requestShot(){

        sendMessage(deviceToken+"@gcm.googleapis.com", "il mio messaggio");

    }



    public void sendMessage(final String recipient, final String message) {

        new AsyncTask<String, String, String>() {
            @Override
            protected String doInBackground(String... params) {
                try {
                    JSONObject root = new JSONObject();
                    JSONObject notification = new JSONObject();
                    notification.put("body", message);

                    JSONObject data = new JSONObject();
                    data.put("message", message);
                    root.put("data", data);
                    root.put("to", recipient);

                    String result = postToFCM(root.toString());
                    Log.d(TAG, "Result: " + result);
                    return result;

                } catch (Exception ex) {

                    ex.printStackTrace();

                }

                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                try {
                    JSONObject resultJson = new JSONObject(result);
                    int success, failure;
                    success = resultJson.getInt("success");
                    failure = resultJson.getInt("failure");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    String postToFCM(String bodyString) throws IOException {

        RequestBody body = RequestBody.create(JSON, bodyString);

        Request request = new Request.Builder()
                .url(FCM_MESSAGE_URL)
                .post(body)
                .addHeader("Authorization", "key=" + messagingToken)
                .build();

        Response response = okHttpClient.newCall(request).execute();

        return response.body().string();

    }

}
