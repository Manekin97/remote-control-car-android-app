package com.example.iot_car_rc;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

class CommandTransmitter {
    private static final CommandTransmitter INSTANCE = null;

    private final int PORT = 4210;

    private DatagramSocket socket;
    private InetAddress inetAddress;

    private class SendCommandTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {
            ByteBuffer message = ByteBuffer.allocate(strings[0].getBytes().length);
            message.put(strings[0].getBytes()).rewind();
            DatagramPacket packet = new DatagramPacket(message.array(), message.limit(), inetAddress, PORT);

            try {
                socket.send(packet);
            } catch (IOException exception) {
                Log.e(exception.getMessage(), "TRANSMITTER_ERROR:SendCommandTask");
            }

            return null;
        }
    }

    static CommandTransmitter getInstance() {
        return INSTANCE == null ? new CommandTransmitter() : INSTANCE;
    }

    private CommandTransmitter() {
        try {
            socket = new DatagramSocket();
        } catch (SocketException exception) {
            Log.e(exception.getMessage(), "TRANSMITTER_ERROR:CommandTransmitter");
        }
    }

    void setInetAddress(String address) {
        try {
            this.inetAddress = InetAddress.getByName(address);
        } catch (UnknownHostException exception) {
            Log.e(exception.getMessage(), "TRANSMITTER_ERROR:setInetAddress");
        }
    }

    void sendCommand(int leftMotorSpeed, int rightMotorSpeed, int direction, int drivingMode) {
        String jsonString = preparePacket(leftMotorSpeed, rightMotorSpeed, direction, drivingMode);
        new SendCommandTask().execute(jsonString);
    }

    private String preparePacket(int leftMotorSpeed, int rightMotorSpeed, int direction, int drivingMode) {
        JSONObject json = new JSONObject();

        try {
            json.put("left_motor_speed", leftMotorSpeed);
            json.put("right_motor_speed", rightMotorSpeed);
            json.put("direction", direction);
            json.put("driving_mode", drivingMode);
        } catch (JSONException exception) {
            Log.e(exception.getMessage(), "TRANSMITTER_ERROR:preparePacket");
        }

        return json.toString();

    }
}
