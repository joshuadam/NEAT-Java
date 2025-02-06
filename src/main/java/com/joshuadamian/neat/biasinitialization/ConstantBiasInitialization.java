package com.joshuadamian.neat.biasinitialization;

public class ConstantBiasInitialization implements BiasInitialization {
    private double constantValue;

    public ConstantBiasInitialization(double constantValue) {
        this.constantValue = constantValue;
    }

    @Override
    public double initializeBias() {
        return constantValue;
    }
}