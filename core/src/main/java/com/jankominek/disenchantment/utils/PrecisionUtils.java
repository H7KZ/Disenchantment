package com.jankominek.disenchantment.utils;

/**
 * Utility class for rounding double values to a specified number of decimal places.
 */
public class PrecisionUtils {
    /**
     * Rounds a double value to the given number of decimal places.
     *
     * @param value     the value to round
     * @param precision the number of decimal places
     * @return the rounded value
     */
    public static double round(double value, int precision) {
        int scale = (int) Math.pow(10, precision);
        return (double) Math.round(value * scale) / scale;
    }
}
