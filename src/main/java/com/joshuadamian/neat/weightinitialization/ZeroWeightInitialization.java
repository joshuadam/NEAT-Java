package com.joshuadamian.neat.weightinitialization;

public class ZeroWeightInitialization implements WeightInitialization {

    @Override
    public double initializeWeight() {
        return 0.0;
    }

    @Override
    public double[] initializeWeights(int size) {
        return new double[size]; // All elements are initialized to 0 by default
    }
}