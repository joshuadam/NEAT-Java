package com.joshuadamian.neat.algorithm;

import com.joshuadamian.neat.config.Config;
import com.joshuadamian.neat.core.genome.Genome;
import com.joshuadamian.neat.util.GenomeBuilder;
import com.joshuadamian.neat.core.population.Population;

import java.util.ArrayList;

public class NEATAlgorithm {

    private Config config;
    private Population population;

    public NEATAlgorithm(Config config) {
        this.config = config;
        Genome baseGenome = GenomeBuilder.buildGenome(config, true);
        population = new Population(config.getPopulationSize(), baseGenome);
    }

    public NEATAlgorithm(Config config, Genome genome) {
        this.config = config;
        Genome baseGenome = genome;
        population = new Population(config.getPopulationSize(), baseGenome);
    }

    public NEATAlgorithm(Config config, ArrayList<Genome> genomes) {
        this.config = config;
        population = new Population(genomes, config);
    }

    public void run() {
        population.evaluatePopulation();
        population.speciate();
        for (int i = 0; i < config.getGenerations(); i++) {
            population.evolve();
            population.evaluatePopulation();
            population.speciate();
            double bestFitness = population.getBestGenome().getFitness();
            System.out.println("Generation: " + population.getGeneration()
                    + " best fitness: " + population.getBestGenome().getFitness());
            if (bestFitness >= config.getTargetFitness()) {
                System.out.println("Target fitness reached");
                break;
            }
        }
    }

    public void run(int iterations) {
        population.evaluatePopulation();
        population.speciate();
        for (int i = 0; i < iterations; i++) {
            population.evolve();
            population.evaluatePopulation();
            population.speciate();
            System.out.println("Generation: " + population.getGeneration()
                    + " best fitness: " + population.getBestGenome().getFitness());
        }
    }

    public Population getPopulation() {
        return population;
    }

    public Config getConfig() {
        return config;
    }

    public Genome getBestGenome() {
        return population.getBestGenome();
    }

    public ArrayList<Genome> getGenomes() {
        return population.getGenomes();
    }
}