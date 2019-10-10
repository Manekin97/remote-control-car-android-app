package com.example.iot_car_rc;

import android.os.AsyncTask;
import android.util.Log;

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

    private class SendCommandTask extends AsyncTask<COMMAND, Void, Void> {

        @Override
        protected Void doInBackground(COMMAND... commands) {
            ByteBuffer message = ByteBuffer.allocate(1);
            message.put(commands[0].getCommandCode()).rewind();
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

    public void setInetAddress(String address) {
        try {
            this.inetAddress = InetAddress.getByName(address);
        } catch (UnknownHostException exception) {
            Log.e(exception.getMessage(), "TRANSMITTER_ERROR:setInetAddress");
        }
    }

    void sendCommand(COMMAND command) {
        new SendCommandTask().execute(command);
    }
}
