package com.example.iot_car_rc;

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
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Switch;

import org.json.JSONException;

import java.lang.ref.WeakReference;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Represents MainActivity of the application.
 *
 * @author Rafał Dąbrowski
 */
public class MainActivity extends AppCompatActivity implements JoystickView.JoystickListener {
    private final String SSID = "ESP8266-ACCESS-POINT";
    private final String IP_ADDRESS = "192.168.1.1";

    private WifiManager wifiManager;
    private WifiConfiguration wifiConfiguration;
    private WiFiStateListener wiFiStateListener;
    private IntentFilter filters;

    private Switch controlModeSwitch;
    private Spinner algorithmSpinner;
    private JoystickView joystickView;
    private ProgressDialog wifiConnectionProgressDialog;

    private CommandTransmitter commandTransmitter;
    private DrivingDirection drivingDirection = DrivingDirection.FORWARD;
    private DrivingMode drivingMode = DrivingMode.REMOTE;
    private DrivingAlgorithm drivingAlgorithm = DrivingAlgorithm.SIMPLE;
    private int leftMotorSpeed;
    private int rightMotorSpeed;

    /**
     * Represents an AsyncTask used to connect to Wi-Fi.
     *
     * @author Rafał Dąbrowski
     */
    private static class WaitForWiFiTask extends AsyncTask<Void, Void, Void> {
        //  Needed to access activity properties
        //  and prevent memory leakage since the class is static
        private WeakReference<MainActivity> mainActivityReference;

        /**
         * A constructor for WaitForWiFiTask
         *
         * @param mainActivity a main activity context
         * @return SendCommandTask instance.
         */
        WaitForWiFiTask(MainActivity mainActivity) {
            this.mainActivityReference = new WeakReference<>(mainActivity);
        }

        /**
         * This method is called before executing the task. It shows the Wi-Fi connection dialog.
         *
         * @return Nothing.
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            MainActivity mainActivity = this.mainActivityReference.get();
            mainActivity.wifiConnectionProgressDialog.show();
        }

        /**
         * This method tries to connect to Wi-Fi.
         *
         * @return Nothing.
         */
        @Override
        protected Void doInBackground(Void... voids) {
            MainActivity mainActivity = this.mainActivityReference.get();
            mainActivity.connectToWifi();

            return null;
        }

