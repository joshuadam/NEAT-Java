package com.joshuadamian.neat.activationfunction;

public class Sine implements ActivationFunction {
    @Override
    public double apply(double value) {
        return Math.sin(value);
    }
}