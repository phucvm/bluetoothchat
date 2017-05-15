package com.example.android.service;

import android.content.Intent;
import android.os.Binder;

/**
 * Created by phucvm on 5/11/17.
 */

public class BluetoothBinder extends Binder implements BluetoothBinderInterface {
    @Override
    public void startBluetoothService(Intent data, boolean secure) {

    }

    @Override
    public void sendMessage(String message) {

    }

    @Override
    public void receiveMessage(String message) {

    }
}
