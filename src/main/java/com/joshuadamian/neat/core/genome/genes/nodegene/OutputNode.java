package com.joshuadamian.neat.core.genome.genes.nodegene;

import com.joshuadamian.neat.activationfunction.ActivationFunction;
import com.joshuadamian.neat.config.Config;
import com.joshuadamian.neat.core.genome.genes.connectiongene.ConnectionGene;

import java.util.ArrayList;

public class OutputNode extends NodeGene {

    private ArrayList<ConnectionGene> incomingConnections = new ArrayList<>();
    private ArrayList<ConnectionGene> inComingRecurrentConnections = new ArrayList<>();
    private ArrayList<ConnectionGene> outgoingConnections = new ArrayList<>();
    private ConnectionGene biasConnection = null;
    private ArrayList<Double> inputs = new ArrayList<>();
    private ActivationFunction activationFunction;

    public OutputNode(int id, Config config) {
        super(id, config);
        setNodeType(NodeType.OUTPUT);
        this.activationFunction = config.getActivationFunction();
    }

    @Override
    public void feedInput(double input) {
        inputs.add(input);
        incrementReceivedInputs();
        if (getReceivedInputs() == getExpectedInputs()) {
            activate(inputs);
        }
    }

    @Override
    public void activate(ArrayList<Double> inputs) {
        double sum = 0;
        for (int i = 0; i < inputs.size(); i++) {
            sum += inputs.get(i);
        }
        for (ConnectionGene connection : inComingRecurrentConnections) {
            if (connection.isEnabled()) {
                sum += connection.getInNode().getLastOutput() * connection.getWeight();
            }
        }
        if (biasConnection != null) {
            sum += biasConnection.getWeight() * biasConnection.getInNode().getLastOutput();
        }
        setLastOutput(activationFunction.apply(sum));
        this.inputs.clear();
        resetReceivedInputs();
    }

    public void forwardExpectedInput() {
        incrementExpectedInputs();
    }

    @Override
    public void addIncomingConnection(ConnectionGene connection) {
        incomingConnections.add(connection);
        if (connection.isRecurrent()) {
            inComingRecurrentConnections.add(connection);
        }
        if (connection.getInNode() instanceof BiasNode) {
            biasConnection = connection;
        }
    }

    @Override
    public void addOutgoingConnection(ConnectionGene connection) {
        outgoingConnections.add(connection);
    }

    @Override
    public ArrayList<ConnectionGene> getIncomingConnections() {
        return incomingConnections;
    }

    @Override
    public ArrayList<ConnectionGene> getOutgoingConnections() {
        return outgoingConnections;
    }

    @Override
    public boolean acceptsIncomingConnections() {
        return true;
    }

    @Override
    public boolean acceptsOutgoingConnections() {
        return true;
    }

    @Override
    public NodeType getNodeType() {
        return NodeType.OUTPUT;
    }

    public void setActivationFunction(ActivationFunction activationFunction) {
        this.activationFunction = activationFunction;
    }

    public ArrayList<ConnectionGene> getInComingRecurrentConnections() {
        return this.inComingRecurrentConnections;
    }

    public ConnectionGene getBiasConnection() {
        return this.biasConnection;
    }

    public void setBiasConnection(ConnectionGene connection) {
        this.biasConnection = connection;
    }
}