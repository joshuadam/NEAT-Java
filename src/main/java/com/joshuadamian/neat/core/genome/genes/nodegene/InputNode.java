package com.joshuadamian.neat.core.genome.genes.nodegene;

import com.joshuadamian.neat.config.Config;
import com.joshuadamian.neat.core.genome.genes.connectiongene.ConnectionGene;

import java.util.ArrayList;

public class InputNode extends NodeGene {

    private ArrayList<ConnectionGene> outgoingConnections = new ArrayList<>();

    public InputNode(int id, Config config) {
        super(id, config);
        setNodeType(NodeType.INPUT);
    }

    @Override
    public void feedInput(double input) {
        activate(input);
    }

    public void activate(double input) {
        for (ConnectionGene connection : outgoingConnections) {
            connection.feedForward(input);
            setLastOutput(input);
        }
    }

    @Override
    public void activate(ArrayList<Double> inputs) {
        throw new UnsupportedOperationException("Input node should receive only one input at a time.");
    }

    public void calculateExpectedInputs() {
        for (ConnectionGene connection : outgoingConnections) {
            if (connection.isEnabled() && !connection.isRecurrent()) {
                connection.forwardExpectedInput();
            }
        }
    }

    @Override
    public void addOutgoingConnection(ConnectionGene connection) {
        outgoingConnections.add(connection);
    }

    @Override
    public ArrayList<ConnectionGene> getOutgoingConnections() {
        return outgoingConnections;
    }

    @Override
    public boolean acceptsOutgoingConnections() {
        return true;
    }

    @Override
    public NodeType getNodeType() {
        return NodeType.INPUT;
    }
}