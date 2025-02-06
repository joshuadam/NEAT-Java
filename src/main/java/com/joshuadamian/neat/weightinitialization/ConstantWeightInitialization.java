package com.joshuadamian.neat.weightinitialization;

public class ConstantWeightInitialization implements WeightInitialization {
    private double constantValue;

    public ConstantWeightInitialization(double constantValue) {
        this.constantValue = constantValue;
    }

    @Override
    public double initializeWeight() {
        return constantValue;
    }

    @Override
    public double[] initializeWeights(int size) {
        double[] weights = new double[size];
        for (int i = 0; i < size; i++) {
            weights[i] = constantValue;
        }
        return weights;
    }
}
