package com.example.android;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;

import com.example.android.bluetoothchat.MainActivity;
import com.example.android.bluetoothchat.R;
import com.example.android.common.logger.Log;
import com.example.android.service.BluetoothService;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

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

        Log.d("2359", "requestIgnoreBattery Request Ignore Battery");
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent();
            String packageName = getPackageName();
            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + packageName));
                startActivity(intent);
            }
        }

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

    @OnClick(R.id.bt_notification)
    void onNotificationClick() {
        Intent intent = new Intent(HomeActivity.this, NotificationActivity.class);
        startActivity(intent);
    }

    private void startApp(boolean isMasterApp) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("isMasterApp", false);
        startActivity(intent);
    }
}
