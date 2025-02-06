package com.joshuadamian.neat.config;

import com.joshuadamian.neat.activationfunction.ActivationFunction;
import com.joshuadamian.neat.activationfunction.NEATSigmoid;
import com.joshuadamian.neat.biasinitialization.BiasInitialization;
import com.joshuadamian.neat.biasinitialization.ConstantBiasInitialization;
import com.joshuadamian.neat.fitnessfunction.FitnessFunction;
import com.joshuadamian.neat.fitnessfunction.XOR;
import com.joshuadamian.neat.util.trackers.ConfigTracker;
import com.joshuadamian.neat.weightinitialization.RandomWeightInitialization;
import com.joshuadamian.neat.weightinitialization.WeightInitialization;

public class Config {

// --------------------------------------
//         NEAT Configuration
// --------------------------------------

    // Unique identifier for this NEAT configuration instance
    private int ID;

    // 1) Input/Output Layer Sizes
    private int inputSize = 2; // Number of inputs in the network
    private int outputSize = 1; // Number of outputs in the network

    // 2) Activation & Fitness Functions
    private ActivationFunction activationFunction = new NEATSigmoid(); // Activation function used in neurons
    private FitnessFunction fitnessFunction = new XOR(); // Fitness function to evaluate performance

    // 3) Bias & Weight Initialization
    private BiasInitialization biasInitialization = new ConstantBiasInitialization(1.0); // Defines how biases are initialized (here, constant = 1.0)

    private WeightInitialization weightInitialization = new RandomWeightInitialization(-1, 1); // Defines how weights are initialized (here, random in range -1 to 1)

    // 4) Compatibility & Speciation
    private double c1 = 1.0; // Compatibility coefficient for excess genes
    private double c2 = 1.0; // Compatibility coefficient for disjoint genes
    private double c3 = 0.4; // Compatibility coefficient for average weight differences
    private double compatibilityThreshold = 3.0; // Threshold at which genomes are placed into separate species
    private double interspeciesMatingRate = 0.001; // Probability of mating between different species

    // 5) Mutation Rates
    private double mutationRate = 1.0; // Overall rate at which genomes undergo mutation
    private double weightMutationRate = 0.8; // Probability of mutating each weight
    private double addConnectionMutationRate = 0.05; // Probability of adding a new connection between nodes
    private double addNodeMutationRate = 0.03; // Probability of adding a new node (splitting a connection)

    // 6) Evolution Parameters
    private int populationSize = 150; // Total number of genomes in each generation
    private int generations = 100; // How many generations to evolve
    private double targetFitness = 0.99; // Fitness the algorithm stops at
    private double survivalRate = 0.2; // Fraction of each species allowed to reproduce
    private int numOfElite = 10; // Number of top individuals to keep as elite
    private int dropOffAge = 15; // Age (generations) after which a species is removed if no improvement
    private double populationStagnationLimit = 20; // If no improvement in this many generations, entire population is considered stagnant

    // 7) Mutation Range & Crossover Settings
    private double perturbRange = 0.5; // Magnitude range for random weight perturbations
    private double keepDisabledOnCrossOverRate = 0.75; // Probability of keeping connections disabled during crossover if they are disabled in either parent
    private double mutateOnlyProb = 0.25; // Probability to skip crossover entirely and only mutate

    // 8) Recurrent Connection Settings
    private boolean allowRecurrentConnections = true; // Whether the algorithm permits recurrent connections
    private double recurrentConnectionRate = 1.0; // Probability of forming recurrent connections if they are allowed

    // 9) Weight Boundaries
    private double minWeight = -4.0; // Minimum allowed weight value
    private double maxWeight = 4.0; // Maximum allowed weight value

