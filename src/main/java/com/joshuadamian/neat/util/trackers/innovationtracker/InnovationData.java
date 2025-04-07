package com.joshuadamian.neat.util.trackers.innovationtracker;

import com.joshuadamian.neat.core.genome.genes.nodegene.NodeGene;

public class InnovationData {
    private InnovationType innovationType;
    private int inNodeID;
    private int outNodeID;
    private int innovationNumber;

    public InnovationData(InnovationType innovationType, NodeGene inNode, NodeGene outNode) {
        this.innovationType = innovationType;
        this.inNodeID = inNode.getId();
        this.outNodeID = outNode.getId();
    }

    public InnovationData(InnovationType innovationType, int inNodeID, int outNodeID) {
        this.innovationType = innovationType;
        this.inNodeID = inNodeID;
        this.outNodeID = outNodeID;
    }

    public int getInnovationNumber() {
        return innovationNumber;
    }

    public void setInnovationNumber(int number) {
        innovationNumber = number;
    }
}