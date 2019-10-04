package com.example.iot_car_rc;

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

    static CommandTransmitter getInstance() {
        return INSTANCE == null ? new CommandTransmitter() : INSTANCE;
    }

    private CommandTransmitter() {
        try {
            socket = new DatagramSocket();
        } catch (SocketException exception) {
            Log.e(exception.getMessage(), "TRANSMITTER_ERROR");
        }
    }

    public void setInetAddress(String address) {
        try {
            this.inetAddress = InetAddress.getByName(address);
        } catch (UnknownHostException exception) {
            Log.e(exception.getMessage(), "TRANSMITTER_ERROR");
        }
    }

    void sendCommand(COMMAND command) {
        ByteBuffer message = ByteBuffer.allocate(1);
        message.put(command.getCommandCode()).rewind();
        DatagramPacket packet = new DatagramPacket(message.array(), message.limit(), this.inetAddress, PORT);

        try {
            socket.send(packet);
        } catch (IOException exception) {
            Log.e(exception.getMessage(), "TRANSMITTER_ERROR");
        }
    }
}
