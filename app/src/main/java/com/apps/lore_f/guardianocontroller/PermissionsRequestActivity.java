package com.apps.lore_f.guardianocontroller;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class PermissionsRequestActivity extends AppCompatActivity {

    private static final int PERMISSION_WRITE_EXTERNAL_STORAGE = 30;
    private static final int PERMISSION_INTERNET=40;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permissions_request);

        updateUI();

    }

    private void updateUI(){

        if(SharedFunctions.checkPermissions(this)){

            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;

        } else {

            askForMissingPermissions();

        }

    }

    private void askForMissingPermissions(){

        TextView txvPermissionWriteExternalStorageStatus = (TextView) findViewById(R.id.TXV___PERMISSIONSREQUEST___PERMISSION_WRITE_EXTERNAL_STORAGE_STATUS);
        TextView txvPermissionInternetStatus = (TextView) findViewById(R.id.TXV___PERMISSIONSREQUEST___PERMISSION_INTERNET_STATUS);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_WRITE_EXTERNAL_STORAGE);
            txvPermissionInternetStatus.setText(R.string.PermissionsRequestActivity_permissionGranted);

        } else {

            txvPermissionWriteExternalStorageStatus.setText(R.string.PermissionsRequestActivity_permissionDenied);

        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, PERMISSION_INTERNET);
            txvPermissionInternetStatus.setText(R.string.PermissionsRequestActivity_permissionDenied);

        } else {

            txvPermissionInternetStatus.setText(R.string.PermissionsRequestActivity_permissionGranted);

        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {

        updateUI();

    }


}
