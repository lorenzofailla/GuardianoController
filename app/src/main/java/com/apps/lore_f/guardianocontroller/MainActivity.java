package com.apps.lore_f.guardianocontroller;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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

    private static final String ONLINE_DEVICES_CHILD="online_devices";

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public static class OnlineDevicesHolder extends RecyclerView.ViewHolder {
        public TextView txvDeviceID;

        public OnlineDevicesHolder(View v) {
            super(v);
            txvDeviceID = (TextView) itemView.findViewById(R.id.TXV___ONLINEDEVICE___DEVICE_ID);

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
                firebaseDatabaseReference.child(firebaseUser.getUid())) {

            @Override
            protected OnlineDeviceMessage parseSnapshot(DataSnapshot snapshot) {
                OnlineDeviceMessage onlineDeviceMessage = super.parseSnapshot(snapshot);
                if (onlineDeviceMessage != null) {
                    onlineDeviceMessage.setId(snapshot.getKey());
                }
                return onlineDeviceMessage;
            }

            @Override
            protected void populateViewHolder(OnlineDevicesHolder viewHolder, OnlineDeviceMessage onlineDeviceMessage, int position) {
                progressBar.setVisibility(ProgressBar.INVISIBLE);
                viewHolder.txvDeviceID.setText(onlineDeviceMessage.getDeviceDescription());

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

}
