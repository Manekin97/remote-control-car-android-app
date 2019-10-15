package com.example.iot_car_rc;

public enum DrivingMode {
    REMOTE(0),
    AUTONOMOUS(1);

    private int value;

    DrivingMode(int value) {
        this.value = value;
    }

    public int getDrivingMode() {
        return value;
    }
}
