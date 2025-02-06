package com.joshuadamian.neat.activationfunction;

public class LeakyReLU implements ActivationFunction {
    private final double alpha;

    public LeakyReLU() {
        this.alpha = 0.01; // Default value for alpha
    }

    public LeakyReLU(double alpha) {
        this.alpha = alpha;
    }

    @Override
    public double apply(double value) {
        return value > 0 ? value : alpha * value;
    }
}