package com.joshuadamian.neat.util.trackers;

import com.joshuadamian.neat.config.Config;

public class NodeTracker {
    private int nodeID;

    public NodeTracker() {
        this.nodeID = 0;
    }

    public int getNextNodeId() {
        return nodeID++;
    }
}