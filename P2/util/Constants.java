package util;

/**
 * Some of these constants need tuning to produce a decent looking result.
 */
public class Constants {

    /* Tuned to work and view */
    public static final double DOWNSCALING = 1;
    public static final double G = 6.67e-4 * (DOWNSCALING);
    public static final double EARTH_MASS = 1e-2 * DOWNSCALING;
    public static final double SUN_MASS = 2500 * DOWNSCALING;
    public static final double RADIUS = 300 * DOWNSCALING;
    public static final double MIN_DIST = 250 * DOWNSCALING;
    public static final double START_VEL = 0.0001 * DOWNSCALING;
    public static final double SOFTENING = 1e5;
    public static final double DT = 0.1;
    public static final double MASS_VARIANCE = 0.1;
    public static final double POS_SCALING = 1;
    public static final double MASS_SCALING = 3;
    public static final double MASS_SCALING_SUN = 1;

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

}
