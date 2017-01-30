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

public class PictureViewerActivity extends AppCompatActivity {

    static final String TAG = "->PictureViewerActivity";
    String mediaID;

    DatabaseReference currentMediaReference;
    PictureTakenMessage currentMedia;

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

        DatabaseReference uploadedMediaDatabaseReference =
                FirebaseDatabase.getInstance().getReference().
                        child(FirebaseAuth.getInstance().getCurrentUser().getUid()).
                        child(getString(R.string.GLOBAL___UPLOADED_MEDIA));
        Query retrieveMediaQuery = uploadedMediaDatabaseReference.orderByChild("id").equalTo(mediaID);
        retrieveMediaQuery.addListenerForSingleValueEvent(retrieveMediaQueryValueEventListener);

        ImageButton deleteMediaItemButton = (ImageButton) findViewById(R.id.BTN___PICTUREVIEWERACTIVITY___DELETE_PICTURE);
        deleteMediaItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // TODO: 30/gen/2017 inserire messagebox per conferma.
                //currentMediaReference.removeValue();

            }

        });

    }

    ValueEventListener retrieveMediaQueryValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {

            currentMediaReference = dataSnapshot.getRef();
            //currentMedia = (PictureTakenMessage) dataSnapshot.getValue();
            //loadPicture();

            Log.d(TAG, "datasnapshot value: " + dataSnapshot.getValue());

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }

    };

    void loadPicture(){

        Toast.makeText(this, currentMedia.getPictureURL(), Toast.LENGTH_SHORT).show();

    }

}
