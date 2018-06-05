package com.example.umathur.androidwebservertest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;

import com.example.umathur.androidwebservertest.CustomAsyncTask.AsyncHandler;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class MainActivity extends AppCompatActivity {

    private static final int DEFAULT_PORT = 8080;

    private AndroidWebServer androidWebServer;
    private BroadcastReceiver broadcastReceiverNetworkState;
    private CustomAsyncTask fetchIpAddressTask;

    private TextView txtIpAddress;
    private EditText txtPortNum;
    private SwitchCompat serverSwitchBtn;

    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        txtIpAddress = findViewById(R.id.txtIpAddress);
        txtPortNum = findViewById(R.id.txtPortNum);
        serverSwitchBtn = findViewById(R.id.switchServerToggle);
        txtPortNum.setText("" + DEFAULT_PORT);
        registerNetworkChangeListener();
        serverSwitchBtn.setOnCheckedChangeListener(getSwitchChangeListener());
    }

    private void registerNetworkChangeListener() {
        broadcastReceiverNetworkState = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                fetchIpAddressTask = new CustomAsyncTask(getIpFetchAsyncHandler());
                fetchIpAddressTask.execute();
            }
        };
        final IntentFilter filters = new IntentFilter();
        filters.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        filters.addAction("android.net.wifi.STATE_CHANGE");
        registerReceiver(broadcastReceiverNetworkState, filters);
    }

    private OnCheckedChangeListener getSwitchChangeListener() {
        return new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (Util.isConnectedToWifi(context)) {
                    if (isChecked) {
                        startAndroidWebServer();
                        txtPortNum.setEnabled(false);
                    } else {
                        stopAndroidWebServer();
                        txtPortNum.setEnabled(true);
                    }
                } else {
                    Util.showToast(context, "You are not connected to a WiFi network !!! ");
                }
            }
        };
    }

    private boolean startAndroidWebServer() {
        int port = getPortFromEditText();
        try {
            if (port < 1024) {
                throw new Exception();
            }
            androidWebServer = new AndroidWebServer(port);
            androidWebServer.start();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            Util.showToast(context, "The PORT " + port + " doesn't work, please change it between 1024 and 65535.");
        }
        return false;
    }

    private boolean stopAndroidWebServer() {
        if (androidWebServer != null) {
            androidWebServer.stop();
            return true;
        }
        return false;
    }

    private AsyncHandler getIpFetchAsyncHandler() {
        return new AsyncHandler() {
            @Override
            public String doInBackgroundHandler() {
                return getDeviceIpAddress();
            }

            @Override
            public void onPostExecHandler() {
                txtIpAddress.setText("http://" + getDeviceIpAddress() + ":");
            }
        };
    }

    private static String getDeviceIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface nwIterface = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = nwIterface.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private int getPortFromEditText() {
        String valueEditText = txtPortNum.getText().toString().trim();
        return (valueEditText.length() > 0) ? Integer.parseInt(valueEditText) : DEFAULT_PORT;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAndroidWebServer();
        if (broadcastReceiverNetworkState != null) {
            unregisterReceiver(broadcastReceiverNetworkState);
        }
    }

}
