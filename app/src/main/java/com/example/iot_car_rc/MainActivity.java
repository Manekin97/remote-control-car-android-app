package com.example.iot_car_rc;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;


public class MainActivity extends AppCompatActivity implements View.OnTouchListener {
    private String IP_ADDRESS;
    private CommandTransmitter commandTransmitter;
    private Button forwardButton;
    private Button backwardsButton;
    private Button steerLeftButton;
    private Button steerRightButton;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        this.IP_ADDRESS = getIntent().getStringExtra("IP_ADDRESS");

        this.commandTransmitter = CommandTransmitter.getInstance();
        this.commandTransmitter.setInetAddress(this.IP_ADDRESS);

        forwardButton = (Button) findViewById(R.id.move_forward_button);
        backwardsButton = (Button) findViewById(R.id.move_backwards_button);
        steerLeftButton = (Button) findViewById(R.id.steer_left_button);
        steerRightButton = (Button) findViewById(R.id.steer_right_button);

        forwardButton.setOnTouchListener(this);
        backwardsButton.setOnTouchListener(this);
        steerLeftButton.setOnTouchListener(this);
        steerRightButton.setOnTouchListener(this);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            commandTransmitter.sendCommand(COMMAND.STOP);
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

        return true;
    }
}
