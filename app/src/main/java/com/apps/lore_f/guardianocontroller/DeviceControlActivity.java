package com.apps.lore_f.guardianocontroller;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import java.io.IOException;

public class DeviceControlActivity extends AppCompatActivity {

    private static final String TAG = "->DeviceControlActivity";
    private String deviceName;
    private String deviceToken;
    private String messagingToken;

    private class GetMessagingToken extends AsyncTask<String, Void, String> {
        /** The system calls this to perform work in a worker thread and
         * delivers it the parameters given to AsyncTask.execute() */
        protected String doInBackground(String... tokens) {

            try {

                Log.d(TAG, "initiated messaging token request");
                return FirebaseInstanceId.getInstance().getToken(tokens[0], "FCM");


            } catch (IOException e) {

                Log.d(TAG, "failed obtaining messaging token");
                return "";
            }

        }

        /** The system calls this to perform work in the UI thread and delivers
        * the result from doInBackground() */
        protected void onPostExecute(String result) {

            messagingToken = result;
            Log.d(TAG, "obtained messaging token:" + result);
            updateUI();

        }

    }

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

        new GetMessagingToken().execute(deviceToken);
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

        // TODO: 22/01/2017 implementare

        FirebaseMessaging firebaseMessaging = FirebaseMessaging.getInstance();
        RemoteMessage takeShotCommand = new RemoteMessage.Builder(deviceToken+"@gcm.googleapis.com")
                .addData("hello","tentativo disperato")
                .build();

        firebaseMessaging.send(takeShotCommand);

        Log.d(TAG, "Sent message to: "+deviceToken);


    }

}
