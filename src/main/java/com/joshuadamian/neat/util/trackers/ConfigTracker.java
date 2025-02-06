package com.joshuadamian.neat.util.trackers;

public class ConfigTracker {
    private static int configID = 0;

    public static int getNextConfigID() {
        return configID++;
    }
}
