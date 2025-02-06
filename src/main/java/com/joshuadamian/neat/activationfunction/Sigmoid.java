package com.joshuadamian.neat.activationfunction;

public class Sigmoid implements ActivationFunction {
    @Override
    public double apply(double value) {
        return 1 / (1 + Math.exp(-value));
    }
}
