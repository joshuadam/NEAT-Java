package com.joshuadamian.neat.core.population;

import com.joshuadamian.neat.util.StaticManager;
import com.joshuadamian.neat.config.Config;
import com.joshuadamian.neat.core.genome.Genome;
import com.joshuadamian.neat.util.trackers.innovationtracker.InnovationTracker;

import java.util.*;
import java.util.stream.Collectors;

public class Population {

    private ArrayList<Genome> genomes = new ArrayList<>();
    private ArrayList<Species> species = new ArrayList<>();
    private int eliteCount;
    private ArrayList<Genome> eliteGenomes = new ArrayList<>();
    private Genome[] newGeneration;
    private Config config;
    private int newGenerationIndex = 0;
    private boolean allStagnated = false;
    private boolean stale = false;
    private InnovationTracker innovationTracker;
    private int generation = 0;
    private int speciesCounter = 1;
    private double bestFitness = 0;
    private double age_since_last_improvement = 0;

    public Population(int populationSize, Genome baseGenome) {
        for (int i = 0; i < populationSize; i++) {
            genomes.add(baseGenome.copy());
        }
        for (Genome genome : genomes) {
            genome.reinitializeWeights();
        }
        config = baseGenome.getConfig();
        newGeneration = new Genome[config.getPopulationSize()];
        this.innovationTracker = StaticManager.getInnovationTracker(config);
    }

    public Population(ArrayList<Genome> genomes, Config config) {
        this.genomes = genomes;
        this.config = config;
        newGeneration = new Genome[config.getPopulationSize()];
        this.innovationTracker = StaticManager.getInnovationTracker(config);
    }

    public void speciate() {
        for (Species s : species) {
            s.clearGenomes();
        }
        for (Genome genome : genomes) {
            boolean speciesFound = false;
            for (Species species : species) {
                Genome representative = species.getRepresentative();
                if (representative != null
                        && genome.getGeneticEncoding().calculateCompatibilityDistance(representative.getGeneticEncoding())
                        < config.getCompatibilityThreshold()) {
                    species.addGenome(genome);
                    speciesFound = true;
                    break;
                }
            }
            if (!speciesFound) {
                Species newSpecies = new Species(speciesCounter++, config);
                newSpecies.addGenome(genome);
                species.add(newSpecies);
            }
        }
        species = species.stream()
                .filter(s -> !s.getGenomes().isEmpty())
                .collect(Collectors.toCollection(ArrayList::new));
        for (Species s : species) {
            s.setRandomRepresentative();
        }
    }

    public void evolve() {
        stale = false;
        allStagnated = false;
        eliteCount = 0;
        newGenerationIndex = 0;
        newGeneration = new Genome[config.getPopulationSize()];
        eliteGenomes.clear();

        innovationTracker.reset();
        saveEliteGenomes();
        handleStagnation();
        removeWorstGenomes();
        calculateOffspring();
        generateOffspring();
        putBackElite();

        genomes.clear();
        Collections.addAll(genomes, newGeneration);

        for (Genome genome : genomes) {
            genome.prune();
        }
        generation++;
    }

    public void evaluatePopulation() {
        for (Genome genome : genomes) {
            genome.calculateFitness();
        }
        for (Species s : species) {
            s.setAdjustedFitness();
        }
    }

    public void removeEmptySpecies() {
        Iterator<Species> iterator = species.iterator();
        while (iterator.hasNext()) {
            Species s = iterator.next();
            if (s.getGenomes().isEmpty()) {
                iterator.remove();
            }
        }
    }

    public Genome getBestGenome() {
        Genome bestGenome = genomes.get(0);
        for (Genome genome : genomes) {
            if (genome.getFitness() > bestGenome.getFitness()) {
                bestGenome = genome;
            }
        }
        return bestGenome;
    }

    private void removeWorstGenomes() {
        for (Species s : species) {
            s.removeBadGenomes();
        }
        removeGenomesWithoutSpecies();
    }

    private void putBackElite() {
        for (Genome genome : eliteGenomes) {
            newGeneration[newGenerationIndex++] = genome;
        }
    }

    private void saveEliteGenomes() {
        sortGenomes();
        for (Species species : species) {
            if (species.getGenomes().size() > 5) {
                eliteGenomes.add(species.getBestGenome().copy());
                eliteCount++;
            }
        }
        for (int i = 0; i < config.getNumOfElite(); i++) {
            Genome bestGenome = genomes.get(i);
            boolean isUnique = true;
            for (Genome eliteGenome : eliteGenomes) {
                if (eliteGenome != null && eliteGenome.equalsGenome(bestGenome)) {
                    isUnique = false;
                    break;
                }
            }
            if (isUnique) {
                eliteGenomes.add(bestGenome.copy());
                eliteCount++;
            }
        }
    }

