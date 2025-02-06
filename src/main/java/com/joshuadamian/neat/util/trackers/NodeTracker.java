package com.joshuadamian.neat.util.trackers;

import com.joshuadamian.neat.config.Config;

public class NodeTracker {
    private Config config;
    private int nodeID;

    public NodeTracker(Config config) {
        this.config = config;
        this.nodeID = 0;
    }

    public int getNextNodeID() {
        return nodeID++;
    }
}
