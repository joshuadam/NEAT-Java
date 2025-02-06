package com.joshuadamian.neat.core.population;

import com.joshuadamian.neat.config.Config;
import com.joshuadamian.neat.core.genome.Genome;

import java.util.ArrayList;
import java.util.Comparator;

public class Species {

    private int id;
    private double bestFitness = 0;
    private int generationsSinceImprovement = 0;
    private boolean stagnated = false;
    private ArrayList<Genome> genomes = new ArrayList<>();
    private Genome representative;
    private int offspringCount = 0;
    private Config config;

    public Species(int id, Config config) {
        this.id = id;
        this.config = config;
    }

    public void addGenome(Genome genome) {
        if (genomes.size() == 0) {
            representative = genome;
        }
        genomes.add(genome);
    }

    public void clearGenomes() {
        genomes.clear();
    }

    public void setAdjustedFitness() {
        for (Genome genome : genomes) {
            genome.setAdjustedFitness(genome.getFitness() / genomes.size());
        }
    }

    public void removeBadGenomes() {
        genomes.sort(Comparator.comparingDouble(Genome::getFitness).reversed());
        int totalGenomes = genomes.size();
        int numberToSurvive = Math.max(1, (int) (totalGenomes * config.getSurvivalRate()));
        if (genomes.size() > numberToSurvive) {
            genomes.subList(numberToSurvive, genomes.size()).clear();
        }
    }

    public void updateFitnessAndStagnation() {
        double currentbest = getBestGenome().getFitness();
        if (currentbest > bestFitness) {
            bestFitness = currentbest;
            generationsSinceImprovement = 0;
        } else {
            generationsSinceImprovement++;
        }
        if (generationsSinceImprovement > config.getDropOffAge()) {
            stagnated = true;
        }
    }

    public double getTotalAdjustedFitness() {
        double total = 0;
        for (Genome genome : genomes) {
            total += genome.getAdjustedFitness();
        }
        return total;
    }

    public int getID() {
        return id;
    }

    public Genome getRepresentative() {
        return representative;
    }

    public void setRandomRepresentative() {
        representative = genomes.get((int) (Math.random() * genomes.size()));
    }

    public double getBestFitness() {
        return bestFitness;
    }

    public ArrayList<Genome> getGenomes() {
        return genomes;
    }

    public void setOffspringCount(int i) {
        offspringCount = i;
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

    public int getOffspringCount() {
        return offspringCount;
    }

    public boolean isStagnated() {
        return stagnated;
    }
}