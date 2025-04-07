package com.joshuadamian.neat.core.genome.genes.geneticencoding;

import com.joshuadamian.neat.config.Config;
import com.joshuadamian.neat.core.genome.genes.connectiongene.ConnectionGene;
import com.joshuadamian.neat.core.genome.genes.nodegene.*;
import com.joshuadamian.neat.core.genome.Genome;

import java.util.*;

public class GeneticEncoding {

    private Map<Integer, NodeGeneData> nodeGenesMap;
    private Map<Integer, ConnectionGeneData> connectionGenesMap;
    private NodeGeneData[] inputNodes = new NodeGeneData[0];
    private NodeGeneData[] outputNodes = new NodeGeneData[0];
    private NodeGeneData biasNode = null;
    private double fitness;
    private Config config;
    private int populationId;

    public GeneticEncoding(Config config, int populationId) {
        this.connectionGenesMap = new HashMap<>();
        this.nodeGenesMap = new HashMap<>();
        this.config = config;
        this.populationId = populationId;
    }

    public void loadGenome(Genome genome) {
        connectionGenesMap.clear();
        nodeGenesMap.clear();

        for (ConnectionGene connection : genome.getConnectionGenes()) {
            connectionGenesMap.put(
                    connection.getInnovationNumber(),
                    new ConnectionGeneData(
                            connection.getInNode().getId(),
                            connection.getOutNode().getId(),
                            connection.getWeight(),
                            connection.isEnabled(),
                            connection.getInnovationNumber(),
                            connection.isRecurrent()
                    )
            );
        }

        for (NodeGene node : genome.getNodeGenes()) {
            nodeGenesMap.put(
                    node.getId(),
                    new NodeGeneData(node.getId(), node.getNodeType())
            );
        }

        this.fitness = genome.getFitness();

        for (NodeGene node : genome.getNodeGenes()) {
            if (node instanceof InputNode) {
                NodeGeneData nodeGeneData = new NodeGeneData(node.getId(), node.getNodeType());
                inputNodes = Arrays.copyOf(inputNodes, inputNodes.length + 1);
                inputNodes[inputNodes.length - 1] = nodeGeneData;
            } else if (node instanceof OutputNode) {
                NodeGeneData nodeGeneData = new NodeGeneData(node.getId(), node.getNodeType());
                outputNodes = Arrays.copyOf(outputNodes, outputNodes.length + 1);
                outputNodes[outputNodes.length - 1] = nodeGeneData;
            } else if (node instanceof BiasNode) {
                NodeGeneData nodeGeneData = new NodeGeneData(node.getId(), node.getNodeType());
                biasNode = nodeGeneData;
            }
        }
    }

    public GeneticEncoding crossover(GeneticEncoding otherParent) {
        GeneticEncoding offspring = new GeneticEncoding(config, this.populationId);
        double thisFitness = this.getFitness();
        double otherFitness = otherParent.getFitness();

        GeneticEncoding bestParent = null;
        GeneticEncoding worstParent = null;
        if (thisFitness > otherFitness) {
            bestParent = this;
            worstParent = otherParent;
        } else if (otherFitness > thisFitness) {
            bestParent = otherParent;
            worstParent = this;
        } else {
            bestParent = this.getNumConnections() < otherParent.getNumConnections() ? this : otherParent;
            worstParent = this.getNumConnections() < otherParent.getNumConnections() ? otherParent : this;
        }

        for (Map.Entry<Integer, ConnectionGeneData> entry : bestParent.connectionGenesMap.entrySet()) {
            int innovationNumber = entry.getKey();
            ConnectionGeneData currentGene = entry.getValue();

            if (worstParent.hasInnovationNumber(innovationNumber)) {
                ConnectionGeneData parent2Gene = worstParent.getConnectionByInnovationNumber(innovationNumber);
                GeneticEncoding selectedParent = Math.random() < 0.5 ? bestParent : worstParent;
                ConnectionGeneData selectedGene = selectedParent.getConnectionByInnovationNumber(innovationNumber);
                boolean isEnabled =
                        !currentGene.isEnabled() || !parent2Gene.isEnabled()
                                ? Math.random() > config.getKeepDisabledOnCrossOverRate()
                                : true;

                if (config.getKeepDisabledOnCrossOverRate() == -1.0) {
                    isEnabled = selectedGene.isEnabled();
                }

                addConnectionAndNodes(offspring, selectedGene, isEnabled, selectedParent, bestParent);
            } else {
                boolean isEnabled =
                        !currentGene.isEnabled()
                                ? Math.random() > config.getKeepDisabledOnCrossOverRate()
                                : true;

                if (config.getKeepDisabledOnCrossOverRate() == -1.0) {
                    isEnabled = currentGene.isEnabled();
                }

                addConnectionAndNodes(offspring, currentGene, isEnabled, bestParent, bestParent);
            }
        }

        for (NodeGeneData inputNode : this.inputNodes) {
            offspring.addNode(inputNode);
        }
        for (NodeGeneData outputNode : this.outputNodes) {
            offspring.addNode(outputNode);
        }
        if (this.biasNode != null) {
            offspring.addNode(this.biasNode);
        }

        return offspring;
    }

