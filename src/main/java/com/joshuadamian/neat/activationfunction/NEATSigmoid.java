package com.joshuadamian.neat.activationfunction;

public class NEATSigmoid implements ActivationFunction {
    @Override
    public double apply(double value) {
        return 1 / (1 + Math.exp(-4.9 * value));
    }
}
