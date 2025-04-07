package com.joshuadamian.neat.util.trackers;

public class PopulationTracker {
        private static int populationId = 0;

        public static int getNextPopulationId() {
            return populationId++;
        }
}