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
import java.util.ArrayList;

public class MusicActivity extends Activity {

    Button btPlay1, btPlay2, btPlay3, btPlay4;
    boolean isPlaying = false;
    MediaPlayer mediaPlayer;
    View.OnClickListener onPlayClickListener;

    ArrayList<Button> listButtons;
    String[] listFiles;
    int currentPlayPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);

        btPlay1 = (Button) findViewById(R.id.bt_play1);
        btPlay2 = (Button) findViewById(R.id.bt_play2);
        btPlay3 = (Button) findViewById(R.id.bt_play3);
        btPlay4 = (Button) findViewById(R.id.bt_play4);

        File path = android.os.Environment.getExternalStorageDirectory();
        String[] listOfFiles = {path + "/Download/Ed Sheeran - Shape of You.mp3", path + "/Samsung/Music/Over_the_Horizon.mp3",
                path + "/Download/Ed Sheeran - Shape of You.mp3", path + "/Samsung/Music/Over_the_Horizon.mp3"};
        listFiles = listOfFiles;

        onPlayClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (int i = 0; i<listButtons.size(); i++) {
                    Button button = listButtons.get(i);
                    if (button.getId() == view.getId()) {
                        if (currentPlayPosition == i && isPlaying) {
                            isPlaying = false;
                            mediaPlayer.pause();
                            button.setText("Play");
                        } else {
                            button.setText("Pause");
                            isPlaying = true;
                            playMusic(i);
                        }
                    } else {
                        button.setText("Play");
                    }
                }
            }
        };

        mediaPlayer = new MediaPlayer();

        btPlay1.setOnClickListener(onPlayClickListener);
        btPlay2.setOnClickListener(onPlayClickListener);
        btPlay3.setOnClickListener(onPlayClickListener);
        btPlay4.setOnClickListener(onPlayClickListener);

        listButtons = new ArrayList<>();
        listButtons.add(btPlay1);
        listButtons.add(btPlay2);
        listButtons.add(btPlay3);
        listButtons.add(btPlay4);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermission();
        }

    }

    private void playMusic(int position) {
        if (mediaPlayer != null && position == currentPlayPosition) {
            mediaPlayer.start();
        } else {
            mediaPlayer.reset();
//            File path = android.os.Environment.getExternalStorageDirectory();
            try {
//                mediaPlayer.setDataSource(path + "/Download/Ed Sheeran - Shape of You.mp3");
//                mediaPlayer.setDataSource(path + "/Samsung/Music/Over_the_Horizon.mp3");
                currentPlayPosition = position;
                mediaPlayer.setDataSource(listFiles[position]);
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
