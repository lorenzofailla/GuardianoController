package com.apps.lore_f.guardianocontroller;

import android.app.DownloadManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
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
    public static final String FCM_MESSAGE_URL = "http://lorenzofailla.esy.es/Guardiano/Messaging/";

    private String messagingToken;

    private ProgressBar progressBar;
    private LinearLayoutManager linearLayoutManager;
    private RecyclerView takenPicturesRecyclerView;
    private DatabaseReference firebaseDatabaseReference;
    private FirebaseUser firebaseUser;

    private FirebaseRecyclerAdapter<PictureTakenMessage, TakenPicturesHolder> firebaseAdapter;

    private static final String TAKEN_PICTURES_CHILD = "pictures_taken";

    private String[] mediaIDs;

    public static class TakenPicturesHolder extends RecyclerView.ViewHolder {

        public TextView txvPictureDate;
        public ImageButton btnGoToPicture;

        public TakenPicturesHolder(View v) {
            super(v);
            txvPictureDate = (TextView) itemView.findViewById(R.id.TXV___TAKENPICTURES___PICTUREDATE);
            btnGoToPicture = (ImageButton) itemView.findViewById(R.id.BTN___TAKENPICTURES___PICTURETHUMBNAIL);

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

            Log.d(TAG, "Device token: " + deviceToken);

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

        progressBar = (ProgressBar) findViewById(R.id.PBR___DEVICECONTROLACTIVITY___PROGRESSBAR);
        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        takenPicturesRecyclerView = (RecyclerView) findViewById(R.id.RVW___DEVICECONTROLACTIVITY___TAKENPICTURES);
        firebaseDatabaseReference= FirebaseDatabase.getInstance().getReference();

        DatabaseReference listOfMedia = firebaseDatabaseReference.child(firebaseUser.getUid()).child(getString(R.string.GLOBAL___UPLOADED_MEDIA));
        listOfMedia.addListenerForSingleValueEvent(listOfMediaIsReady);
        // l'esecuzione si sposta sul listener listOfMediaIsReady

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

        sendMessage(deviceToken, "COMMAND_FROM_CLIENT:::TAKE_PICTURE");

    }

    public void sendMessage(final String recipient, final String message) {

        new AsyncTask<String, String, String>() {
            @Override
            protected String doInBackground(String... params) {
                try {

                    return getResponseFromMessagingServer(
                            "http://lorenzofailla.esy.es/Guardiano/Messaging/sendmessage.php?Action=M&t=title&m="+message+"&r="+recipient+"");

                    // TODO: 29/01/2017 implementare, sia lato server che lato client, il time to live del messaggio 
                    
                } catch (Exception e) {

                    e.printStackTrace();
                    return null;

                }

            }

            @Override
            protected void onPostExecute(String result) {
                try {

                    if(result!=null) {

                        JSONObject resultJson = new JSONObject(result);
                        int success, failure;
                        success = resultJson.getInt("success");
                        failure = resultJson.getInt("failure");

                        Log.d(TAG, "got response from messaging server: " + success + " success, " + failure + " failure");

                    } else {

                        Log.d(TAG, "got NULL response from messaging server");

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    String getResponseFromMessagingServer(String requestUrl) throws IOException {

        Request request = new Request.Builder()
                .url(requestUrl)
                .build();

        Response response = okHttpClient.newCall(request).execute();

        return response.body().string();

    }

    ValueEventListener listOfMediaIsReady = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {

            mediaIDs = new String[(int) dataSnapshot.getChildrenCount()];
            int arrayIndex = 0;

            for (DataSnapshot child : dataSnapshot.getChildren()) {

                mediaIDs[arrayIndex] = child.getKey();
                arrayIndex++;

            }

            initializeAdapter();
            updateUI();

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    void initializeAdapter(){


        firebaseAdapter = new FirebaseRecyclerAdapter<PictureTakenMessage, TakenPicturesHolder>(
                PictureTakenMessage.class,
                R.layout.taken_picture,
                TakenPicturesHolder.class,
                firebaseDatabaseReference.child(firebaseUser.getUid()).child(TAKEN_PICTURES_CHILD)) {

            @Override
            protected void populateViewHolder(TakenPicturesHolder viewHolder, final PictureTakenMessage pictureTakenMessage, final int position) {

                progressBar.setVisibility(ProgressBar.INVISIBLE);

                viewHolder.txvPictureDate.setText(pictureTakenMessage.getDateStamp());
                viewHolder.btnGoToPicture.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Intent intent = new Intent(getApplicationContext(), PictureViewerActivity.class);
                        intent.putExtra("_media-id", mediaIDs[position]);
                        startActivity(intent);

                    }

                });

            }

        };

        firebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int onlineDevicesCount = firebaseAdapter.getItemCount();
                int lastVisiblePosition = linearLayoutManager.findLastCompletelyVisibleItemPosition();
                // If the recycler view is initially being loaded or the user is at the bottom of the list, scroll
                // to the bottom of the list to show the newly added message.
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (onlineDevicesCount - 1) && lastVisiblePosition == (positionStart - 1))) {
                    takenPicturesRecyclerView.scrollToPosition(positionStart);
                }

            }

        });

        takenPicturesRecyclerView.setLayoutManager(linearLayoutManager);
        takenPicturesRecyclerView.setAdapter(firebaseAdapter);

    }

}
