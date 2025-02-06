package com.joshuadamian.neat.core.genome.genes.nodegene;

import com.joshuadamian.neat.config.Config;
import com.joshuadamian.neat.core.genome.genes.connectiongene.ConnectionGene;

import java.util.ArrayList;

public abstract class NodeGene {

    private int id;
    private NodeType nodeType;
    private double lastOutput = 0;
    private ArrayList<Double> inputs = new ArrayList<>();
    private int expectedInputs = 0;
    private int receivedInputs = 0;
    private Config config;

    public NodeGene(int id, Config config) {
        this.id = id;
        this.config = config;
    }

    public abstract void activate(ArrayList<Double> inputs);

    public abstract void feedInput(double input);

    public void resetState() {
        this.lastOutput = 0.0;
        this.expectedInputs = 0;
        this.receivedInputs = 0;
    }

    public void addIncomingConnection(ConnectionGene connection) {
        throw new UnsupportedOperationException("This node does not support incoming connections.");
    }

    public void addOutgoingConnection(ConnectionGene connection) {
        throw new UnsupportedOperationException("This node does not support outgoing connections.");
    }

    public ArrayList<ConnectionGene> getIncomingConnections() {
        throw new UnsupportedOperationException("This node does not support incoming connections.");
    }

    public ArrayList<ConnectionGene> getOutgoingConnections() {
        throw new UnsupportedOperationException("This node does not support outgoing connections.");
    }

    public boolean acceptsIncomingConnections() {
        return false;
    }

    public boolean acceptsOutgoingConnections() {
        return false;
    }

    public int getId() {
        return id;
    }

    public Config getConfig() {
        return config;
    }

    public void setConfig(Config config) {
        this.config = config;
    }

    public double getLastOutput() {
        return lastOutput;
    }

    public void setLastOutput(double lastOutput) {
        this.lastOutput = lastOutput;
    }

    public int getExpectedInputs() {
        return expectedInputs;
    }

    public void incrementExpectedInputs() {
        expectedInputs++;
    }

    public void resetExpectedInputs() {
        expectedInputs = 0;
    }

    public int getReceivedInputs() {
        return receivedInputs;
    }

    public void incrementReceivedInputs() {
        receivedInputs++;
    }

    public void resetReceivedInputs() {
        receivedInputs = 0;
    }

    public NodeType getNodeType() {
        return nodeType;
    }

    public void setNodeType(NodeType nodeType) {
        this.nodeType = nodeType;
    }
}