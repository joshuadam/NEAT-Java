package com.joshuadamian.neat.fitnessfunction;

import com.joshuadamian.neat.core.genome.Genome;

public interface FitnessFunction {
    double calculateFitness(Genome genome);
}