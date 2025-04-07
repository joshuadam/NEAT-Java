package com.joshuadamian.neat.core.genome.genes.geneticencoding;

class ConnectionGeneData {

    private int inNodeID;
    private int outNodeID;
    private double weight;
    private boolean enabled;
    private int innovationNumber;
    private boolean recurrent;

    protected ConnectionGeneData(int inNodeID, int outNodeID, double weight, boolean enabled, int innovationNumber, boolean recurrent) {
        this.inNodeID = inNodeID;
        this.outNodeID = outNodeID;
        this.weight = weight;
        this.enabled = enabled;
        this.innovationNumber = innovationNumber;
        this.recurrent = recurrent;
    }

    protected int getInNodeID() {
        return inNodeID;
    }

    protected int getOutNodeID() {
        return outNodeID;
    }

    protected double getWeight() {
        return weight;
    }

    protected boolean isEnabled() {
        return enabled;
    }

    protected int getInnovationNumber() {
        return innovationNumber;
    }

    protected boolean isRecurrent() {
        return recurrent;
    }
}