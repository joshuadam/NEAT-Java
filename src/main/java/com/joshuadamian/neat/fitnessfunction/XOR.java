package com.joshuadamian.neat.fitnessfunction;

import com.joshuadamian.neat.core.genome.Genome;

public class XOR implements FitnessFunction {


    public double calculateFitness(Genome genome) {

        double[][] inputs = {
                {0, 0}, {0, 1}, {1, 0}, {1, 1}
        };
        double[] expectedOutputs = {0, 1, 1, 0};

        double error = 0;
        for (int i = 0; i < inputs.length; i++) {
            double[] input = inputs[i];

            double[] output = genome.propagate(input);

            error += Math.pow(output[0] - expectedOutputs[i], 2);
        }

        return 1.0 / (1.0 + error);
    }

}