        /**
         * This method is called after executing the task. It dismisses the Wi-Fi connection dialog.
         *
         * @return Nothing.
         */
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            MainActivity mainActivity = this.mainActivityReference.get();
            mainActivity.wifiConnectionProgressDialog.dismiss();
        }
    }

    /**
     * Represents a Wi-Fi state listener.
     * It listens for changes in Wi-Fi connections and handles them.
     *
     * @author Rafał Dąbrowski
     */
    public class WiFiStateListener extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action != null && action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
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
                        new WaitForWiFiTask((MainActivity) context).execute();
                        break;
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.joystickView = (JoystickView) findViewById(R.id.joystickView);

        //  Lock the screen orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);

        //  Set broadcast receiver filters
        this.filters = new IntentFilter();
        this.filters.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        this.filters.addAction("android.net.wifi.STATE_CHANGE");

        //  Register Wi-Fi listener
        this.wiFiStateListener = new WiFiStateListener();
        super.registerReceiver(wiFiStateListener, filters);

        //  Configure Wi-Fi connection progress dialog
        this.wifiConnectionProgressDialog = new ProgressDialog(this, R.style.progressDialogTheme);
        this.wifiConnectionProgressDialog.setMessage(getString(R.string.connecting_to_wifi));
        this.wifiConnectionProgressDialog.setCancelable(false);
        this.wifiConnectionProgressDialog.setInverseBackgroundForced(false);

        //  Configure Wi-Fi connection parameters
        this.wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        this.wifiConfiguration = new WifiConfiguration();
        this.wifiConfiguration.SSID = String.format("\"%s\"", SSID);
        this.wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);

        //  Try connecting to Wi-Fi
        new WaitForWiFiTask(this).execute();

        //  Get command transmitter instance and set IP Address
        try {
            this.commandTransmitter = CommandTransmitter.getInstance();
        } catch (SocketException exception) {
            Log.e(exception.getMessage(), "TRANSMITTER_ERROR:getInstance");
        }

        //  Set the IP Address
        try {
            this.commandTransmitter.setInetAddress(this.IP_ADDRESS);
        } catch (UnknownHostException exception) {
            Log.e(exception.getMessage(), "TRANSMITTER_ERROR:setInetAddress");
        }

        controlModeSwitch = (Switch) findViewById(R.id.control_mode_switch);
        algorithmSpinner = (Spinner) findViewById(R.id.algorithm_spinner);

        //  Create an adapter for spinner view
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.algorithms_entries,
                R.layout.spinner_item
        );
        //  Set the spinner view resource
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //  Set the adapter
        algorithmSpinner.setAdapter(adapter);

        //  Set up on item selected listener for algorithm spinner
        algorithmSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                drivingAlgorithm = stringToDrivingAlgorithm((String) parent.getSelectedItem());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                drivingAlgorithm = stringToDrivingAlgorithm((String) parent.getItemAtPosition(0));
            }
        });

        //  Set up on checked listener for control mode switch
        controlModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                controlModeSwitch.setText(R.string.control_mode_switch_text_off);
                drivingMode = DrivingMode.AUTONOMOUS;
                disableJoystick();
            } else {
                controlModeSwitch.setText(R.string.control_mode_switch_text_on);
                drivingMode = DrivingMode.REMOTE;
                enableJoystick();
            }

            try {
                commandTransmitter.sendCommand(
                        leftMotorSpeed,
                        rightMotorSpeed,
                        drivingDirection.getDrivingDirection(),
                        drivingMode.getDrivingMode(),
                        drivingAlgorithm.getDrivingAlgorithm()
                );
            } catch (JSONException exception) {
                Log.e(exception.getMessage(), "TRANSMITTER_ERROR:sendCommand");
            }
        });
    }

    /**
     * This is an indicator that the activity became active and ready to receive input.
     * It's called after onPause(). It registers the broadcast receiver.
     *
     * @return Nothing.
     */
    @Override
    protected void onResume() {
        super.onResume();

        super.registerReceiver(wiFiStateListener, this.filters);
    }

    /**
     * This method is called as part of the activity lifecycle when the user no longer
     * actively interacts with the activity, but it is still visible on screen.
     * It dismisses the Wi-Fi connection dialog and unregisters the broadcast receiver.
     *
     * @return Nothing.
     */
    @Override
    protected void onPause() {
        super.onPause();

        wifiConnectionProgressDialog.dismiss();
        unregisterReceiver(this.wiFiStateListener);
    }

    /**
     * This method handles joystick movement event.
     *
     * @param xOffset the offset of the joystick knob in the X axis, ranges between <0, 100>.
     * @param yOffset the offset of the joystick knob in the Y axis, ranges between <0, 100>.
     * @return Nothing.
     * @throws JSONException if parameters are of the wrong type
     * @see JSONException
     */
    @Override
    public void onJoystickMoved(int xOffset, int yOffset) throws JSONException {
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

        this.commandTransmitter.sendCommand(
                this.leftMotorSpeed,
                this.rightMotorSpeed,
                this.drivingDirection.getDrivingDirection(),
                this.drivingMode.getDrivingMode(),
                this.drivingAlgorithm.getDrivingAlgorithm()
        );
    }

    /**
     * This method connects to Wi-Fi.
     *
     * @return Nothing.
     */
    private void connectToWifi() {
        //  Wait until Wi-Fi is enabled
        while (!wifiManager.isWifiEnabled()) ;

        //  Get the network ID
        int networkID = wifiManager.addNetwork(wifiConfiguration);

        //  Try to connect
        wifiManager.enableNetwork(networkID, true);
        wifiManager.reconnect();

        //  Wait until connection is established
        while (wifiManager.getConnectionInfo().getNetworkId() == -1) ;
    }

    /**
     * This method maps the value between range.
     *
     * @param input       the value to be mapped.
     * @param inputStart  the minimum value of input.
     * @param inputEnd    the maximum value of input.
     * @param outputStart the minimum value of output.
     * @param outputEnd   the maximum value of output.
     * @return A mapped value between outputStart and outputEnd.
     */
    private int map(int input, int inputStart, int inputEnd, int outputStart, int outputEnd) {
        int inputRange = inputEnd - inputStart;
        int outputRange = outputEnd - outputStart;

        return (input - inputStart) * outputRange / inputRange + outputStart;
    }

    /**
     * This method constrains speed between 0 and maxSpeedValue
     *
     * @param speed         The value of speed.
     * @param maxSpeedValue The maximum value of speed.
     * @return Constrained value of speed.
     */
    private int constrainSpeedValue(int speed, int maxSpeedValue) {
        if (speed > maxSpeedValue) {
            return maxSpeedValue;
        }

        if (speed < 0) {
            return 0;
        }

        return speed;
    }

    /**
     * This method disables joystick control.
     *
     * @return Nothing.
     */
    private void disableJoystick() {
        this.joystickView.disableJoystick();
    }

    /**
     * This method enables joystick control.
     *
     * @return Nothing.
     */
    private void enableJoystick() {
        this.joystickView.enableJoystick();
    }

    private DrivingAlgorithm stringToDrivingAlgorithm(String string) {
        switch (string.toUpperCase()){
            case "SIMPLE ALGORITHM":
                return DrivingAlgorithm.SIMPLE;
            case "ADVANCED ALGORITHM":
                return DrivingAlgorithm.ADVANCED;
            case "COMPLEX ALGORITHM":
                return DrivingAlgorithm.COMPLEX;
            default:
                return null;
        }
    }
}
