package com.joshuadamian.neat.core.genome.genes.geneticencoding;

import com.joshuadamian.neat.core.genome.genes.nodegene.NodeType;

class NodeGeneData {

    private int id;
    private NodeType nodeType;

    protected NodeGeneData(int id, NodeType nodeType) {
        this.id = id;
        this.nodeType = nodeType;
    }

    protected int getId() {
        return id;
    }

    protected NodeType getNodeType() {
        return nodeType;
    }
}