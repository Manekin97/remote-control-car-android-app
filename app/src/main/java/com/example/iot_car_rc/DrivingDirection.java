package com.example.iot_car_rc;

public enum DrivingDirection {
    FORWARD(1),
    BACKWARD(0);

    private int value;

    DrivingDirection(int value) {
        this.value = value;
    }

    public int getDrivingDirection() {
        return value;
    }
}
