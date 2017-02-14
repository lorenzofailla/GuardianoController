package com.apps.lore_f.guardianocontroller;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
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
import com.google.firebase.appindexing.FirebaseAppIndex;
import com.google.firebase.appindexing.FirebaseUserActions;
import com.google.firebase.appindexing.Indexable;
import com.google.firebase.appindexing.builders.Indexables;
import com.google.firebase.appindexing.builders.PersonBuilder;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;

    private RecyclerView onlineDevicesRecyclerView;
    private DatabaseReference firebaseDatabaseReference;
    private FirebaseRecyclerAdapter<OnlineDeviceMessage, OnlineDevicesHolder> firebaseAdapter;
    private ProgressBar progressBar;
    private LinearLayoutManager linearLayoutManager;
    private GoogleApiClient googleApiClient;
    private String deviceTokenToConnect;
    private String deviceNameToConnect;
    private int timeOutForDeviceHeartBeat = 30;
    private int timeElapsedForDeviceHeartBeat;
    private LocalBroadcastManager localBroadcastManager;
    private static final String ONLINE_DEVICES_CHILD="online_devices";
    private static final String TAG="->MainActivity";
    private ProgressDialog waitForDeviceHeartBeat;

    private Handler tasksHandler = new Handler();

    private Runnable tickTimeoutForDeviceHeartBeat = new Runnable() {
        @Override
        public void run() {

            /* controlla il raggiungimento del timeout
            * se non si è ancora raggiunto il timeout, incrementa il contatore del tempo trascorso e
            * passa il valore alla ProgressDialog
            * il Runnable viene rilanciato con ritardo di 1 secondo
            *
            * altrimenti: elimina la registrazione del dispositivo remoto dal database
            * */

            if (timeElapsedForDeviceHeartBeat++<timeOutForDeviceHeartBeat){

                waitForDeviceHeartBeat.setProgress(timeElapsedForDeviceHeartBeat);
                tasksHandler.postAtTime(this, SystemClock.uptimeMillis() + 1000);

            } else {

                waitForDeviceHeartBeat.dismiss();
                /*
                Query selectedDevice=firebaseDatabaseReference
                        .orderByChild("deviceDescription")
                        .equalTo(deviceNameToConnect);

                selectedDevice.addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        dataSnapshot.getRef().removeValue(new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                                waitForDeviceHeartBeat.dismiss();

                            }

                        });

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }

                });
                */

            }

        }

    };

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            switch (intent.getAction()){

                case "GUARDIANOCONTROLLER___HEARTBEAT_RECEIVED":

                    Log.d(TAG, "remote device heartbeat received");

                    if(intent.hasExtra("_sender-token")){

                        String senderToken = intent.getStringExtra("_sender-token");

                        if (senderToken.equals(deviceTokenToConnect)) {

                            /* il dispositivo a cui si era chiesto di connettersi ha risposto con un heartbeat */

                            // rimuove eventuali tasks in sospeso
                            tasksHandler.removeCallbacks(tickTimeoutForDeviceHeartBeat);

                            // chiude la ProgressDialog
                            waitForDeviceHeartBeat.dismiss();

                            // avvia DeviceControlActivity
                            Intent startActivityIntent = new Intent(getApplicationContext(), DeviceControlActivity.class)
                                    .putExtra("_device-name", deviceNameToConnect)
                                    .putExtra("_device-token", deviceTokenToConnect);

                            startActivity(startActivityIntent);
                            return;

                        }


                    }
                    break;

            }

        }
        
    };

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public static class OnlineDevicesHolder extends RecyclerView.ViewHolder {

        public TextView txvDeviceID;
        public ImageButton btnGoToDeviceControl;

        public OnlineDevicesHolder(View v) {
            super(v);
            txvDeviceID = (TextView) itemView.findViewById(R.id.TXV___ONLINEDEVICE___DEVICE_ID);
            btnGoToDeviceControl = (ImageButton) itemView.findViewById(R.id.BTN___ONLINEDEVICE___DEVICE_IMG);

        }

    }

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

        FirebaseMessaging firebaseMessaging = FirebaseMessaging.getInstance();
        firebaseMessaging.subscribeToTopic("notifications");

        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();

        progressBar = (ProgressBar) findViewById(R.id.PBR___MAINACTIVITY___PROGRESSBAR);
        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);

        onlineDevicesRecyclerView = (RecyclerView) findViewById(R.id.RVW___MAINACTIVITY___ONLINEDEVICES);

        firebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();

        firebaseAdapter = new FirebaseRecyclerAdapter<OnlineDeviceMessage, OnlineDevicesHolder>(
                OnlineDeviceMessage.class,
                R.layout.online_device,
                OnlineDevicesHolder.class,
                firebaseDatabaseReference.child(firebaseUser.getUid()).child(ONLINE_DEVICES_CHILD)) {


            @Override
            protected void populateViewHolder(OnlineDevicesHolder viewHolder, final OnlineDeviceMessage onlineDeviceMessage, int position) {

                progressBar.setVisibility(ProgressBar.INVISIBLE);

                viewHolder.txvDeviceID.setText(onlineDeviceMessage.getDeviceDescription());
                viewHolder.btnGoToDeviceControl.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        deviceTokenToConnect=onlineDeviceMessage.getDeviceToken();
                        deviceNameToConnect=onlineDeviceMessage.getDeviceDescription();

                        Messaging.sendMessage(deviceTokenToConnect, "COMMAND_FROM_CLIENT:::ARE_YOU_ALIVE",FirebaseInstanceId.getInstance().getToken());

                        timeElapsedForDeviceHeartBeat=0;

                        waitForDeviceHeartBeat = new ProgressDialog(MainActivity.this);

                        waitForDeviceHeartBeat.setTitle(getString(R.string.MainActivity_waitForRemoteDeviceTitle));
                        waitForDeviceHeartBeat.setMessage(getString(R.string.MainActivity_waitForRemoteDevice));
                        waitForDeviceHeartBeat.setIndeterminate(false);
                        waitForDeviceHeartBeat.setCancelable(false);
                        waitForDeviceHeartBeat.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                        waitForDeviceHeartBeat.setMax(10);
                        waitForDeviceHeartBeat.setProgress(timeElapsedForDeviceHeartBeat);

                        waitForDeviceHeartBeat.show();

                        tasksHandler.postAtTime(tickTimeoutForDeviceHeartBeat,1000);

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
                    onlineDevicesRecyclerView.scrollToPosition(positionStart);
                }

            }

        });

        onlineDevicesRecyclerView.setLayoutManager(linearLayoutManager);
        onlineDevicesRecyclerView.setAdapter(firebaseAdapter);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.sign_out_menu:
                firebaseAuth.signOut();
                Auth.GoogleSignInApi.signOut(googleApiClient);
                firebaseUser = null;

                startActivity(new Intent(this, SignInActivity.class));
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }

    }
    
    @Override
    public void onPause(){
        
        super.onPause();

        /* registra il ricevitore di broadcast con il filtro creato */
        try {

            unregisterReceiver(broadcastReceiver);

        }catch (IllegalArgumentException e) {


        }

    }

    @Override
    public void onResume(){

        super.onResume();

        /* inizializza e popola il filtro degli intent */
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("GUARDIANOCONTROLLER___HEARTBEAT_RECEIVED");

        /* registra il ricevitore di broadcast con il filtro creato */
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.registerReceiver(broadcastReceiver, intentFilter);

    }

}