    public Config() {
        this.ID = ConfigTracker.getNextConfigID();
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public int getID() {
        return ID;
    }

    public Config setInputSize(int inputsize) {
        this.inputSize = inputsize;
        return this;
    }

    public int getInputSize() {
        return inputSize;
    }

    public Config setOutputSize(int outputsize) {
        this.outputSize = outputsize;
        return this;
    }

    public int getOutputSize() {
        return outputSize;
    }

    public Config setActivationFunction(ActivationFunction activationFunction) {
        this.activationFunction = activationFunction;
        return this;
    }

    public ActivationFunction getActivationFunction() {
        return activationFunction;
    }

    public Config setFitnessFunction(FitnessFunction fitnessFunction) {
        this.fitnessFunction = fitnessFunction;
        return this;
    }

    public FitnessFunction getFitnessFunction() {
        return fitnessFunction;
    }

    public Config setBiasInitialization(BiasInitialization biasInitialization) {
        this.biasInitialization = biasInitialization;
        return this;
    }

    public BiasInitialization getBiasInitialization() {
        return biasInitialization;
    }

    public Config setWeightInitialization(WeightInitialization weightInitialization) {
        this.weightInitialization = weightInitialization;
        return this;
    }

    public WeightInitialization getWeightInitialization() {
        return weightInitialization;
    }

    public Config setC1(double c1) {
        this.c1 = c1;
        return this;
    }

    public double getC1() {
        return c1;
    }

    public Config setC2(double c2) {
        this.c2 = c2;
        return this;
    }

    public double getC2() {
        return c2;
    }

    public Config setC3(double c3) {
        this.c3 = c3;
        return this;
    }

    public double getC3() {
        return c3;
    }

    public Config setCompatibilityThreshold(double compatibilityThreshold) {
        this.compatibilityThreshold = compatibilityThreshold;
        return this;
    }

    public double getCompatibilityThreshold() {
        return compatibilityThreshold;
    }

    public Config setInterspeciesMatingRate(double interspeciesMatingRate) {
        this.interspeciesMatingRate = interspeciesMatingRate;
        return this;
    }

    public double getInterspeciesMatingRate() {
        return interspeciesMatingRate;
    }

    public Config setMutationRate(double mutationRate) {
        this.mutationRate = mutationRate;
        return this;
    }

    public double getMutationRate() {
        return mutationRate;
    }

    public Config setWeightMutationRate(double weightMutationRate) {
        this.weightMutationRate = weightMutationRate;
        return this;
    }

    public double getWeightMutationRate() {
        return weightMutationRate;
    }

    public Config setAddConnectionMutationRate(double addConnectionMutationRate) {
        this.addConnectionMutationRate = addConnectionMutationRate;
        return this;
    }

    public double getTargetFitness() {
        return targetFitness;
    }

    public Config setTargetFitness(double targetFitness) {
        this.targetFitness = targetFitness;
        return this;
    }

    public double getAddConnectionMutationRate() {
        return addConnectionMutationRate;
    }

    public Config setAddNodeMutationRate(double addNodeMutationRate) {
        this.addNodeMutationRate = addNodeMutationRate;
        return this;
    }

    public double getAddNodeMutationRate() {
        return addNodeMutationRate;
    }

    public Config setPopulationSize(int populationSize) {
        this.populationSize = populationSize;
        return this;
    }

    public int getPopulationSize() {
        return populationSize;
    }

    public Config setGenerations(int generations) {
        this.generations = generations;
        return this;
    }

    public int getGenerations() {
        return generations;
    }

    public Config setSurvivalRate(double survivalRate) {
        this.survivalRate = survivalRate;
        return this;
    }

    public double getSurvivalRate() {
        return survivalRate;
    }

    public Config setNumOfElite(int numOfElite) {
        this.numOfElite = numOfElite;
        return this;
    }

    public int getNumOfElite() {
        return numOfElite;
    }

    public Config setDropOffAge(int dropOffAge) {
        this.dropOffAge = dropOffAge;
        return this;
    }

    public int getDropOffAge() {
        return dropOffAge;
    }

    public Config setPopulationStagnationLimit(double populationStagnationLimit) {
        this.populationStagnationLimit = populationStagnationLimit;
        return this;
    }

    public double getPopulationStagnationLimit() {
        return populationStagnationLimit;
    }

    public Config setPerturbRange(double perturbRange) {
        this.perturbRange = perturbRange;
        return this;
    }

    public double getPerturbRange() {
        return perturbRange;
    }

    public Config setKeepDisabledOnCrossOverRate(double keepDisabledOnCrossOverRate) {
        this.keepDisabledOnCrossOverRate = keepDisabledOnCrossOverRate;
        return this;
    }

    public double getKeepDisabledOnCrossOverRate() {
        return keepDisabledOnCrossOverRate;
    }

    public Config setAllowRecurrentConnections(boolean allowRecurrentConnections) {
        this.allowRecurrentConnections = allowRecurrentConnections;
        return this;
    }

    public boolean getAllowRecurrentConnections() {
        return allowRecurrentConnections;
    }

    public Config setRecurrentConnectionRate(double recurrentConnectionRate) {
        this.recurrentConnectionRate = recurrentConnectionRate;
        return this;
    }

    public double getRecurrentConnectionRate() {
        return recurrentConnectionRate;
    }

    public Config setMinWeight(double minWeight) {
        this.minWeight = minWeight;
        return this;
    }

    public double getMinWeight() {
        return minWeight;
    }

    public Config setMaxWeight(double maxWeight) {
        this.maxWeight = maxWeight;
        return this;
    }

    public double getMaxWeight() {
        return maxWeight;
    }

    public Config setMutateOnlyProb(double mutateOnlyProb) {
        this.mutateOnlyProb = mutateOnlyProb;
        return this;
    }

    public double getMutateOnlyProb() {
        return mutateOnlyProb;
    }
}
