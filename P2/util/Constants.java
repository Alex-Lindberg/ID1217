package util;

public class Constants {
    /**
     * Some of these constants need tuning to produce a decent looking result.
     */

    public static final double DOWNSCALING = 1E7;
    public static final double G = 6.67e-11 * (DOWNSCALING / 4);
    public static final double EARTH_MASS = 5E04 / DOWNSCALING;
    public static final double SUN_MASS = 6E26 / DOWNSCALING;
    public static final double RADIUS = 1.33920E05 / DOWNSCALING;
    public static final double MIN_DIST = 1.01565E03 / DOWNSCALING;
    public static final double START_VEL = 1E05 / DOWNSCALING;
    public static final double SOFTENING = 1e5;
    public static final double DT = 0.1;
    public static final double MASS_VARIANCE = 0.1;

    public static final double theta = 1.5;

    public static final int POS_SCALING = 400;
    public static final int MASS_SCALING = 5;

    // public static final double DOWNSCALING = 0.01;
    // public static final double G = 6.67e-2;
    // public static final double EARTH_MASS = 59.742 * DOWNSCALING;
    // public static final double SUN_MASS = EARTH_MASS * 3.3 * 1e6;
    // public static final double RADIUS = 10;
    // public static final double MIN_DIST = 5;
    // public static final double SOFTENING = 1e5;
    // public static final double DT = 0.1;
    // public static final double START_VEL = 0.0001;
    // public static final double MASS_VARIANCE = 0.2;

    // public static final double theta = 1.5;

    // public static final int POS_SCALING = 400;
    // public static final int MASS_SCALING = 5;
}
