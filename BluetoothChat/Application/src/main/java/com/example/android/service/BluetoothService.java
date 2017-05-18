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

    private String address;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            address = intent.getStringExtra("ADDRESS");
        }
        Log.d("2359", "Start Command "+address);
        if (address != null  && address.length() > 0) {
            connectDevice(address, true);
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
//            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                // Otherwise, setup the chat session
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

    private void setupChat() {
       Log.d("2359", "setupChat");

//        // Initialize the array adapter for the conversation thread
//        mConversationArrayAdapter = new ArrayAdapter<String>(this, R.layout.message);
//
//        mConversationView.setAdapter(mConversationArrayAdapter);
//
//        // Initialize the compose field with a listener for the return key
//        mOutEditText.setOnEditorActionListener(mWriteListener);
//
//        // Initialize the send button with a listener that for click events
//        mSendButton.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                // Send a message using content of the edit text widget
//                View view = getView();
//                if (null != view) {
//                    TextView textView = (TextView) view.findViewById(R.id.edit_text_out);
//                    String message = textView.getText().toString();
//                    sendMessage(message);
//                }
//            }
//        });

        // Initialize the BluetoothChatService to perform bluetooth connections
        mChatService = new BluetoothChatService(this, mHandler);

        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
                // Start the Bluetooth chat services
                mChatService.start();
            }
        }

//        // Initialize the buffer for outgoing messages
//        mOutStringBuffer = new StringBuffer("");
    }

    private void connectDevice(String address, boolean secure) {
        Log.d("2359", "Connect in Service");
        // Get the device MAC address
//        String address = data.getExtras()
//                .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mChatService.connect(device, secure);
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
//            FragmentActivity activity = getActivity();
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothChatService.STATE_CONNECTED:
//                            setStatus(getString(R.string.title_connected_to, mConnectedDeviceName));
//                            mConversationArrayAdapter.clear();
                            break;
                        case BluetoothChatService.STATE_CONNECTING:
//                            setStatus(R.string.title_connecting);
                            break;
                        case BluetoothChatService.STATE_LISTEN:
                        case BluetoothChatService.STATE_NONE:
//                            setStatus(R.string.title_not_connected);
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
//                    mConversationArrayAdapter.add("Me:  " + writeMessage);
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
//                    mConversationArrayAdapter.add(mConnectedDeviceName + ":  " + readMessage);

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
//            intent.setFlags(268500992);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
//             ((Activity) context).overridePendingTransition(VR_VIEWTYPE_ALBUM, VR_VIEWTYPE_ALBUM);
            return;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("2359", "Service Destroy");
    }
}
