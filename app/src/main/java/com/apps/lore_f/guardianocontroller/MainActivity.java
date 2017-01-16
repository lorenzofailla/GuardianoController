package com.apps.lore_f.guardianocontroller;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;

    private String recipientTokenID="f7kmpKl5LJg:APA91bEZ2_tXG_EsLaAVxN-PO1Vk7-1ga-FhHi_sidZLiJS5hwJrurxA7nb3P3mHHhRDqG48wzKCxelKgPqjgch0KwMFDd63m9Cqbu0YIyQ4763Emqm933We4H5AhNgDWNxFwOCuorL8";
    private String authTokenID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*
        controlla che i permessi siano stati dati.
        in assenza dei permessi, lancia l'activity per richiedere i permessi
        altrimenti inizializza i controlli
         */

        if(!SharedFunctions.checkPermissions(this)){
            //
            //
            startActivity(new Intent(this, PermissionsRequestActivity.class));
            finish();
            return;

        }

        // inizializza il FirebaseAuth
        firebaseAuth= FirebaseAuth.getInstance();

        // ottiene l'user corrente
        firebaseUser = firebaseAuth.getCurrentUser();

        if(firebaseUser==null){
            // autenticazione non effettuata

            // lancia la SignInActivity e termina l'attività corrente
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;

        }

        FirebaseInstanceId firebaseInstanceId = FirebaseInstanceId.getInstance();
        String thisDeviceToken = firebaseInstanceId.getToken();

        try {
            authTokenID = firebaseInstanceId.getToken(thisDeviceToken, "FCM");
        } catch (IOException e) {
            e.printStackTrace();
        }


        Button btnRequestInstantPicture = (Button) findViewById(R.id.BTN_REMOTECONTROL_REQUEST_INSTANT_PICTURE);
        btnRequestInstantPicture.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        requestPicture();

                    }

                }
        );

    }

    private void requestPicture(){

        // ottiene un'istanza di FirebaseMessaging
        FirebaseMessaging firebaseMessaging = FirebaseMessaging.getInstance();

        RemoteMessage remoteMessage = new RemoteMessage.Builder(recipientTokenID)
                .addData("body", "COMMAND_FROM_CLIENT:::TAKE_PICTURE")
                .setTtl(120)
                .build();

        firebaseMessaging.send(remoteMessage);

    }

}
