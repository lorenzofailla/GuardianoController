package com.apps.lore_f.guardianocontroller;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

public class PictureViewerActivity extends AppCompatActivity {

    static final String TAG = "->PictureViewerActivity";
    String mediaID;

    DatabaseReference currentMediaReference;
    PictureTakenMessage currentMedia;
    ImageButton deleteMediaImageButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_viewer);

        // ottiene il nome e il token del dispositivo remoto
        Bundle extras = getIntent().getExtras();
        if (extras != null) {

            mediaID = extras.getString("_media-id");
            Log.d(TAG, "media id:" + mediaID);


        } else {

            // TODO: 30/gen/2017 inserire messagebox per messaggio utente
            finish();
            return;

        }

        /*
        * Recupera le informazioni dell'elemento multimediale tramite un ListenerForSingleValueEvent
        * */

        currentMediaReference = FirebaseDatabase.getInstance().getReference().
                child(FirebaseAuth.getInstance().getCurrentUser().getUid()).
                child(getString(R.string.GLOBAL___UPLOADED_MEDIA)).child(mediaID);

        currentMediaReference.addListenerForSingleValueEvent(retrieveMedia);

        deleteMediaImageButton = (ImageButton) findViewById(R.id.BTN___PICTUREVIEWERACTIVITY___DELETE_PICTURE);
        deleteMediaImageButton.setEnabled(false);
        deleteMediaImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // TODO: 30/gen/2017 inserire messagebox per conferma.
                currentMediaReference.removeValue(new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                        if (databaseError!=null) {

                            Log.d(TAG, "onComplete: " + databaseError.getMessage());

                        } else {

                            finish();
                            return;

                        }

                    }
                });

            }

        });

    }

    ValueEventListener retrieveMedia = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {


            Log.d(TAG, dataSnapshot.toString());

            currentMedia = new PictureTakenMessage(
                    dataSnapshot.child("dateStamp").getValue().toString(),
                    dataSnapshot.child("generalInfo").getValue().toString(),
                    dataSnapshot.child("pictureURL").getValue().toString()
            );

            deleteMediaImageButton.setEnabled(true);

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }

    };

    void loadPicture(){

        //Toast.makeText(this, currentMedia.getPictureURL(), Toast.LENGTH_SHORT).show();

    }

}
