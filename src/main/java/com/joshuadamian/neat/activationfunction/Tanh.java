package com.joshuadamian.neat.activationfunction;

public class Tanh implements ActivationFunction {
    @Override
    public double apply(double x) {
        return Math.tanh(x);
    }
}