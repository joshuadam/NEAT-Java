package com.joshuadamian.neat.activationfunction;

public class ReLU implements ActivationFunction {
    @Override
    public double apply(double value) {
        return Math.max(0, value);
    }
}