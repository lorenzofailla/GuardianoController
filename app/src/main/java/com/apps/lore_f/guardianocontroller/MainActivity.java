package com.apps.lore_f.guardianocontroller;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        Button btnRequestInstantPicture = (Button) findViewById(R.id.BTN_REMOTECONTROL_REQUEST_INSTANT_PICTURE);
        btnRequestInstantPicture.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {



                    }
                }
        );

    }

}