    private void addConnectionAndNodes(GeneticEncoding offspring, ConnectionGeneData connection,
                                       boolean enabled, GeneticEncoding parent, GeneticEncoding bestParent) {
        NodeType outNodeType = parent.getNodeByID(connection.getOutNodeID()).getNodeType();
        if (outNodeType == NodeType.INPUT) {
            throw new IllegalStateException("Invalid connection: Input node " + connection.getOutNodeID()
                    + " cannot be used as out node.");
        }

        ConnectionGeneData bestParentConnection = bestParent.getConnectionByInnovationNumber(connection.getInnovationNumber());
        ConnectionGeneData newConnection = new ConnectionGeneData(
                connection.getInNodeID(),
                connection.getOutNodeID(),
                connection.getWeight(),
                enabled,
                connection.getInnovationNumber(),
                bestParentConnection.isRecurrent()
        );

        offspring.addConnection(newConnection);

        NodeGeneData inNode = new NodeGeneData(
                connection.getInNodeID(),
                parent.getNodeByID(connection.getInNodeID()).getNodeType()
        );

        NodeGeneData outNode = new NodeGeneData(
                connection.getOutNodeID(),
                parent.getNodeByID(connection.getOutNodeID()).getNodeType()
        );

        offspring.addNode(inNode);
        offspring.addNode(outNode);
    }

    public double calculateCompatibilityDistance(GeneticEncoding otherParent) {
        int disjointGenes = getNumberOfDisjointGenes(otherParent);
        int excessGenes = getNumberOfExcessGenes(otherParent);
        int maxGenes = Math.max(this.getNumConnections(), otherParent.getNumConnections());
        maxGenes = maxGenes < 20 ? 1 : maxGenes;
        return ((config.getC1() * excessGenes) / maxGenes)
                + ((config.getC2() * disjointGenes) / maxGenes)
                + (config.getC3() * calculateAverageWeightDifference(otherParent));
    }

    public int getNumberOfMatchingGenes(GeneticEncoding otherParent) {
        int matchingGenes = 0;
        for (Integer innovationNumber : this.connectionGenesMap.keySet()) {
            if (otherParent.hasInnovationNumber(innovationNumber)) {
                matchingGenes++;
            }
        }
        return matchingGenes;
    }

    public int getNumberOfDisjointGenes(GeneticEncoding otherParent) {
        int disjointGenes = 0;
        int maxInnovationSelf = this.getHighestInnovationNumber();
        int maxInnovationOther = otherParent.getHighestInnovationNumber();
        int comparisonLimit = Math.min(maxInnovationSelf, maxInnovationOther);

        for (Integer innovationNumber : this.connectionGenesMap.keySet()) {
            if (innovationNumber <= comparisonLimit && !otherParent.hasInnovationNumber(innovationNumber)) {
                disjointGenes++;
            }
        }

        for (Integer innovationNumber : otherParent.connectionGenesMap.keySet()) {
            if (innovationNumber <= comparisonLimit && !this.hasInnovationNumber(innovationNumber)) {
                disjointGenes++;
            }
        }

        return disjointGenes;
    }

    public int getNumberOfExcessGenes(GeneticEncoding otherParent) {
        int excessGenes = 0;
        int maxInnovationSelf = this.getHighestInnovationNumber();
        int maxInnovationOther = otherParent.getHighestInnovationNumber();
        GeneticEncoding largerParent = maxInnovationSelf > maxInnovationOther ? this : otherParent;
        int minInnovation = Math.min(maxInnovationSelf, maxInnovationOther);

        for (Integer innovationNumber : largerParent.connectionGenesMap.keySet()) {
            if (innovationNumber > minInnovation) {
                excessGenes++;
            }
        }
        return excessGenes;
    }

