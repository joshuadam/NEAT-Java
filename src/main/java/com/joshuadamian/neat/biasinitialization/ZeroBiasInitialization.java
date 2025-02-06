package com.joshuadamian.neat.biasinitialization;

public class ZeroBiasInitialization implements BiasInitialization {

    @Override
    public double initializeBias() {
        return 0.0;
    }
}