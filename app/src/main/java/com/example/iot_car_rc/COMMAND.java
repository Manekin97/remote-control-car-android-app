package com.example.iot_car_rc;

public enum COMMAND {
    FORWARD(0b11),
    BACKWARD(0b00),
    LEFT(0b10),
    RIGHT(0b01),
    STOP(0b101);

    private byte value;

    COMMAND(int value) {
        this.value = (byte) value;
    }

    public byte getCommandCode() {
        return value;
    }
}
