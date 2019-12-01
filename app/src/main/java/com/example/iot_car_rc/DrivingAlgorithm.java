package com.example.iot_car_rc;

/**
 * Represents driving algorithms of the remote controlled car.
 *
 * @author Rafał Dąbrowski
 */
public enum DrivingAlgorithm {
    SIMPLE(0),
    ADVANCED(1),
    COMPLEX(2);

    private int value;

    /**
     * Constructor for DrivingAlgorithm enum.
     *
     * @param value the integer value assigned to the driving algorithm.
     */
    DrivingAlgorithm(int value) {
        this.value = value;
    }

    /**
     * This method returns the driving algorithm.
     *
     * @return integer assigned to the driving algorithm.
     */
    public int getDrivingAlgorithm() {
        return value;
    }
}
