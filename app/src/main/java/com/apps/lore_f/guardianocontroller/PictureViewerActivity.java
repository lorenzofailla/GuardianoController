package com.apps.lore_f.guardianocontroller;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.media.Image;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

public class PictureViewerActivity extends AppCompatActivity {

    private static final String TAG = "->PictureViewerActivity";
    private String mediaID;

    private DatabaseReference currentMediaReference;
    private PictureTakenMessage currentMedia;
    private ImageButton deleteMediaImageButton;

    private ProgressDialog deletingMedia;
    private ProgressDialog downloadingMedia;

    private ScaleGestureDetector scaleGestureDetector;
    private Matrix matrix = new Matrix();

    private ImageView pictureViewer;

    private File mediaStorageDir;
    private float scaleFactor;

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

        mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), getString(R.string.app_name));

        pictureViewer = (ImageView) findViewById(R.id.IVW___PICTUREVIEWERACTIVITY___PICTUREVIEWER);

        scaleGestureDetector = new ScaleGestureDetector(PictureViewerActivity.this,new ScaleListener());

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
                deleteElement();


    }

    private void deleteElement(){

        deletingMedia = new ProgressDialog(PictureViewerActivity.this);
        deletingMedia.setIndeterminate(true);
        deletingMedia.setCancelable(false);
        deletingMedia.setTitle(getString(R.string.PictureViewerActivity_waitForElementDeletionTitle));
        deletingMedia.setMessage(getString(R.string.PictureViewerActivity_removingFromDatabase));
        deletingMedia.show();

        currentMediaReference.removeValue(new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                if (databaseError!=null) {

                    // TODO: 04/02/2017 messaggio utente

                } else {

                    /* non si sono riscontrati errori, si procede ad eliminare il file dal cloud
                    storage */

                    deletingMedia.setMessage(getString(R.string.PictureViewerActivity_removingFromCloudStorage));
                    StorageReference httpsReference = FirebaseStorage.getInstance().getReferenceFromUrl(currentMedia.getPictureURL());
                    httpsReference.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            /* rimozione dal cloud completata, si procede ad eliminare il file dal
                            locacl storage*/

                            deletingMedia.setMessage(getString(R.string.PictureViewerActivity_removingFromCloudStorage));

                        }

                    });

                    finish();
                    return;

                }

            }
        });

    }

        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        Log.d(TAG, "onTouchEvent");
        scaleGestureDetector.onTouchEvent(ev);

        return true;

    }

        private class ScaleListener extends ScaleGestureDetector.
            SimpleOnScaleGestureListener {

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scaleFactor *= detector.getScaleFactor();
            scaleFactor = Math.max(0.1f, Math.min(scaleFactor, 5.0f));

            Log.d(TAG, String.format("scale factor: %1.2f", scaleFactor));

            matrix.setScale(scaleFactor, scaleFactor);
            pictureViewer.setImageMatrix(matrix);
            return true;

        }

    }

    ValueEventListener retrieveMedia = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {

            Log.d(TAG, dataSnapshot.toString());

            currentMedia = new PictureTakenMessage(
                    dataSnapshot.child("dateStamp").getValue().toString(),
                    dataSnapshot.child("generalInfo").getValue().toString(),
                    dataSnapshot.child("pictureURL").getValue().toString(),
                    dataSnapshot.child("sourceDevice").getValue().toString()
            );

            deleteMediaImageButton.setEnabled(true);

            pictureViewer.setImageBitmap(loadPicture());

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }

    };

    private void checkIfPicturesFolderExists(){

        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {

            if (!mediaStorageDir.mkdirs()) {

                Log.d(TAG, "failed to create directory");
                return;

            }
        }
    }

    private Bitmap loadPicture(){

        /* controlla che nello storage locale esista un file che risponda alla naming convention
        * se esiste, carica la risorsa in memoria
        * se non esiste, scarica la risorsa dal cloud e la salva in un file locale secondo la naming convention
        * */

        checkIfPicturesFolderExists();

        final File mediaFile =  new File(
                mediaStorageDir.getPath() +
                        File.separator +
                        "IMG_" + currentMedia.getSourceDevice()+"_"+currentMedia.getDateStamp() + ".jpg"
        );

        if(!mediaFile.exists()) {
            // il file non esiste, si procede a scaricarlo dal cloud

            // inizializza e mostra la ProgressDialog
            downloadingMedia = new ProgressDialog(this);
            downloadingMedia.setTitle(getString(R.string.PictureViewerActivity_downloadingResourceFromCloudTitle));
            downloadingMedia.setMessage(getString(R.string.PictureViewerActivity_downloadingResourceFromCloud));
            downloadingMedia.setIndeterminate(false);
            downloadingMedia.setCancelable(true);
            downloadingMedia.setMax(10000);
            downloadingMedia.setProgress(0);
            downloadingMedia.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

            downloadingMedia.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {

                    finish();
                    return;

                }

            });

            downloadingMedia.show();

            StorageReference httpsReference = FirebaseStorage.getInstance().getReferenceFromUrl(currentMedia.getPictureURL());
            httpsReference.getFile(mediaFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {

                    downloadingMedia.dismiss();
                    pictureViewer.setImageBitmap(BitmapFactory.decodeFile(mediaFile.getPath()));
                }

            }).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {

                    downloadingMedia.setProgress((int) (10000.0*taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount()));

                }

            });

            return null;

        } else {

            return BitmapFactory.decodeFile(mediaFile.getPath());

        }
    }

}
