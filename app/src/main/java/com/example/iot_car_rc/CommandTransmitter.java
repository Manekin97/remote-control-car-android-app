package com.example.iot_car_rc;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

/**
 * Represents a Command Transmitter.
 *
 * @author Rafał Dąbrowski
 */
class CommandTransmitter {
    private static final CommandTransmitter INSTANCE = null;
    private final int PORT = 4210;
    private DatagramSocket socket;
    private InetAddress inetAddress;

    /**
     * Represents an AsyncTask used to send commands using UDP
     *
     * @author Rafał Dąbrowski
     */
    private static class SendCommandTask extends AsyncTask<String, Void, Void> {
        //  Needed to access CommandTransmitter properties
        //  and prevent memory leakage since the class is static
        private WeakReference<CommandTransmitter> commandTransmitterReference;

        /**
         * A constructor for SendCommandTask
         *
         * @param commandTransmitter a CommandTransmitter context
         * @return SendCommandTask instance.
         */
        SendCommandTask(CommandTransmitter commandTransmitter) {
            this.commandTransmitterReference = new WeakReference<>(commandTransmitter);
        }

        /**
         * This method sets the transmitter's IP Address.
         *
         * @param strings An array of strings containing JSON objects,
         *                only the first one is used.
         * @return Nothing.
         */
        @Override
        protected Void doInBackground(String... strings) {
            //  Get the command transmitter instance
            CommandTransmitter commandTransmitter = this.commandTransmitterReference.get();

            //  Allocate space for the message
            ByteBuffer message = ByteBuffer.allocate(strings[0].getBytes().length);
            //  Set the message and rewind bytes
            message.put(strings[0].getBytes()).rewind();
            //  Create the packet
            DatagramPacket packet = new DatagramPacket(
                    message.array(),                //  The message as byte array
                    message.limit(),                //  The message length
                    commandTransmitter.inetAddress, //  IP Address
                    commandTransmitter.PORT         //  Port
            );

            try {
                commandTransmitter.socket.send(packet);
            } catch (IOException exception) {
                Log.e(exception.getMessage(), "TRANSMITTER_ERROR:SendCommandTask");
            }

            return null;
        }
    }

    /**
     * This method returns the Command Transmitter instance
     *
     * @return Command Transmitter instance.
     * @throws SocketException if the socket could not be opened,
     *                         or the socket could not bind to the specified local port.
     * @see SocketException
     */
    static CommandTransmitter getInstance() throws SocketException {
        return INSTANCE == null ? new CommandTransmitter() : INSTANCE;
    }

    /**
     * A constructor for Command Transmitter
     *
     * @return Command Transmitter instance.
     * @throws SocketException if the socket could not be opened,
     *                         or the socket could not bind to the specified local port.
     * @see SocketException
     */
    private CommandTransmitter() throws SocketException {
        socket = new DatagramSocket();
    }

    /**
     * This method sets the transmitter's IP Address.
     *
     * @param address The IPv4 Address of the remote car.
     * @return Nothing.
     * @throws UnknownHostException if no IP address for the host could be found,
     *                              or if a scope_id was specified for a global IPv6 address.
     * @see UnknownHostException
     */
    void setInetAddress(String address) throws UnknownHostException {
        this.inetAddress = InetAddress.getByName(address);
    }

    /**
     * This method sends the command to the remote car.
     *
     * @param leftMotorSpeed   The speed of the left motor, ranges from <0, 255>.
     * @param rightMotorSpeed  The speed of the right motor, ranges from <0, 255>.
     * @param direction        The driving direction of the car (0 is backward, 1 is forward).
     * @param drivingMode      The driving mode of the car (0 is remote, 1 is autonomous).
     * @param drivingAlgorithm The algorithm used to drive the car.
     * @return Nothing.
     * @throws JSONException if parameters are of the wrong type
     * @see JSONException
     */
    void sendCommand(int leftMotorSpeed, int rightMotorSpeed,
                     int direction, int drivingMode, int drivingAlgorithm) throws JSONException {
        //  Prepare a stringified json object
        String jsonString = preparePacket(leftMotorSpeed,
                rightMotorSpeed,
                direction,
                drivingMode,
                drivingAlgorithm
        );

        //  Send the packet
        new SendCommandTask(this).execute(jsonString);
    }

    /**
     * This method creates a JSON object containing the speed, direction
     * and driving mode of the remote car
     *
     * @param leftMotorSpeed   The speed of the left motor, ranges from <0, 255>.
     * @param rightMotorSpeed  The speed of the right motor, ranges from <0, 255>.
     * @param direction        The driving direction of the car (0 is backward, 1 is forward).
     * @param drivingMode      The driving mode of the car (0 is remote, 1 is autonomous).
     * @param drivingAlgorithm The algorithm used to drive the car.
     * @return Stringified JSON object.
     * @throws JSONException if parameters are of the wrong type
     * @see JSONException
     */
    private String preparePacket(
            int leftMotorSpeed, int rightMotorSpeed,
            int direction, int drivingMode, int drivingAlgorithm) throws JSONException {

        JSONObject json = new JSONObject();

        json.put("left_motor_speed", leftMotorSpeed);
        json.put("right_motor_speed", rightMotorSpeed);
        json.put("direction", direction);
        json.put("driving_mode", drivingMode);
        json.put("driving_algorithm", drivingAlgorithm);

        return json.toString();
    }
}