    private void generateOffspring() {
        for (Species s : species) {
            int offspringCount = s.getOffspringCount();
            ArrayList<Genome> mutatedonlyGenomes = new ArrayList<>();

            for (int i = 0; i < offspringCount; i++) {
                Random mutateRandom = new Random();
                int genomesInSpecies = s.getGenomes().size();

                if (mutateRandom.nextDouble() < config.getMutateOnlyProb()) {
                    Genome selectedGenome = s.getGenomes().get((int) (Math.random() * genomesInSpecies));
                    while (genomesInSpecies > 1 && mutatedonlyGenomes.contains(selectedGenome) && i < genomesInSpecies) {
                        selectedGenome = s.getGenomes().get((int) (Math.random() * genomesInSpecies));
                    }
                    mutatedonlyGenomes.add(selectedGenome);
                    Genome offspring = selectedGenome.copy();
                    offspring.mutate();
                    newGeneration[newGenerationIndex++] = offspring;
                    continue;
                }

                Random random = new Random();
                if (random.nextDouble() < config.getInterspeciesMatingRate() && species.size() > 1) {
                    Species randomSpecies = species.get(random.nextInt(species.size()));
                    while (randomSpecies == s) {
                        randomSpecies = species.get(random.nextInt(species.size()));
                    }
                    Genome parent1 = s.getGenomes().get((int) (Math.random() * s.getGenomes().size()));
                    Genome parent2 = randomSpecies.getGenomes().get((int) (Math.random() * randomSpecies.getGenomes().size()));
                    Genome offspring = parent1.crossover(parent2);
                    if (Math.random() <= config.getMutationRate()) {
                        offspring.mutate();
                    }
                    newGeneration[newGenerationIndex++] = offspring;
                    continue;
                }

                boolean parentsFound = false;
                Genome parent1 = null;
                Genome parent2 = null;
                Genome offspring = null;

                if (s.getGenomes().size() > 1) {
                    while (!parentsFound) {
                        parent1 = s.getGenomes().get((int) (Math.random() * s.getGenomes().size()));
                        parent2 = s.getGenomes().get((int) (Math.random() * s.getGenomes().size()));
                        if (parent1 != parent2) {
                            parentsFound = true;
                        }
                    }
                    offspring = parent1.crossover(parent2);
                    if (Math.random() <= config.getMutationRate()) {
                        offspring.mutate();
                    }
                } else {
                    offspring = s.getGenomes().get(0).copy();
                    offspring.mutate();
                }
                newGeneration[newGenerationIndex++] = offspring;
            }
        }
    }

    private void sortGenomes() {
        genomes.sort(Comparator.comparingDouble(Genome::getFitness).reversed());
    }

    private void handleStagnation() {
        updateFitnessAndStagnation();
        for (Species s : species) {
            s.updateFitnessAndStagnation();
        }
        allStagnated = true;
        for (Species s : species) {
            if (!s.isStagnated()) {
                allStagnated = false;
                break;
            }
        }
        removeStale();
    }

    private void updateFitnessAndStagnation() {
        double currentBestFitness = getBestGenome().getFitness();
        if (currentBestFitness > bestFitness) {
            bestFitness = currentBestFitness;
            age_since_last_improvement = 0;
        } else {
            age_since_last_improvement++;
        }
        if (age_since_last_improvement > config.getPopulationStagnationLimit()) {
            stale = true;
        }
    }

    private void removeStale() {
        sortSpecies();
        if (stale) {
            if (species.size() > 3) {
                species.subList(2, species.size()).clear();
                removeGenomesWithoutSpecies();
            }
            return;
        }
        if (allStagnated) {
            if (species.size() > 2) {
                species.subList(1, species.size()).clear();
                removeGenomesWithoutSpecies();
            }
            return;
        }
        Iterator<Species> iterator = species.iterator();
        while (iterator.hasNext()) {
            Species s = iterator.next();
            if (s.isStagnated()) {
                iterator.remove();
            }
        }
        removeGenomesWithoutSpecies();
    }

    private void removeGenomesWithoutSpecies() {
        ArrayList<Genome> survivingGenomes = new ArrayList<>();
        for (Species s : species) {
            survivingGenomes.addAll(s.getGenomes());
        }
        genomes = survivingGenomes;
    }

    private void sortSpecies() {
        species.sort(Comparator.comparingDouble(Species::getBestFitness).reversed());
    }

    private void calculateOffspring() {
        int remainingPopulation = config.getPopulationSize() - eliteCount;
        double totalAdjustedFitness = 0;
        for (Species s : species) {
            s.setAdjustedFitness();
            totalAdjustedFitness += s.getTotalAdjustedFitness();
        }
        double[] percentageOfOffspring = new double[species.size()];
        for (int i = 0; i < species.size(); i++) {
            percentageOfOffspring[i] = (species.get(i).getTotalAdjustedFitness() / totalAdjustedFitness) * 100;
        }
        for (int i = 0; i < species.size(); i++) {
            int count = (int) (percentageOfOffspring[i] / 100 * remainingPopulation);
            species.get(i).setOffspringCount(count);
        }
        int totalOffspring = 0;
        for (Species s : species) {
            totalOffspring += s.getOffspringCount();
        }
        if (totalOffspring > remainingPopulation) {
            int difference = totalOffspring - remainingPopulation;
            Species worstSpecies = species.get(0);
            for (Species s : species) {
                if (s.getTotalAdjustedFitness() < worstSpecies.getTotalAdjustedFitness()) {
                    worstSpecies = s;
                }
            }
            worstSpecies.setOffspringCount(worstSpecies.getOffspringCount() - difference);
        }
        if (totalOffspring < remainingPopulation) {
            int difference = remainingPopulation - totalOffspring;
            for (int i = 0; i < difference; i++) {
                Species bestSpecies = selectBestSpecies();
                bestSpecies.setOffspringCount(bestSpecies.getOffspringCount() + 1);
            }
        }
    }

    private Species selectBestSpecies() {
        Species bestSpecies = species.get(0);
        for (Species s : species) {
            if (s.getBestFitness() > bestSpecies.getBestFitness()) {
                bestSpecies = s;
            }
        }
        return bestSpecies;
    }

    public int getGeneration() {
        return generation;
    }

    public void setConfig(Config config) {
        this.config = config;
    }

    public Config getConfig() {
        return config;
    }

    public ArrayList<Species> getSpecies() {
        return species;
    }

    public ArrayList<Genome> getGenomes() {
        return genomes;
    }
}