package com.joshuadamian.neat.activationfunction;

public class Gaussian implements ActivationFunction {
    @Override
    public double apply(double value) {
        return Math.exp(-value * value);
    }
}