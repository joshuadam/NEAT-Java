package com.joshuadamian.neat.core.genome.genes.nodegene;

import com.joshuadamian.neat.config.Config;
import com.joshuadamian.neat.core.genome.genes.connectiongene.ConnectionGene;

import java.util.ArrayList;

public class BiasNode extends NodeGene {

    private double bias;
    private ArrayList<ConnectionGene> outgoingConnections = new ArrayList<>();

    public BiasNode(int id, Config config) {
        super(id, config);
        setNodeType(NodeType.BIAS);
        this.bias = getConfig().getBiasInitialization().initializeBias();
        setLastOutput(bias);
    }

    @Override
    public void activate(ArrayList<Double> inputs) {
        throw new UnsupportedOperationException("Bias node can not be activated");
    }

    @Override
    public void feedInput(double input) {
        throw new UnsupportedOperationException("Bias node does not have any input");
    }

    @Override
    public void addOutgoingConnection(ConnectionGene connection) {
        outgoingConnections.add(connection);
    }

    @Override
    public boolean acceptsOutgoingConnections() {
        return true;
    }

    @Override
    public NodeType getNodeType() {
        return NodeType.BIAS;
    }

    @Override
    public ArrayList<ConnectionGene> getOutgoingConnections() {
        return outgoingConnections;
    }
}