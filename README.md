# NEAT-Java

This project is a Java-based implementation of NEAT (Neuroevolution of Augmenting Topologies), an evolutionary algorithm developed by Kenneth O. Stanley and Risto Miikkulainen. Originally introduced in their 2002 paper, [Evolving Neural Networks Through Augmenting Topologies](https://nn.cs.utexas.edu/downloads/papers/stanley.ec02.pdf), NEAT presents a novel approach to evolving artificial neural networks by optimizing both network weights and structures over generations.

In this implementation, NEAT’s principles are faithfully applied, emphasizing the algorithm's core components: speciation, crossover, and structural mutation. These elements enable neural networks to adapt in complexity as they evolve, making NEAT a unique approach to neuroevolutionary algorithms.

This implementation offers a clear, accessible codebase for those exploring NEAT and has achieved results comparable to the benchmarks demonstrated in the original paper, providing a solid foundation for further exploration and development.

## Installation

### Maven

Add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.joshuadamian</groupId>
    <artifactId>neat</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Gradle

#### Kotlin DSL (build.gradle.kts):

```kotlin
dependencies {
    implementation("com.joshuadamian:neat:1.0.0")
}
```

#### Groovy DSL (build.gradle):

```gradle
dependencies {
    implementation 'com.joshuadamian:neat:1.0.0'
}
```

### Manually Downloading the JAR

You can manually download the JAR file from the **Releases** section on GitHub and add it to your classpath.
[NEAT-Java Releases](https://github.com/joshuadam/NEAT-Java/releases)

## Basic usage

### Implementing a Fitness Function

To get started you have to implement a fitness function. A fitness function evaluates how well a given genome performs a specific task. You must implement the FitnessFunction interface from neat.fitnessfunction:

```java
package com.joshuadamian.neat.fitnessfunction;

import com.joshuadamian.neat.core.genome.Genome;

public interface FitnessFunction {
    double calculateFitness(Genome genome);
}
```

The function should return a fitness score, where higher scores indicate better performance. The XOR function is provided as an example:

```java
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
            double[] output = genome.propagate(inputs[i]);
            error += Math.pow(output[0] - expectedOutputs[i], 2);
        }
        return 1.0 / (1.0 + error);
    }
}
```

### Configuration

Create a configuration instance and customize the settings you want to change. If not specified, the default parameters will be used.

```java
import com.joshuadamian.neat.config.Config;
import com.joshuadamian.neat.activationfunction.NEATSigmoid;
import com.joshuadamian.neat.biasinitialization.ConstantBiasInitialization;
import com.joshuadamian.neat.fitnessfunction.XOR;
import com.joshuadamian.neat.weightinitialization.RandomWeightInitialization;

Config config = new Config()
        .setInputSize(2) // Number of input neurons
        .setOutputSize(1) // Number of output neurons
        .setActivationFunction(new NEATSigmoid()) // Activation function
        .setFitnessFunction(new XOR()) // Fitness function
        .setBiasInitialization(new ConstantBiasInitialization(1.0)) // Bias initialization (constant value of 1.0)
        .setWeightInitialization(new RandomWeightInitialization(-1, 1)) // Random weight initialization between -1 and 1
        .setC1(1.0) // Excess gene coefficient
        .setC2(1.0) // Disjoint gene coefficient
        .setC3(0.4) // Weight difference coefficient
        .setCompatibilityThreshold(3.0) // Threshold for species separation
        .setInterspeciesMatingRate(0.001) // Probability of interspecies mating
        .setMutationRate(1.0) // Overall mutation rate
        .setWeightMutationRate(0.8) // Probability of mutating a weight
        .setAddConnectionMutationRate(0.05) // Probability of adding a connection
        .setAddNodeMutationRate(0.03) // Probability of adding a node
        .setPopulationSize(150) // Population size
        .setGenerations(100) // Number of generations to evolve
        .setTargetFitness(0.8) // Fitness the algorithm stops at
        .setSurvivalRate(0.2) // Percentage of top individuals surviving per generation
        .setNumOfElite(10) // Number of elite genomes that are preserved
        .setDropOffAge(15) // Max age before a species is removed if no improvement
        .setPopulationStagnationLimit(20) // Max generations without improvement before population is considered stagnant
        .setPerturbRange(0.5) // Range for random weight perturbations
        .setKeepDisabledOnCrossOverRate(0.75) // Probability of keeping connections disabled during crossover if they are disabled in either parent
        .setMutateOnlyProb(0.25) // Probability of skipping crossover and only mutating
        .setAllowRecurrentConnections(true) // Allow recurrent connections in the network
        .setRecurrentConnectionRate(1.0) // Probability of forming recurrent connections
        .setMinWeight(-4.0) // Minimum allowed weight
        .setMaxWeight(4.0); // Maximum allowed weight
        .setConnectBias(true) // Fully connect bias on network construction
```

### Running the Algorithm

To execute the algorithm, create an instance and pass the configuration:

```java
NEATAlgorithm algorithm = new NEATAlgorithm(config);
algorithm.run();

// Optionally, run it for a specific number of generations
algorithm.run(10);
```

### Results

If everything has been set up correctly, your population should have successfully learned the given task. You can verify this by propagating the best-performing genome.

#### Example: Propagating the Best Genome

```java
// Retrieve the best-performing genome from the algorithm
Genome bestGenome = algorithm.getBestGenome(); 

// Reset the genome's state before propagating if it includes recurrent connections
bestGenome.resetState(); 

// Define input test cases
double[][] inputs = {
        {0, 0}, {0, 1}, {1, 0}, {1, 1}
};

// Propagate each input through the best genome and display the output
for (int i = 0; i < inputs.length; i++) {
    double[] input = inputs[i];
    double[] output = bestGenome.propagate(input);
    System.out.println("Genome's output for " + input[0] + ", " + input[1] + " is: " + output[0]);
}
```

### Saving and loading genomes

To save a genome, use the saveGenome method and specify the file path:

```java
genome.saveGenome("./genome.json");
```

To load a genome, use the GenomeBuilder.loadGenome method, providing the file path and a Config instance:

```java
Genome loadedGenome = GenomeBuilder.loadGenome("./genome.json", config);
```


## Contributing

This project is still a work in progress. Before making any contributions, please reach out to me to ensure your efforts align with the project's current direction.