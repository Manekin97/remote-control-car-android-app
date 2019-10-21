package com.example.iot_car_rc;

/**
 * Represents driving modes of the remote controlled car.
 *
 * @author Rafał Dąbrowski
 */
public enum DrivingMode {
    REMOTE(0),
    AUTONOMOUS(1);

    private int value;

    /**
     * Constructor for DrivingMode enum.
     *
     * @param value the integer value assigned to the driving mode.
     */
    DrivingMode(int value) {
        this.value = value;
    }

    /**
     * This method returns the driving mode.
     *
     * @return integer assigned to the driving mode.
     */
    public int getDrivingMode() {
        return value;
    }
}