    public double calculateAverageWeightDifference(GeneticEncoding otherParent) {
        double totalWeightDifference = 0.0;
        int matchingGenesCount = 0;

        for (Map.Entry<Integer, ConnectionGeneData> entry : this.connectionGenesMap.entrySet()) {
            int innovationNumber = entry.getKey();
            ConnectionGeneData thisGene = entry.getValue();
            if (otherParent.hasInnovationNumber(innovationNumber)) {
                ConnectionGeneData otherGene = otherParent.getConnectionByInnovationNumber(innovationNumber);
                double weightDifference = Math.abs(thisGene.getWeight() - otherGene.getWeight());
                totalWeightDifference += weightDifference;
                matchingGenesCount++;
            }
        }

        if (matchingGenesCount == 0) {
            return 0.0;
        }
        return totalWeightDifference / matchingGenesCount;
    }

    public Genome buildGenome() {
        Map<Integer, NodeGene> newNodeGeneMap = new HashMap<>();
        ArrayList<NodeGene> newNodeGenes = new ArrayList<>();

        for (NodeGeneData oldNode : nodeGenesMap.values()) {
            NodeGene newNode = null;
            switch (oldNode.getNodeType()) {
                case INPUT:
                    newNode = new InputNode(oldNode.getId(), config);
                    break;
                case HIDDEN:
                    newNode = new HiddenNode(oldNode.getId(), config);
                    break;
                case OUTPUT:
                    newNode = new OutputNode(oldNode.getId(), config);
                    break;
                case BIAS:
                    newNode = new BiasNode(oldNode.getId(), config);
                    break;
            }

            if (newNode != null) {
                newNodeGeneMap.put(newNode.getId(), newNode);
                newNodeGenes.add(newNode);
            }
        }

        ArrayList<ConnectionGene> newConnectionGenes = new ArrayList<>();
        for (ConnectionGeneData oldConnection : connectionGenesMap.values()) {
            NodeGene newInNode = newNodeGeneMap.get(oldConnection.getInNodeID());
            NodeGene newOutNode = newNodeGeneMap.get(oldConnection.getOutNodeID());

            if (newInNode == null || newOutNode == null) {
                throw new IllegalStateException("Error: Node referenced in a connection does not exist.");
            }

            ConnectionGene newConnection = new ConnectionGene(
                    newInNode,
                    newOutNode,
                    oldConnection.getWeight(),
                    oldConnection.isEnabled(),
                    oldConnection.getInnovationNumber(),
                    oldConnection.isRecurrent(),
                    config
            );
            newConnectionGenes.add(newConnection);
        }

        Genome genome = new Genome(newNodeGenes, newConnectionGenes, config, populationId);
        genome.checkForRecurrentConnections();
        return genome;
    }

    private int getHighestInnovationNumber() {
        return connectionGenesMap.keySet().stream().max(Comparator.naturalOrder()).orElse(0);
    }

    private boolean hasInnovationNumber(int innovationNumber) {
        return connectionGenesMap.containsKey(innovationNumber);
    }

    private NodeGeneData getNodeByID(int id) {
        if (!nodeGenesMap.containsKey(id)) {
            throw new IllegalStateException("Error: Node with ID " + id + " does not exist.");
        }
        return nodeGenesMap.get(id);
    }

    private ConnectionGeneData getConnectionByInnovationNumber(int innovationNumber) {
        return connectionGenesMap.get(innovationNumber);
    }

    private void addConnection(ConnectionGeneData connection) {
        for (ConnectionGeneData c : connectionGenesMap.values()) {
            if (c.getInNodeID() == connection.getInNodeID() && c.getOutNodeID() == connection.getOutNodeID()) {
                return;
            }
        }
        connectionGenesMap.put(connection.getInnovationNumber(), connection);
    }

    private boolean hasNodeID(int nodeId) {
        return nodeGenesMap.containsKey(nodeId);
    }

    private void addNode(NodeGeneData node) {
        if (nodeGenesMap.containsKey(node.getId())) {
            return;
        }
        nodeGenesMap.put(node.getId(), node);
        switch (node.getNodeType()) {
            case INPUT:
                inputNodes = Arrays.copyOf(inputNodes, inputNodes.length + 1);
                inputNodes[inputNodes.length - 1] = node;
                break;
            case OUTPUT:
                outputNodes = Arrays.copyOf(outputNodes, outputNodes.length + 1);
                outputNodes[outputNodes.length - 1] = node;
                break;
            case BIAS:
                biasNode = node;
                break;
        }
    }

    private int getNumConnections() {
        return getConnections().length;
    }

    public double getFitness() {
        return fitness;
    }

    public ConnectionGeneData[] getConnections() {
        return connectionGenesMap.values().toArray(new ConnectionGeneData[0]);
    }
}