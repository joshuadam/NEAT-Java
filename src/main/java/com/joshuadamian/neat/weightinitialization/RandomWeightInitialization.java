package com.joshuadamian.neat.weightinitialization;

import java.util.Random;

public class RandomWeightInitialization implements WeightInitialization {
    private double min;
    private double max;
    private Random random;

    public RandomWeightInitialization(double min, double max) {
        this.min = min;
        this.max = max;
        this.random = new Random();
    }

    @Override
    public double initializeWeight() {
        return min + (max - min) * random.nextDouble();
    }

    @Override
    public double[] initializeWeights(int size) {
        double[] weights = new double[size];
        for (int i = 0; i < size; i++) {
            weights[i] = initializeWeight();
        }
        return weights;
    }
}