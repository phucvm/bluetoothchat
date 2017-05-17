package com.example.android;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
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
    }

    private void startApp(boolean isMasterApp) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("isMasterApp", isMasterApp);
        startActivity(intent);
    }
}
