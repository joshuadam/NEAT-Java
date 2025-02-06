package com.joshuadamian.neat.biasinitialization;

import java.util.Random;

public class RandomBiasInitialization implements BiasInitialization {
    private double min;
    private double max;
    private Random random;

    public RandomBiasInitialization(double min, double max) {
        this.min = min;
        this.max = max;
        this.random = new Random();
    }

    @Override
    public double initializeBias() {
        return min + (max - min) * random.nextDouble();
    }
}