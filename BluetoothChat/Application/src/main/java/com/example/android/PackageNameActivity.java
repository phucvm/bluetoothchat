package com.example.android;

import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.android.adapter.ApplicationAdapter;
import com.example.android.bluetoothchat.R;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class PackageNameActivity extends Activity {

    @Bind(R.id.application_recycler_view) RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pakage_name);
        ButterKnife.bind(this);

        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        final PackageManager pm = getPackageManager();
//get a list of installed apps.
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        Collections.sort(packages, new AppComparator());

//        for (final ApplicationInfo packageInfo : packages) {
//            if (pm.getLaunchIntentForPackage(packageInfo.packageName) != null) {
//                Log.d("2359", "Installed package :" + packageInfo.packageName);
//                Log.d("2359", "Source dir : " + packageInfo.sourceDir);
//                Log.d("2359", "Launch Activity :" + pm.getLaunchIntentForPackage(packageInfo.packageName));
//
//                String appFile = packageInfo.sourceDir;
//                long installed = new File(appFile).lastModified(); //Epoch Time
//                final String dateString = new SimpleDateFormat("MM/dd/yyyy").format(new Date(installed));
//
//                final ImageView iv = new ImageView(this);
//                iv.getLayoutParams().height = 100;
//                iv.getLayoutParams().width = 100;
//                iv.setImageDrawable(packageInfo.loadIcon(pm));
//                iv.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        Toast.makeText(PackageNameActivity.this, packageInfo.packageName + " - " + dateString, Toast.LENGTH_LONG).show();
//                    }
//                });
//                linearLayout.addView(iv);
//            }
//        }

        recyclerView.setAdapter(new ApplicationAdapter(this, packages));
    }

    class AppComparator implements Comparator<ApplicationInfo> {
        @Override
        public int compare(ApplicationInfo application1, ApplicationInfo application2) {
            String appFile1 = application1.sourceDir;
            long installed1 = new File(appFile1).lastModified();
            String appFile2 = application2.sourceDir;
            long installed2 = new File(appFile2).lastModified();
            if (installed1>installed2) return -1;
            if (installed1<installed2) return 1;
            return 0;
        }
    }
}
