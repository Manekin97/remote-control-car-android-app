package com.example.iot_car_rc;

public enum COMMAND {
    FORWARD(0b0011),
    BACKWARD(0b0000),
    LEFT(0b0010),
    RIGHT(0b0001),
    STOP(0b0101),
    SET_REMOTE_CONTROL_MODE(0b0110),
    SET_AUTONOMOUS_CONTROL_MODE(0b111);

    private byte value;

    COMMAND(int value) {
        this.value = (byte) value;
    }

    public byte getCommandCode() {
        return value;
    }
}
