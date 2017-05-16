/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.bluetoothchat;

import android.Manifest;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.common.logger.Log;
import com.example.android.service.BluetoothService;

import java.io.File;
import java.lang.reflect.Method;

/**
 * This fragment controls Bluetooth to communicate with other devices.
 */
public class BluetoothChatFragment extends Fragment {

    private static final String TAG = "BluetoothChatFragment";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

    // Layout Views
    private ListView mConversationView;
    private EditText mOutEditText;
    private Button mSendButton;

    /**
     * Name of the connected device
     */
    private String mConnectedDeviceName = null;

    /**
     * Array adapter for the conversation thread
     */
    private ArrayAdapter<String> mConversationArrayAdapter;

    /**
     * String buffer for outgoing messages
     */
    private StringBuffer mOutStringBuffer;

    /**
     * Local Bluetooth adapter
     */
    private BluetoothAdapter mBluetoothAdapter = null;

    /**
     * Member object for the chat services
     */
    private BluetoothChatService mChatService = null;

    private Handler mMainHandler;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            FragmentActivity activity = getActivity();
            Toast.makeText(activity, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            activity.finish();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermission();
        }

        mMainHandler = new Handler(Looper.getMainLooper());
    }

    private boolean requestPermission() {
        String[] permissions = { Manifest.permission.ACCESS_COARSE_LOCATION,  Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
        boolean isGrantedBluetooth = isGrantedPermission(getActivity(), permissions);

        if (!isGrantedBluetooth) {
            requestPermission(getActivity(), permissions, 111);
            return false;
        }

        return true;
    }

    boolean isGrantedPermission (Activity activity, String... permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(activity,
                    permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }

        return true;
    }

    public static void requestPermission(Activity activity, String permission, int requestCode) {
        ActivityCompat.requestPermissions(activity,
                new String[]{permission},
                requestCode);
    }

    void requestPermission(Activity activity, String[] permissions, int requestCode) {
        ActivityCompat.requestPermissions(activity,
                permissions,
                requestCode);
    }


    @Override
    public void onStart() {
        super.onStart();
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else if (mChatService == null) {
            setupChat();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mChatService != null) {
            mChatService.stop();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
                // Start the Bluetooth chat services
                mChatService.start();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bluetooth_chat, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mConversationView = (ListView) view.findViewById(R.id.in);
        mOutEditText = (EditText) view.findViewById(R.id.edit_text_out);
        mSendButton = (Button) view.findViewById(R.id.button_send);
    }

    /**
     * Set up the UI and background operations for chat.
     */
    private void setupChat() {
        Log.d(TAG, "setupChat()");

        // Initialize the array adapter for the conversation thread
        mConversationArrayAdapter = new ArrayAdapter<String>(getActivity(), R.layout.message);

        mConversationView.setAdapter(mConversationArrayAdapter);

        // Initialize the compose field with a listener for the return key
        mOutEditText.setOnEditorActionListener(mWriteListener);

        // Initialize the send button with a listener that for click events
        mSendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                View view = getView();
                if (null != view) {
                    TextView textView = (TextView) view.findViewById(R.id.edit_text_out);
                    String message = textView.getText().toString();
                    sendMessage(message);
                }
            }
        });

        // Initialize the BluetoothChatService to perform bluetooth connections
        mChatService = new BluetoothChatService(getActivity(), mHandler);

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");
    }

    /**
     * Makes this device discoverable for 300 seconds (5 minutes).
     */
    private void ensureDiscoverable() {
        if (mBluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    /**
     * Sends a message.
     *
     * @param message A string of text to send.
     */
    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(getActivity(), R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mChatService.write(send);

            // Reset out string buffer to zero and clear the edit text field
            mOutStringBuffer.setLength(0);
            mOutEditText.setText(mOutStringBuffer);
        }
    }

    /**
     * The action listener for the EditText widget, to listen for the return key
     */
    private TextView.OnEditorActionListener mWriteListener
            = new TextView.OnEditorActionListener() {
        public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
            // If the action is a key-up event on the return key, send the message
            if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP) {
                String message = view.getText().toString();
                sendMessage(message);
            }
            return true;
        }
    };

    /**
     * Updates the status on the action bar.
     *
     * @param resId a string resource ID
     */
    private void setStatus(int resId) {
        FragmentActivity activity = getActivity();
        if (null == activity) {
            return;
        }
        final ActionBar actionBar = activity.getActionBar();
        if (null == actionBar) {
            return;
        }
        actionBar.setSubtitle(resId);
    }

    /**
     * Updates the status on the action bar.
     *
     * @param subTitle status
     */
    private void setStatus(CharSequence subTitle) {
        FragmentActivity activity = getActivity();
        if (null == activity) {
            return;
        }
        final ActionBar actionBar = activity.getActionBar();
        if (null == actionBar) {
            return;
        }
        actionBar.setSubtitle(subTitle);
    }

    /**
     * The Handler that gets information back from the BluetoothChatService
     */
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            FragmentActivity activity = getActivity();
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothChatService.STATE_CONNECTED:
                            setStatus(getString(R.string.title_connected_to, mConnectedDeviceName));
                            mConversationArrayAdapter.clear();
                            break;
                        case BluetoothChatService.STATE_CONNECTING:
                            setStatus(R.string.title_connecting);
                            break;
                        case BluetoothChatService.STATE_LISTEN:
                        case BluetoothChatService.STATE_NONE:
                            setStatus(R.string.title_not_connected);
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    mConversationArrayAdapter.add("Me:  " + writeMessage);
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    mConversationArrayAdapter.add(mConnectedDeviceName + ":  " + readMessage);

                    if (readMessage.equals("home")) {
                        Intent launchIntent = getActivity().getPackageManager().getLaunchIntentForPackage("com.oculus.home");
                        if (launchIntent != null) {
                            launchIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            startActivity(launchIntent);//null pointer check in case package name was not found
                        }
                    }
                    if (readMessage.equals("image")) {
                        startVrGallery2(getActivity(), 0, "/storage/emulated/0/DCIM/Gear 360/SAM_100_0016.jpg", 0);
                    }
                    if (readMessage.equals("movie")) {
//                        startVrGallery2(getActivity(), 0, "/storage/emulated/0/DCIM/Gear 360/SAM_100_0016.jpg", 0);
//                        startVrGallery2(getActivity(), 0, "/storage/emulated/0/DCIM/Gear 360/SAM_100_0019.mp4", 1);

                        Intent launchIntent = getActivity().getPackageManager().getLaunchIntentForPackage("com.samsung.vrvideo");
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
                        Intent launchIntent = getActivity().getPackageManager().getLaunchIntentForPackage("com.felixandpaul.jurassicworld.apatosaurus");
                        if (launchIntent != null) {
                            launchIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(launchIntent);//null pointer check in case package name was not found
                        }
                    }
                    if (readMessage.equals("roller")) {
                        Intent launchIntent = getActivity().getPackageManager().getLaunchIntentForPackage("com.azamoka.PyramidsRollercoasterGearVr");
                        if (launchIntent != null) {
                            launchIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(launchIntent);//null pointer check in case package name was not found
                        }
                    }
                    if (readMessage.equals("rilix")) {
                        Intent launchIntent = getActivity().getPackageManager().getLaunchIntentForPackage("br.com.rilix.rilixcoaster.gearvr");
                        if (launchIntent != null) {
                            launchIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(launchIntent);//null pointer check in case package name was not found
                        }
                    }
                    if (readMessage.equals("whatsapp")) {
                        Intent launchIntent = getActivity().getPackageManager().getLaunchIntentForPackage("com.whatsapp");
                        if (launchIntent != null) {
                            launchIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(launchIntent);//null pointer check in case package name was not found
                        }
                    }
                    if (readMessage.equals("message")) {
                        Intent launchIntent = getActivity().getPackageManager().getLaunchIntentForPackage("com.samsung.android.messaging");
                        if (launchIntent != null) {
                            launchIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(launchIntent);//null pointer check in case package name was not found
                        }
                    }
                    if (readMessage.equals("instagram")) {
                        Intent launchIntent = getActivity().getPackageManager().getLaunchIntentForPackage("com.instagram.android");
                        if (launchIntent != null) {
                            launchIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(launchIntent);//null pointer check in case package name was not found
                        }
                    }


                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    if (null != activity) {
                        Toast.makeText(activity, "Connected to "
                                + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case Constants.MESSAGE_TOAST:
                    if (null != activity) {
                        Toast.makeText(activity, msg.getData().getString(Constants.TOAST),
                                Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_SECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, true);
                }
                break;
            case REQUEST_CONNECT_DEVICE_INSECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, false);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    setupChat();
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(getActivity(), R.string.bt_not_enabled_leaving,
                            Toast.LENGTH_SHORT).show();
                    getActivity().finish();
                }
        }
    }

    public static final int VR_VIEWTYPE_ALBUM = 0;
    public static final int VR_VIEWTYPE_DETAIL = 2;
    public static final int VR_VIEWTYPE_LIST = 1;

    private static final String VR_GALLERY2_ACTIVITY_NAME = "com.samsung.android.app.vr.gallery2.MainActivity";
    private static final String VR_GALLERY2_PKG_NAME = "com.samsung.android.app.vr.gallery2";

    public static void startVrGallery2(Context context, int type, String path, int currentSeek) {
        PackageInfo svrInstalled;
        String SVR_PACKAGE_NAME_LOCAL = VR_GALLERY2_PKG_NAME;
        String SVR_ACTIVITY_NAME = VR_GALLERY2_ACTIVITY_NAME;
        try {
            svrInstalled = context.getPackageManager().getPackageInfo(SVR_PACKAGE_NAME_LOCAL, VR_VIEWTYPE_ALBUM);
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
            context.startActivity(intent);
            ((Activity) context).overridePendingTransition(VR_VIEWTYPE_ALBUM, VR_VIEWTYPE_ALBUM);
            return;
        }
    }

    private static final String VR_VIDEO_ACTIVITY_NAME = "com.samsung.android.app.vr.video.MainActivity";
    private static final String VR_VIDEO_PKG_NAME = "com.samsung.android.app.vr.video";
