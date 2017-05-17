package com.example.android;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;

import com.example.android.bluetoothchat.R;

import java.io.File;
import java.io.IOException;

public class MusicActivity extends Activity {

    Button btPlay;
    boolean isPlaying = false;
    MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);

        btPlay = (Button) findViewById(R.id.bt_play);
        btPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isPlaying) {
                    btPlay.setText("Pause");
                    isPlaying = true;
                    playMusic();
                } else {
                    btPlay.setText("Play");
                    isPlaying = false;
                    mediaPlayer.pause();
                }
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermission();
        }

    }

    private void playMusic() {
        if (mediaPlayer != null) {
            mediaPlayer.start();
        } else {
            mediaPlayer = new MediaPlayer();
            File path = android.os.Environment.getExternalStorageDirectory();
            try {
                mediaPlayer.setDataSource(path + "/Samsung/Music/Over_the_Horizon.mp3");
                mediaPlayer.prepare();
                mediaPlayer.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
    }

    private boolean requestPermission() {
        String[] permissions = { Manifest.permission.READ_EXTERNAL_STORAGE};
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
}
