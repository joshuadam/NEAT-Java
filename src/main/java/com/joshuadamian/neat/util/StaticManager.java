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


    public static InnovationTracker getInnovationTracker(int populationId) {
        if (innovationTrackerMap.containsKey(populationId)) {
            return innovationTrackerMap.get((populationId));
        } else {
            InnovationTracker newInnovationTracker = new InnovationTracker();
            innovationTrackerMap.put(populationId, newInnovationTracker);
            return newInnovationTracker;
        }
    }

    public static GenomeTracker getGenomeTracker(int populationId) {
        if (genomeTrackerMap.containsKey(populationId)) {
            return genomeTrackerMap.get((populationId));
        } else {
            GenomeTracker genomeTracker = new GenomeTracker();
            genomeTrackerMap.put(populationId, genomeTracker);
            return genomeTracker;
        }
    }

    public static NodeTracker getNodeTracker(int populationId) {
        if (nodeTrackerMap.containsKey(populationId)) {
            return nodeTrackerMap.get((populationId));
        } else {
            NodeTracker nodeTracker = new NodeTracker();
            nodeTrackerMap.put(populationId, nodeTracker);
            return nodeTracker;
        }
    }
}