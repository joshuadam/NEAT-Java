package com.joshuadamian.neat.util;

import com.joshuadamian.neat.config.Config;
import com.joshuadamian.neat.util.trackers.GenomeTracker;
import com.joshuadamian.neat.util.trackers.NodeTracker;
import com.joshuadamian.neat.util.trackers.innovationtracker.InnovationTracker;

import java.util.HashMap;
import java.util.Map;

public class StaticManager {
    private static Map<Integer, InnovationTracker> innovationTrackerMap = new HashMap<>();
    private static Map<Integer, GenomeTracker> genomeTrackerMap = new HashMap<>();
    private static Map<Integer, NodeTracker> nodeTrackerMap = new HashMap<>();


    public static InnovationTracker getInnovationTracker(Config config) {
        if (innovationTrackerMap.containsKey(config.getID())) {
            return innovationTrackerMap.get((config.getID()));
        } else {
            InnovationTracker newInnovationTracker = new InnovationTracker();
            innovationTrackerMap.put(config.getID(), newInnovationTracker);
            return newInnovationTracker;
        }
    }

    public static InnovationTracker registerInnovationTracker(Config config) {
        if (innovationTrackerMap.containsKey(config.getID())) {
            return innovationTrackerMap.get((config.getID()));
        } else {
            InnovationTracker newInnovationTracker = new InnovationTracker();
            innovationTrackerMap.put(config.getID(), newInnovationTracker);
            return newInnovationTracker;
        }
    }

    public static GenomeTracker getGenomeTracker(Config config) {
        if (genomeTrackerMap.containsKey(config.getID())) {
            return genomeTrackerMap.get((config.getID()));
        } else {
            GenomeTracker genomeTracker = new GenomeTracker();
            genomeTrackerMap.put(config.getID(), genomeTracker);
            return genomeTracker;
        }
    }

    public static NodeTracker getNodeTracker(Config config) {
        if (nodeTrackerMap.containsKey(config.getID())) {
            return nodeTrackerMap.get((config.getID()));
        } else {
            NodeTracker nodeTracker = new NodeTracker(config);
            nodeTrackerMap.put(config.getID(), nodeTracker);
            return nodeTracker;
        }
    }
}
