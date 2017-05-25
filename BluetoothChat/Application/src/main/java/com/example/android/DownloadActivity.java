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
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.android.bluetoothchat.R;

import java.io.File;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DownloadActivity extends Activity {

    private DownloadManager downloadManager;

    private long imageDownloadID, videoDownloadID;

    @Bind(R.id.bt_play) Button btPlay;

    private File videoFile;

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

        videoFile = new File(android.os.Environment.getExternalStorageDirectory() +"/Download/Content/Video.mp4");

        btPlay.setVisibility(videoFile.exists() ? View.VISIBLE : View.GONE);
    }

    @OnClick(R.id.bt_start_download)
    void onStartDownloadClick() {
        Uri videoUri = Uri.parse("https://s3-ap-southeast-1.amazonaws.com/samsung-marvel-stg/assets%2F4-40w1p2ncz6v%2F%5BSamsung%5D+360+video+demo.mp4");
        videoDownloadID = download(videoUri);
    }

    @OnClick(R.id.bt_play)
    void onPlayVideoClick() {
        if (videoFile != null && videoFile.exists()) {

            Uri uri = Uri.parse("file://" + videoFile.getPath());
            Intent intent = new Intent();
            intent.setClassName("com.samsung.android.gear360viewer", "com.samsung.android.gear360viewer.videoplayer360.VideoPlayer360Activity");
            intent.setData(uri);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(downloadReceiver);
    }

    private long download(Uri uri) {

        long downloadReference;

        downloadManager = (DownloadManager)getSystemService(DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(uri);

        //Setting title of request
        request.setTitle("Downloading ...");

        //Setting description of request
//        request.setDescription("Android Data download using DownloadManager.");


        if (videoFile.exists()) videoFile.delete();
        request.setDestinationUri(Uri.fromFile(videoFile));





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
            }
            else if(referenceId == videoDownloadID) {
                Toast.makeText(DownloadActivity.this, "Video Download Complete", Toast.LENGTH_LONG).show();
                btPlay.setVisibility(View.VISIBLE);
            }
        }
    };
}
