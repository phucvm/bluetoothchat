package com.example.android.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.example.android.NotificationActivity;
import com.example.android.bluetoothchat.BluetoothChatService;
import com.example.android.bluetoothchat.Constants;
import com.example.android.bluetoothchat.R;

/**
 * Created by phucvm on 5/2/17.
 */

public class BluetoothService extends Service{

    BluetoothBinder binder;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        return binder;
    }

    private BluetoothAdapter mBluetoothAdapter = null;
    private Handler mMainHandler;

    private BluetoothChatService mChatService = null;
    private String mConnectedDeviceName = null;

    private String address = "";
    private String action = "";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            address = intent.getStringExtra("ADDRESS");
            action = intent.getStringExtra("ACTION");
        }
        Log.d("2359", "Start Command "+address);
        if (address != null  && address.length() > 0) {
            connectDevice(address, true);
        }

        if (action != null && action.equals("SEND")) {
            String content = intent.getStringExtra("CONTENT");
            sendMessage(content);
        }

        startForeground(0, null);
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("2359", "Service Start "+address);
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            mMainHandler = new Handler(Looper.getMainLooper());
            if (!mBluetoothAdapter.isEnabled()) {

            } else if (mChatService == null) {
                setupChat();
            }
            binder = new BluetoothBinder() {
                @Override
                public void startBluetoothService(Intent data, boolean secure) {
                    super.startBluetoothService(data, secure);
                }

                @Override
                public void sendMessage(String message) {
                    super.sendMessage(message);
                }

                @Override
                public void receiveMessage(String message) {
                    super.receiveMessage(message);
                }
            };
    }

    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(getBaseContext(), R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mChatService.write(send);
        }
    }

    private void setupChat() {
       Log.d("2359", "setupChat");

        // Initialize the BluetoothChatService to perform bluetooth connections
        mChatService = new BluetoothChatService(this, mHandler);

        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
                // Start the Bluetooth chat services
                mChatService.start();
            }
        }

    }

    private void connectDevice(String address, boolean secure) {
        Log.d("2359", "Connect in Service");
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        mChatService.connect(device, secure);
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothChatService.STATE_CONNECTED:
                            break;
                        case BluetoothChatService.STATE_CONNECTING:
                            break;
                        case BluetoothChatService.STATE_LISTEN:
                        case BluetoothChatService.STATE_NONE:
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    if (readMessage.equals("home")) {
                        Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.oculus.home");
                        if (launchIntent != null) {
                            launchIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            startActivity(launchIntent);//null pointer check in case package name was not found
                        }
                    }
                    if (readMessage.equals("image")) {
                        startVrGallery2(0, "/storage/emulated/0/DCIM/Gear 360/SAM_100_0016.jpg", 0);
                    }
                    if (readMessage.equals("movie")) {
//                        startVrGallery2(getActivity(), 0, "/storage/emulated/0/DCIM/Gear 360/SAM_100_0016.jpg", 0);
//                        startVrGallery2(getActivity(), 0, "/storage/emulated/0/DCIM/Gear 360/SAM_100_0019.mp4", 1);

                        Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.samsung.vrvideo");
                        if (launchIntent != null) {
                            launchIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
//                            Uri uri = Uri.parse("samsungvr://sideload/?url=file:///storage/emulated/0/DCIM/Gear 360/SAM_100_0025.mp4&audio_type=other&video_type=_v360&title=Video Title");
                            Uri uri = Uri.parse("samsungvr://sideload/?url=file:///storage/emulated/0/DCIM/Gear 360/SAM_100_0026.mp4");
                            launchIntent.setData(uri);
                            startActivity(launchIntent);//null pointer check in case package name was not found
                        }

                    }
                    if (readMessage.equals("toast")) {
                        //Not work
//                        Toast.makeText(getActivity(), "This is a text", Toast.LENGTH_LONG).show();

                        //Not work
//                        showToast(getActivity(), "Toast Message", Toast.LENGTH_LONG);
                    }
                    if (readMessage.equals("jurassic")) {
                        Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.felixandpaul.jurassicworld.apatosaurus");
                        if (launchIntent != null) {
                            launchIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(launchIntent);//null pointer check in case package name was not found
                        }
                    }
                    if (readMessage.equals("roller")) {
                        Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.azamoka.PyramidsRollercoasterGearVr");
                        if (launchIntent != null) {
                            launchIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(launchIntent);//null pointer check in case package name was not found
                        }
                    }
                    if (readMessage.equals("rilix")) {
                        Intent launchIntent = getPackageManager().getLaunchIntentForPackage("br.com.rilix.rilixcoaster.gearvr");
                        if (launchIntent != null) {
                            launchIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(launchIntent);//null pointer check in case package name was not found
                        }
                    }
                    if (readMessage.equals("whatsapp")) {
                        Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.whatsapp");
                        if (launchIntent != null) {
                            launchIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(launchIntent);//null pointer check in case package name was not found
                        }
                    }
                    if (readMessage.equals("message")) {
                        Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.samsung.android.messaging");
                        if (launchIntent != null) {
                            launchIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(launchIntent);//null pointer check in case package name was not found
                        }
                    }
                    if (readMessage.equals("instagram")) {
                        Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.instagram.android");
                        if (launchIntent != null) {
                            launchIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(launchIntent);//null pointer check in case package name was not found
                        }
                    }

                    if (readMessage.equals("notify")) {
                        Log.d("debug","notify done");
                        NotificationActivity.scheduleNotification(getApplicationContext(),10,0);
                    }

                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    if (null != getBaseContext()) {
                        Toast.makeText(getBaseContext(), "Connected to "
                                + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case Constants.MESSAGE_TOAST:
                    if (null != getBaseContext()) {
                        Toast.makeText(getBaseContext(), msg.getData().getString(Constants.TOAST),
                                Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };

    public static final int VR_VIEWTYPE_ALBUM = 0;
    public static final int VR_VIEWTYPE_DETAIL = 2;
    public static final int VR_VIEWTYPE_LIST = 1;

    private static final String VR_GALLERY2_ACTIVITY_NAME = "com.samsung.android.app.vr.gallery2.MainActivity";
    private static final String VR_GALLERY2_PKG_NAME = "com.samsung.android.app.vr.gallery2";

    public void startVrGallery2(int type, String path, int currentSeek) {
        PackageInfo svrInstalled;
        String SVR_PACKAGE_NAME_LOCAL = VR_GALLERY2_PKG_NAME;
        String SVR_ACTIVITY_NAME = VR_GALLERY2_ACTIVITY_NAME;
        try {
            svrInstalled = getPackageManager().getPackageInfo(SVR_PACKAGE_NAME_LOCAL, VR_VIEWTYPE_ALBUM);
        } catch (PackageManager.NameNotFoundException e) {
            svrInstalled = null;
        }
        if (svrInstalled != null) {
            Uri uri = Uri.parse("file://" + path);
            Intent intent = new Intent();
            intent.setClassName(SVR_PACKAGE_NAME_LOCAL, SVR_ACTIVITY_NAME);
            intent.setData(uri);
            intent.putExtra("viewmode", type);
//            intent.putExtra("wait_title", "Wait Title");
//            intent.putExtra("wait_message", "Wait Message");
            if (currentSeek > 0) {
                intent.putExtra("seek", currentSeek);
            }
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            return;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("2359", "Service Destroy");
    }
}
