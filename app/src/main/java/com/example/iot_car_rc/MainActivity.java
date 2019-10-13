package com.example.iot_car_rc;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;


public class MainActivity extends AppCompatActivity implements View.OnTouchListener {
    private CommandTransmitter commandTransmitter;

    private WifiManager wifiManager;
    private WifiConfiguration wifiConfiguration;
    private final String SSID = "ESP8266-ACCESS-POINT";
    private final String IP_ADDRESS = "192.168.1.1";
    private WiFiStateListener wiFiStateListener;
    private IntentFilter filters;

    private ImageButton forwardButton;
    private ImageButton backwardsButton;
    private ImageButton steerLeftButton;
    private ImageButton steerRightButton;
    private Switch controlModeSwitch;

    ProgressDialog dialog;

    private class WaitForWiFiTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                connectToWifi();
            } catch (Exception e) {
                Log.e(e.getMessage(), "WIFI_ERROR");
            }

            return null;
        }

        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            dialog.dismiss();
        }
    }

    public class WiFiStateListener extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            try {
                if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                    NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                    NetworkInfo.DetailedState state = info.getDetailedState();
                    Log.d(state.toString(), "STATE");

                    switch (state) {
                        case SCANNING:
                            dialog.setMessage(getString(R.string.scanning_wifi));
                            break;
                        case CONNECTING:
                            dialog.setMessage(getString(R.string.connecting_to_wifi));
                            break;
                        case OBTAINING_IPADDR:
                            dialog.setMessage(getString(R.string.obtaining_wifi_ip_address));
                            break;
                        case DISCONNECTED:
                            new WaitForWiFiTask().execute();
                            break;
                    }
                }
            } catch (NullPointerException exception) {
                Log.e(exception.getMessage(), "WIFI_ERROR:WiFiStateListener");
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        this.getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );

        this.filters = new IntentFilter();
        this.filters.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        this.filters.addAction("android.net.wifi.STATE_CHANGE");

        this.wiFiStateListener = new WiFiStateListener();
        super.registerReceiver(wiFiStateListener, filters);

        this.dialog = new ProgressDialog(this);
        this.dialog.setMessage(getString(R.string.connecting_to_wifi));
        this.dialog.setCancelable(false);
        this.dialog.setInverseBackgroundForced(false);

        this.wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        this.wifiConfiguration = new WifiConfiguration();
        this.wifiConfiguration.SSID = String.format("\"%s\"", SSID);
        this.wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);

        new WaitForWiFiTask().execute();

        this.commandTransmitter = CommandTransmitter.getInstance();
        this.commandTransmitter.setInetAddress(this.IP_ADDRESS);

        forwardButton = (ImageButton) findViewById(R.id.move_forward_button);
        backwardsButton = (ImageButton) findViewById(R.id.move_backwards_button);
        steerLeftButton = (ImageButton) findViewById(R.id.steer_left_button);
        steerRightButton = (ImageButton) findViewById(R.id.steer_right_button);
        controlModeSwitch = (Switch) findViewById(R.id.control_mode_switch);

        forwardButton.setOnTouchListener(this);
        backwardsButton.setOnTouchListener(this);
        steerLeftButton.setOnTouchListener(this);
        steerRightButton.setOnTouchListener(this);

        controlModeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    controlModeSwitch.setText(R.string.control_mode_switch_text_off);
                    commandTransmitter.sendCommand(COMMAND.SET_AUTONOMOUS_CONTROL_MODE);
                    disableSteeringButtons();
                } else {
                    controlModeSwitch.setText(R.string.control_mode_switch_text_on);
                    commandTransmitter.sendCommand(COMMAND.SET_REMOTE_CONTROL_MODE);
                    enableSteeringButtons();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        super.registerReceiver(wiFiStateListener, this.filters);
    }

    @Override
    protected void onPause() {
        super.onPause();

        dialog.dismiss();
        unregisterReceiver(this.wiFiStateListener);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            commandTransmitter.sendCommand(COMMAND.STOP);

            return false;
        }

        switch (v.getId()) {
            case R.id.move_forward_button:
                commandTransmitter.sendCommand(COMMAND.FORWARD);
                break;
            case R.id.move_backwards_button:
                commandTransmitter.sendCommand(COMMAND.BACKWARD);
                break;
            case R.id.steer_left_button:
                commandTransmitter.sendCommand(COMMAND.LEFT);
                break;
            case R.id.steer_right_button:
                commandTransmitter.sendCommand(COMMAND.RIGHT);
                break;
        }

        return false;
    }

    private void disableSteeringButtons() {
        forwardButton.setEnabled(false);
        backwardsButton.setEnabled(false);
        steerLeftButton.setEnabled(false);
        steerRightButton.setEnabled(false);
    }

    private void enableSteeringButtons() {
        forwardButton.setEnabled(true);
        backwardsButton.setEnabled(true);
        steerLeftButton.setEnabled(true);
        steerRightButton.setEnabled(true);
    }

    private void connectToWifi() {
        try {
            while (!wifiManager.isWifiEnabled()) ;

            int networkID = wifiManager.addNetwork(wifiConfiguration);

            wifiManager.enableNetwork(networkID, true);
            wifiManager.reconnect();

            while (wifiManager.getConnectionInfo().getNetworkId() == -1) ;
        } catch (Exception e) {
            Log.e(e.getMessage(), "WIFI_ERROR");
        }
    }
}
