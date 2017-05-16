package com.example.android;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.android.bluetoothchat.MainActivity;
import com.example.android.bluetoothchat.R;

public class HomeActivity extends Activity {

    private Button btVR, btMaster, btMusic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        btVR = (Button) findViewById(R.id.bt_vr);
        btMaster = (Button) findViewById(R.id.bt_master);
        btMusic = (Button) findViewById(R.id.bt_music);

        btVR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startApp(false);
            }
        });

        btMaster.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startApp(true);
            }
        });

        btMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, MusicActivity.class);
                startActivity(intent);
            }
        });
    }

    private void startApp(boolean isMasterApp) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("isMasterApp", isMasterApp);
        startActivity(intent);
    }
}
