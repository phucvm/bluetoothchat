package com.example.android.adapter;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.bluetoothchat.R;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by phucvm on 5/17/17.
 */

public class ApplicationAdapter extends RecyclerView.Adapter<ApplicationAdapter.Holder> {

    private Context context;
    private List<ApplicationInfo> packages;
    private PackageManager pm;

    public ApplicationAdapter(Context context, List<ApplicationInfo> packages) {
        this.context = context;
        this.packages = packages;
        pm = context.getPackageManager();
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.application, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        ApplicationInfo appInfo = packages.get(position);
        holder.ivApplication.setImageDrawable(appInfo.loadIcon(pm));
        holder.tvApplicationName.setText(appInfo.packageName);

        String appFile = appInfo.sourceDir;
        long installed = new File(appFile).lastModified(); //Epoch Time
        final String dateString = new SimpleDateFormat("MM/dd/yyyy", context.getResources().getConfiguration().locale).format(new Date(installed));

        holder.tvApplicateDate.setText(dateString);
    }

    @Override
    public int getItemCount() {
        return packages.size();
    }

    public class Holder extends RecyclerView.ViewHolder{
        @Bind(R.id.iv_app_icon) ImageView ivApplication;
        @Bind(R.id.tv_app_name) TextView tvApplicationName;
        @Bind(R.id.tv_date) TextView tvApplicateDate;

        public Holder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }


    }
}
