package com.example.iot_car_rc;

/**
 * Represents driving direction of the remote controlled car.
 *
 * @author Rafał Dąbrowski
 */
public enum DrivingDirection {
    FORWARD(1),
    BACKWARD(0);

    private int value;

    /**
     * Constructor for DrivingDirection enum.
     *
     * @param value the integer value assigned to the driving direction.
     */
    DrivingDirection(int value) {
        this.value = value;
    }

    /**
     * This method returns the driving direction.
     *
     * @return integer assigned to the driving direction.
     */
    public int getDrivingDirection() {
        return value;
    }
}
