package com.joshuadamian.neat.activationfunction;

public class Softplus implements ActivationFunction {
    @Override
    public double apply(double value) {
        return Math.log(1 + Math.exp(value));
    }
}