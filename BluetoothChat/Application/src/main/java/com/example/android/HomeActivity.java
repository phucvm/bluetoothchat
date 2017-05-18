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
import butterknife.OnClick;

public class HomeActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);

    }

    @OnClick(R.id.bt_vr)
    void onVRClick() {
        startApp(false);
    }

    @OnClick(R.id.bt_master)
    void onMasterClick() {
        startApp(true);
    }

    @OnClick(R.id.bt_music)
    void onMusicClick() {
        Intent intent = new Intent(HomeActivity.this, MusicActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.bt_package)
    void onPackageClick() {
        Intent intent = new Intent(HomeActivity.this, PackageNameActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.bt_download)
    void onDownloadClick() {
        Intent intent = new Intent(HomeActivity.this, DownloadActivity.class);
        startActivity(intent);
    }

    private void startApp(boolean isMasterApp) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("isMasterApp", isMasterApp);
        startActivity(intent);
    }
}