//    private static final String VR_VIDEO_PKG_NAME = "com.samsung.vrvideo";

    private static void gearVRforVideofunctionality(Context context, String mVRfilePath) {
        PackageInfo svrInstalled;
        String SVR_PACKAGE_NAME_LOCAL = VR_VIDEO_PKG_NAME;
        String SVR_ACTIVITY_NAME = VR_VIDEO_ACTIVITY_NAME;
        try {
            svrInstalled = context.getPackageManager().getPackageInfo(SVR_PACKAGE_NAME_LOCAL, VR_VIEWTYPE_ALBUM);
        } catch (PackageManager.NameNotFoundException e) {
            svrInstalled = null;
        }
        if (svrInstalled != null) {
            Uri uri = Uri.parse("file://" + mVRfilePath);
            Intent intent = new Intent();
            intent.setClassName(SVR_PACKAGE_NAME_LOCAL, SVR_ACTIVITY_NAME);
            intent.setData(uri);
//            intent.putExtra("wait_title", context.getString(C0804R.string.SS_INSERT_DEVICE_INTO_GEAR_VR_HEADER_ABB));
//            intent.putExtra("wait_message", context.getString(C0804R.string.f148xfc67daff));
//            intent.setFlags(268500992);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            ((Activity) context).overridePendingTransition(VR_VIEWTYPE_ALBUM, VR_VIEWTYPE_ALBUM);
            return;
        }
    }

    Toast mToast;

    //For Toast
    private class ToastRunable implements Runnable {
        final /* synthetic */ Context val$context;
        final /* synthetic */ int val$duration;
        final /* synthetic */ String val$str;

        ToastRunable(Context context, String str, int i) {
            this.val$context = context;
            this.val$str = str;
            this.val$duration = i;
        }

        public void run() {
            if (mToast != null) {
                mToast.cancel();
            }
            mToast = Toast.makeText(this.val$context, this.val$str, this.val$duration);
            mToast.show();
        }
    }

    private void showToast(Context context, String str, int duration) {
        Runnable action = new ToastRunable(context, str, duration);
        if (Looper.myLooper() == Looper.getMainLooper()) {
            action.run();
        } else {
            mMainHandler.post(action);
        }
    }

    //

    private Boolean connect(BluetoothDevice bdDevice) {
        Boolean bool = false;
        try {
            Log.i("Log", "service method is called ");
            Class cl = Class.forName("android.bluetooth.BluetoothDevice");
            Class[] par = {};
            Method method = cl.getMethod("createBond", par);
            Object[] args = {};
            bool = (Boolean) method.invoke(bdDevice);//, args);// this invoke creates the detected devices paired.
            //Log.i("Log", "This is: "+bool.booleanValue());
            //Log.i("Log", "devicesss: "+bdDevice.getName());
        } catch (Exception e) {
            Log.i("Log", "Inside catch of serviceFromDevice Method");
            e.printStackTrace();
        }
        return bool.booleanValue();
    };


    public boolean removeBond(BluetoothDevice btDevice)
            throws Exception
    {
        Class btClass = Class.forName("android.bluetooth.BluetoothDevice");
        Method removeBondMethod = btClass.getMethod("removeBond");
        Boolean returnValue = (Boolean) removeBondMethod.invoke(btDevice);
        return returnValue.booleanValue();
    }


    public boolean createBond(BluetoothDevice btDevice)
            throws Exception
    {
        Class class1 = Class.forName("android.bluetooth.BluetoothDevice");
        Method createBondMethod = class1.getMethod("createBond");
        Boolean returnValue = (Boolean) createBondMethod.invoke(btDevice);
        return returnValue.booleanValue();
    }







    /**
     * Establish connection with other device
     *
     * @param data   An {@link Intent} with {@link DeviceListActivity#EXTRA_DEVICE_ADDRESS} extra.
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    private void connectDevice(Intent data, boolean secure) {
        // Get the device MAC address
        String address = data.getExtras()
                .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);

        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mChatService.connect(device, secure);



    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.bluetooth_chat, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.secure_connect_scan: {
                // Launch the DeviceListActivity to see devices and do scan
                Intent serverIntent = new Intent(getActivity(), DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
                return true;
            }
            case R.id.insecure_connect_scan: {
                // Launch the DeviceListActivity to see devices and do scan
                Intent serverIntent = new Intent(getActivity(), DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_INSECURE);
                return true;
            }
            case R.id.discoverable: {
                // Ensure this device is discoverable by others
                ensureDiscoverable();
                return true;
            }
        }
        return false;
    }

}
