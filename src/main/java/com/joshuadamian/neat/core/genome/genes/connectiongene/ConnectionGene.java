package com.joshuadamian.neat.core.genome.genes.connectiongene;

import com.joshuadamian.neat.config.Config;
import com.joshuadamian.neat.core.genome.genes.nodegene.HiddenNode;
import com.joshuadamian.neat.core.genome.genes.nodegene.NodeGene;
import com.joshuadamian.neat.core.genome.genes.nodegene.OutputNode;

public class ConnectionGene {

    private NodeGene inNode;
    private NodeGene outNode;
    private double weight;
    private boolean enabled;
    private int innovationNumber;
    private boolean recurrent;
    private boolean forwardedExpectedInput = false;
    private Config config;

    public ConnectionGene(
            NodeGene inNode,
            NodeGene outNode,
            double weight,
            boolean enabled,
            int innovationNumber,
            boolean recurrent,
            Config config
    ) {
        this.inNode = inNode;
        this.outNode = outNode;
        this.weight = weight;
        this.enabled = enabled;
        this.innovationNumber = innovationNumber;
        this.recurrent = recurrent;
        this.config = config;
        inNode.addOutgoingConnection(this);
        outNode.addIncomingConnection(this);
    }

    public void feedForward(double input) {
        if (enabled && !recurrent) {
            outNode.feedInput(input * weight);
        }
    }

    public void forwardExpectedInput() {
        if (forwardedExpectedInput) {
            return;
        }
        if (outNode instanceof HiddenNode) {
            HiddenNode node = (HiddenNode) outNode;
            node.forwardExpectedInput();
        } else if (outNode instanceof OutputNode) {
            OutputNode node = (OutputNode) outNode;
            node.forwardExpectedInput();
        }
        forwardedExpectedInput = true;
    }

    public void reinitializeWeight() {
        this.weight = config.getWeightInitialization().initializeWeight();
    }

    public boolean isRecurrent() {
        return recurrent;
    }

    public NodeGene getInNode() {
        return inNode;
    }

    public NodeGene getOutNode() {
        return outNode;
    }

    public double getWeight() {
        return weight;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getInnovationNumber() {
        return innovationNumber;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setRecurrent(boolean recurrent) {
        this.recurrent = recurrent;
    }

    public void setForwardedExpectedInput(boolean forwardExpectedInput) {
        this.forwardedExpectedInput = forwardExpectedInput;
    }
}