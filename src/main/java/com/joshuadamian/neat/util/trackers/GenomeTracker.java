package com.joshuadamian.neat.util.trackers;

public class GenomeTracker {
    private int genomeId = 0;

    public int getNextGenomeId() {
        return genomeId++;
    }
}