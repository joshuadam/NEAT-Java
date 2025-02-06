package com.joshuadamian.neat.util.trackers;

public class GenomeTracker {
    private int genomeID = 0;

    public int getNextGenomeID() {
        return genomeID++;
    }
}
