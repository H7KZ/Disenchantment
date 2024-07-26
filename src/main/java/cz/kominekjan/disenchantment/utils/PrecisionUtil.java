package cz.kominekjan.disenchantment.utils;

public class PrecisionUtil {
    public static double round(double value, int precision) {
        int scale = (int) Math.pow(10, precision);
        return (double) Math.round(value * scale) / scale;
    }
}
