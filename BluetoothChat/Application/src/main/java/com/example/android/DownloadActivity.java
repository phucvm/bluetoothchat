package com.example.android;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.android.bluetoothchat.R;

import java.io.File;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class DownloadActivity extends Activity {

    private DownloadManager downloadManager;

    private long imageDownloadID, musicDownloadID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
        ButterKnife.bind(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermission();
        }

        IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        registerReceiver(downloadReceiver, filter);
    }

    @OnClick(R.id.bt_start_download)
    void onStartDownloadClick() {
        Uri music_uri = Uri.parse("http://www.androidtutorialpoint.com/wp-content/uploads/2016/09/AndroidDownloadManager.mp3");
        musicDownloadID = download(music_uri);
    }

    private long download(Uri uri) {

        long downloadReference;

        downloadManager = (DownloadManager)getSystemService(DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(uri);

        //Setting title of request
        request.setTitle("Downloading ...");

        //Setting description of request
//        request.setDescription("Android Data download using DownloadManager.");



        File path = new File(android.os.Environment.getExternalStorageDirectory() +"/Download/Content/AndroidDownloadManager.mp3");
        if (path.exists()) path.delete();
        request.setDestinationUri(Uri.fromFile(path));





                //Set the local destination for the downloaded file to a path within the application's external files directory
//        if(v.getId() == R.id.DownloadMusic)
//            request.setDestinationInExternalFilesDir(DownloadActivity.this, Environment.DIRECTORY_DOWNLOADS,"AndroidTutorialPoint.mp3");
//        else if(v.getId() == R.id.DownloadImage)
//            request.setDestinationInExternalFilesDir(DownloadActivity.this, Environment.DIRECTORY_DOWNLOADS,"AndroidTutorialPoint.jpg");

        //Enqueue download and save the referenceId
        downloadReference = downloadManager.enqueue(request);

        return downloadReference;
    }

    private boolean requestPermission() {
        String[] permissions = { Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        boolean isGrantedBluetooth = isGrantedPermission(this, permissions);

        if (!isGrantedBluetooth) {
            requestPermission(this, permissions, 111);
            return false;
        }

        return true;
    }

    void requestPermission(Activity activity, String[] permissions, int requestCode) {
        ActivityCompat.requestPermissions(activity,
                permissions,
                requestCode);
    }

    boolean isGrantedPermission (Activity activity, String... permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(activity,
                    permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private BroadcastReceiver downloadReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            //check if the broadcast message is for our Enqueued download
            long referenceId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);

            if(referenceId == imageDownloadID) {
                Toast.makeText(DownloadActivity.this, "Image Download Complete", Toast.LENGTH_LONG).show();
//                Toast toast = Toast.makeText(DownloadActivity.this, "Image Download Complete", Toast.LENGTH_LONG);
//                toast.setGravity(Gravity.TOP, 25, 400);
//                toast.show();
            }
            else if(referenceId == musicDownloadID) {
                Toast.makeText(DownloadActivity.this, "Music Download Complete", Toast.LENGTH_LONG).show();
//                Toast toast = Toast.makeText(DownloadActivity.this, "Music Download Complete", Toast.LENGTH_LONG);
//                toast.setGravity(Gravity.TOP, 25, 400);
//                toast.show();
            }
        }
    };
}
