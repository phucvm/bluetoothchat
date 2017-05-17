package com.example.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.android.bluetoothchat.MainActivity;
import com.example.android.bluetoothchat.R;

import butterknife.Bind;
import butterknife.ButterKnife;

public class HomeActivity extends Activity {

    @Bind(R.id.bt_vr) Button btVR;
    @Bind(R.id.bt_master) Button btMaster;
    @Bind(R.id.bt_music) Button btMusic;
    @Bind(R.id.bt_package) Button btPackage;
    @Bind(R.id.bt_notification) Button btNotification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);

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

        btPackage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, PackageNameActivity.class);
                startActivity(intent);
            }
        });

        btNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, NotificationActivity.class);
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
