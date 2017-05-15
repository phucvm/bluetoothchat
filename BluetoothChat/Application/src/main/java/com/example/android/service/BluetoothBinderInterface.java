package com.example.android.service;

import android.content.Intent;

/**
 * Created by phucvm on 5/11/17.
 */

public interface BluetoothBinderInterface {
    public void startBluetoothService(Intent data, boolean secure);
    public void sendMessage(String message);
    public void receiveMessage(String message);
}
