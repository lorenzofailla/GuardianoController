package com.apps.lore_f.guardianocontroller;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.messaging.RemoteMessage;

public class DeviceControlActivity extends AppCompatActivity {

    String deviceName;
    String deviceToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_control);

        // ottiene il nome e il token del dispositivo remoto
        Bundle extras = getIntent().getExtras();
        if (extras != null) {

            deviceName = extras.getString("_device_name");
            deviceToken = extras.getString("_device_token");

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

        updateUI();

    }

    private void updateUI(){

        TextView deviceNameTextView = (TextView) findViewById(R.id.TXV___DEVICECONTROLACTIVITY___DEVICENAMELABEL);
        deviceNameTextView.setText(deviceName);


    }

    private void requestShot(){

        // TODO: 22/01/2017 implementare

    }
}
