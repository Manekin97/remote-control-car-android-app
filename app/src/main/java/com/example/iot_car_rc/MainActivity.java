package com.example.iot_car_rc;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Switch;


public class MainActivity extends AppCompatActivity implements JoystickView.JoystickListener {
    private CommandTransmitter commandTransmitter;
    private WifiManager wifiManager;
    private WifiConfiguration wifiConfiguration;
    private final String SSID = "ESP8266-ACCESS-POINT";
    private final String IP_ADDRESS = "192.168.1.1";
    private WiFiStateListener wiFiStateListener;
    private IntentFilter filters;
    private Switch controlModeSwitch;
    private JoystickView joystickView;
    private ProgressDialog wifiConnectionProgressDialog;
    private int leftMotorSpeed;
    private int rightMotorSpeed;
    private DrivingDirection drivingDirection;
    private DrivingMode drivingMode;

    private class WaitForWiFiTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            wifiConnectionProgressDialog.show();
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
            wifiConnectionProgressDialog.dismiss();
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

                    switch (state) {
                        case SCANNING:
                            wifiConnectionProgressDialog.setMessage(getString(R.string.scanning_wifi));
                            break;
                        case CONNECTING:
                            wifiConnectionProgressDialog.setMessage(getString(R.string.connecting_to_wifi));
                            break;
                        case OBTAINING_IPADDR:
                            wifiConnectionProgressDialog.setMessage(getString(R.string.obtaining_wifi_ip_address));
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.joystickView = (JoystickView) findViewById(R.id.joystickView);

        this.filters = new IntentFilter();
        this.filters.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        this.filters.addAction("android.net.wifi.STATE_CHANGE");

        this.wiFiStateListener = new WiFiStateListener();
        super.registerReceiver(wiFiStateListener, filters);

        this.wifiConnectionProgressDialog = new ProgressDialog(this);
        this.wifiConnectionProgressDialog.setMessage(getString(R.string.connecting_to_wifi));
        this.wifiConnectionProgressDialog.setCancelable(false);
        this.wifiConnectionProgressDialog.setInverseBackgroundForced(false);

        this.wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        this.wifiConfiguration = new WifiConfiguration();
        this.wifiConfiguration.SSID = String.format("\"%s\"", SSID);
        this.wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);

        new WaitForWiFiTask().execute();

        this.commandTransmitter = CommandTransmitter.getInstance();
        this.commandTransmitter.setInetAddress(this.IP_ADDRESS);

        controlModeSwitch = (Switch) findViewById(R.id.control_mode_switch);

        controlModeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    controlModeSwitch.setText(R.string.control_mode_switch_text_off);
                    drivingMode = DrivingMode.AUTONOMOUS;
                    commandTransmitter.sendCommand(leftMotorSpeed, rightMotorSpeed, drivingDirection.getDrivingDirection(), drivingMode.getDrivingMode());
                    disableJoystick();
                } else {
                    controlModeSwitch.setText(R.string.control_mode_switch_text_on);
                    drivingMode = DrivingMode.REMOTE;
                    commandTransmitter.sendCommand(leftMotorSpeed, rightMotorSpeed, drivingDirection.getDrivingDirection(), drivingMode.getDrivingMode());
                    enableJoystick();
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

        wifiConnectionProgressDialog.dismiss();
        unregisterReceiver(this.wiFiStateListener);
    }

    @Override
    public void onJoystickMoved(int xOffset, int yOffset) {
        int speed;
        int speedOffset;

        if (yOffset < 0) {
            speed = -map(-yOffset, 0, 100, 0, 255);
            drivingDirection = DrivingDirection.BACKWARD;
        } else {
            speed = map(yOffset, 0, 100, 0, 255);
            drivingDirection = DrivingDirection.FORWARD;
        }

        if (xOffset < 0) {
            speedOffset = map(-xOffset, 0, 100, 0, 255);

            this.rightMotorSpeed = constrainSpeedValue(Math.abs(speed) + speedOffset, 255);
            this.leftMotorSpeed = constrainSpeedValue(Math.abs(speed) - speedOffset, 255);
        } else {
            speedOffset = map(xOffset, 0, 100, 0, 255);

            this.leftMotorSpeed = constrainSpeedValue(Math.abs(speed) + speedOffset, 255);
            this.rightMotorSpeed = constrainSpeedValue(Math.abs(speed) - speedOffset, 255);
        }

        this.commandTransmitter.sendCommand(this.leftMotorSpeed, this.rightMotorSpeed, this.drivingDirection.getDrivingDirection(), 1);
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

    private int map(int input, int input_start, int input_end, int output_start, int output_end) {
        int input_range = input_end - input_start;
        int output_range = output_end - output_start;

        return (input - input_start) * output_range / input_range + output_start;
    }

    private int constrainSpeedValue(int speed, int maxSpeedValue) {
        if (speed > maxSpeedValue) {
            return maxSpeedValue;
        }

        if (speed < 0) {
            return 0;
        }

        return speed;
    }

    private void disableJoystick() {
        this.joystickView.disableJoystick();
    }

    private void enableJoystick() {
        this.joystickView.enableJoystick();
    }
}
