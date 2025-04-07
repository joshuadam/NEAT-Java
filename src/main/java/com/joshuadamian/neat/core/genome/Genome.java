package com.joshuadamian.neat.core.genome;

import com.joshuadamian.neat.util.StaticManager;
import com.joshuadamian.neat.config.Config;
import com.joshuadamian.neat.core.genome.genes.connectiongene.ConnectionGene;
import com.joshuadamian.neat.core.genome.genes.geneticencoding.GeneticEncoding;
import com.joshuadamian.neat.core.genome.genes.nodegene.*;
import com.joshuadamian.neat.util.trackers.*;
import com.joshuadamian.neat.util.trackers.innovationtracker.InnovationData;
import com.joshuadamian.neat.util.trackers.innovationtracker.InnovationTracker;
import com.joshuadamian.neat.util.trackers.innovationtracker.InnovationType;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class Genome {

    private int ID;
    private ArrayList<NodeGene> nodeGenes;
    private ArrayList<ConnectionGene> connectionGenes;
    private double fitness;
    private double adjustedFitness;
    private ArrayList<NodeGene> inputNodes = new ArrayList<>();
    private ArrayList<NodeGene> outputNodes = new ArrayList<>();
    private BiasNode biasNode;
    private Config config;
    private GenomeTracker genomeTracker;
    private NodeTracker nodeTracker;
    private InnovationTracker innovationTracker;
    private int populationId;

    public Genome(ArrayList<NodeGene> nodeGenes, ArrayList<ConnectionGene> connectionGenes, Config config, int populationId) {
        this.nodeGenes = nodeGenes;
        this.connectionGenes = connectionGenes;
        this.inputNodes = nodeGenes.stream()
                .filter(node -> node instanceof InputNode)
                .collect(Collectors.toCollection(ArrayList::new));
        this.outputNodes = nodeGenes.stream()
                .filter(node -> node instanceof OutputNode)
                .collect(Collectors.toCollection(ArrayList::new));
        this.biasNode = nodeGenes.stream()
                .filter(node -> node instanceof BiasNode)
                .map(node -> (BiasNode) node)
                .findFirst()
                .orElse(null);
        this.config = config;
        this.genomeTracker = StaticManager.getGenomeTracker(populationId);
        this.nodeTracker = StaticManager.getNodeTracker(populationId);
        this.innovationTracker = StaticManager.getInnovationTracker(populationId);
        this.ID = genomeTracker.getNextGenomeId();
        this.populationId = populationId;
    }

    public double[] propagate(double[] inputs) {
        calculateExpectedInputs();
        for (int i = 0; i < inputs.length; i++) {
            InputNode inputNode = (InputNode) getNodeById(i);
            inputNode.feedInput(inputs[i]);
        }
        double[] outputs = new double[outputNodes.size()];
        for (int i = 0; i < outputNodes.size(); i++) {
            OutputNode outputNode = (OutputNode) getNodeById(i + inputNodes.size());
            outputs[i] = outputNode.getLastOutput();
        }
        return outputs;
    }

    public void checkForRecurrentConnections() {
        for (ConnectionGene connection : connectionGenes) {
            boolean recurrent = checkIfConnectionIsRecurent(connection.getInNode(), connection.getOutNode());
            connection.setRecurrent(recurrent);
        }
    }

    public void calculateExpectedInputs() {
        for (NodeGene node : nodeGenes) {
            node.resetExpectedInputs();
            node.resetReceivedInputs();
        }
        for (ConnectionGene connection : connectionGenes) {
            connection.setForwardedExpectedInput(false);
        }
        for (NodeGene node : inputNodes) {
            InputNode inputnode = (InputNode) node;
            inputnode.calculateExpectedInputs();
        }
    }

    public void mutate() {
        Random weightRandom = new Random();
        Random connectionRandom = new Random();
        Random nodeRandom = new Random();

        double weightMutationRate = config.getWeightMutationRate();
        double addConnectionMutationRate = config.getAddConnectionMutationRate();
        double addNodeMutationRate = config.getAddNodeMutationRate();

        if (weightRandom.nextDouble() < weightMutationRate) {
            mutateWeight();
        }
        if (connectionRandom.nextDouble() < addConnectionMutationRate) {
            mutateAddConnection();
        }
        if (nodeRandom.nextDouble() < addNodeMutationRate) {
            mutateAddNode();
        }
    }

    public void mutateWeight() {
        double minWeight = config.getMinWeight();
        double maxWeight = config.getMaxWeight();

        for (ConnectionGene connection : connectionGenes) {
            double weight = connection.getWeight();
            double random = Math.random();

            if (random < 0.1) {
                double newWeight = config.getWeightInitialization().initializeWeight();
                newWeight = Math.max(minWeight, Math.min(newWeight, maxWeight));
                connection.setWeight(newWeight);
            } else {
                double perturbRange = config.getPerturbRange();
                double perturb = (Math.random() * 2 * perturbRange) - perturbRange;
                double newWeight = weight + perturb;
                newWeight = Math.max(minWeight, Math.min(newWeight, maxWeight));
                connection.setWeight(newWeight);
            }
        }
    }

    public void mutateAddConnection() {
        Random random = new Random();
        NodeGene fromNode;
        NodeGene toNode;
        int maxAttempts = 100;
        int attempts = 0;

        while (attempts < maxAttempts) {
            fromNode = nodeGenes.get(random.nextInt(nodeGenes.size()));
            toNode = nodeGenes.get(random.nextInt(nodeGenes.size()));

            if (!fromNode.acceptsOutgoingConnections() || !toNode.acceptsIncomingConnections()) {
                attempts++;
                continue;
            }

            boolean connectionExists = false;
            for (ConnectionGene connection : connectionGenes) {
                if (connection.getInNode() == fromNode && connection.getOutNode() == toNode) {
                    connectionExists = true;
                    break;
                }
            }

            if (connectionExists) {
                attempts++;
                continue;
            }

            boolean isRecurrent = checkIfConnectionIsRecurent(fromNode, toNode);
            double recurrentConnectionRate = config.getRecurrentConnectionRate();
            boolean allowRecurrentConnections = config.getAllowRecurrentConnections();

            if (isRecurrent) {
                if (!allowRecurrentConnections || random.nextDouble() > recurrentConnectionRate) {
                    attempts++;
                    continue;
                }
            }

            InnovationData innovationData = innovationTracker.trackInnovation(
                    fromNode.getId(),
                    toNode.getId()
            );

            ConnectionGene newConnection = new ConnectionGene(
                    fromNode,
                    toNode,
                    config.getWeightInitialization().initializeWeight(),
                    true,
                    innovationData.getInnovationNumber(),
                    isRecurrent,
                    config
            );

            connectionGenes.add(newConnection);
            break;
        }
    }

    public void mutateAddNode() {
        if (connectionGenes.isEmpty()) {
            return;
        }
        Random random = new Random();
        ConnectionGene selectedConnection = null;
        int maxAttempts = 100;
        int attempts = 0;

        while (attempts < maxAttempts) {
            ConnectionGene potentialConnection = connectionGenes.get(random.nextInt(connectionGenes.size()));
            if (potentialConnection.isEnabled()) {
                selectedConnection = potentialConnection;
                break;
            }
            attempts++;
        }

        if (selectedConnection == null) {
            return;
        }

        selectedConnection.setEnabled(false);
        Map<String, Object> innovations = innovationTracker.trackAddNodeInnovation(
                selectedConnection.getInNode(),
                selectedConnection.getOutNode(),
                nodeTracker
        );

        Integer newNodeId = (Integer) innovations.get("newNodeId");
        InnovationData inToNewInnovation = (InnovationData) innovations.get("inToNew");
        InnovationData newToOutInnovation = (InnovationData) innovations.get("newToOut");

        HiddenNode newNode = new HiddenNode(newNodeId, config);
        nodeGenes.add(newNode);

        ConnectionGene connection1 = new ConnectionGene(
                selectedConnection.getInNode(),
                newNode,
                1,
                true,
                inToNewInnovation.getInnovationNumber(),
                false,
                config
        );

        ConnectionGene connection2 = new ConnectionGene(
                newNode,
                selectedConnection.getOutNode(),
                selectedConnection.getWeight(),
                true,
                newToOutInnovation.getInnovationNumber(),
                selectedConnection.isRecurrent(),
                config
        );

        connectionGenes.add(connection1);
        connectionGenes.add(connection2);
    }

    public boolean checkIfConnectionIsRecurent(NodeGene fromNode, NodeGene toNode) {
        boolean recurrent = false;
        if (toNode == fromNode) {
            recurrent = true;
            return recurrent;
        }
        if (fromNode instanceof OutputNode) {
            recurrent = true;
            return recurrent;
        }
        Stack<NodeGene> stack = new Stack<>();
        Set<NodeGene> visited = new HashSet<>();
        stack.push(toNode);

        while (!stack.isEmpty()) {
            NodeGene currentNode = stack.pop();
            if (currentNode == fromNode) {
                recurrent = true;
                return recurrent;
            }
            visited.add(currentNode);
            for (ConnectionGene connection : currentNode.getOutgoingConnections()) {
                NodeGene nextNode = connection.getOutNode();
                if (!connection.isRecurrent() && !visited.contains(nextNode)) {
                    stack.push(nextNode);
                }
            }
        }
        return recurrent;
    }

    public void prune(boolean removeDisabledConnections) {
        if (removeDisabledConnections) {
            ArrayList<ConnectionGene> disabledConnections = new ArrayList<>();
            for (ConnectionGene conn : connectionGenes) {
                if (!conn.isEnabled()) {
                    disabledConnections.add(conn);
                }
            }

            for (ConnectionGene connection : disabledConnections) {
                NodeGene inNode = connection.getInNode();
                NodeGene outNode = connection.getOutNode();

                if (inNode.acceptsOutgoingConnections()) {
                    ArrayList<ConnectionGene> outgoingConnections = inNode.getOutgoingConnections();
                    outgoingConnections.remove(connection);
                }

                if (outNode.acceptsIncomingConnections()) {
                    ArrayList<ConnectionGene> incomingConnections = outNode.getIncomingConnections();
                    incomingConnections.remove(connection);

                    if (connection.isRecurrent() && outNode instanceof OutputNode) {
                        OutputNode outputNode = (OutputNode) outNode;
                        ArrayList<ConnectionGene> recurrentConnections = outputNode.getInComingRecurrentConnections();
                        if (recurrentConnections != null) {
                            recurrentConnections.remove(connection);
                        }
                    } else if (connection.isRecurrent() && outNode instanceof HiddenNode) {
                        HiddenNode hiddenNode = (HiddenNode) outNode;
                        ArrayList<ConnectionGene> recurrentConnections = hiddenNode.getInComingRecurrentConnections();
                        if (recurrentConnections != null) {
                            recurrentConnections.remove(connection);
                        }
                    }

                    if (outNode instanceof OutputNode) {
                        OutputNode outputNode = (OutputNode) outNode;
                        if (outputNode.getBiasConnection() == connection) {
                            outputNode.setBiasConnection(null);
                        }
                    } else if (outNode instanceof HiddenNode) {
                        HiddenNode hiddenNode = (HiddenNode) outNode;
                        if (hiddenNode.getBiasConnection() == connection) {
                            hiddenNode.setBiasConnection(null);
                        }
                    }
                }
            }

            connectionGenes.removeAll(disabledConnections);
        }

        boolean nodesPruned = true;

        while (nodesPruned) {
            nodesPruned = false;

            Iterator<NodeGene> nodeIterator = nodeGenes.iterator();
            while (nodeIterator.hasNext()) {
                NodeGene node = nodeIterator.next();

                if (!(node instanceof HiddenNode)) {
                    continue;
                }

                ArrayList<ConnectionGene> incomingConnections = node.getIncomingConnections();
                ArrayList<ConnectionGene> outgoingConnections = node.getOutgoingConnections();

                if (incomingConnections.isEmpty()) {
                    for (ConnectionGene conn : outgoingConnections) {
                        NodeGene targetNode = conn.getOutNode();
                        if (targetNode.acceptsIncomingConnections()) {
                            ArrayList<ConnectionGene> targetIncomingConnections = targetNode.getIncomingConnections();
                            targetIncomingConnections.remove(conn);

                            if (conn.isRecurrent() && targetNode instanceof OutputNode) {
                                OutputNode outputNode = (OutputNode) targetNode;
                                ArrayList<ConnectionGene> recurrentConnections = outputNode.getInComingRecurrentConnections();
                                if (recurrentConnections != null) {
                                    recurrentConnections.remove(conn);
                                }
                            } else if (conn.isRecurrent() && targetNode instanceof HiddenNode) {
                                HiddenNode hiddenNode = (HiddenNode) targetNode;
                                ArrayList<ConnectionGene> recurrentConnections = hiddenNode.getInComingRecurrentConnections();
                                if (recurrentConnections != null) {
                                    recurrentConnections.remove(conn);
                                }
                            }

                            if (targetNode instanceof OutputNode) {
                                OutputNode outputNode = (OutputNode) targetNode;
                                if (outputNode.getBiasConnection() == conn) {
                                    outputNode.setBiasConnection(null);
                                }
                            } else if (targetNode instanceof HiddenNode) {
                                HiddenNode hiddenNode = (HiddenNode) targetNode;
                                if (hiddenNode.getBiasConnection() == conn) {
                                    hiddenNode.setBiasConnection(null);
                                }
                            }
                        }
                    }

                    connectionGenes.removeAll(outgoingConnections);
                    nodeIterator.remove();
                    nodesPruned = true;
                }
                else if (outgoingConnections.isEmpty()) {
                    for (ConnectionGene conn : incomingConnections) {
                        NodeGene sourceNode = conn.getInNode();
                        if (sourceNode.acceptsOutgoingConnections()) {
                            ArrayList<ConnectionGene> sourceOutgoingConnections = sourceNode.getOutgoingConnections();
                            sourceOutgoingConnections.remove(conn);
                        }
                    }

                    connectionGenes.removeAll(incomingConnections);
                    nodeIterator.remove();
                    nodesPruned = true;
                }
            }
        }
    }

    public GeneticEncoding getGeneticEncoding() {
        GeneticEncoding geneticEncoding = new GeneticEncoding(config, populationId);
        geneticEncoding.loadGenome(this);
        return geneticEncoding;
    }

    public Genome crossover(Genome parent2) {
        GeneticEncoding parent1Encoding = this.getGeneticEncoding();
        GeneticEncoding parent2Encoding = parent2.getGeneticEncoding();
        GeneticEncoding childEncoding = parent1Encoding.crossover(parent2Encoding);
        return childEncoding.buildGenome();
    }

    public void calculateFitness() {
        fitness = config.getFitnessFunction().calculateFitness(this);
    }

    public void resetState() {
        for (NodeGene node : nodeGenes) {
            if (!(node instanceof BiasNode)) {
                node.resetState();
            }
        }
    }

    public boolean equalsGenome(Genome genome) {
        if (nodeGenes.size() != genome.getNodeGenes().size()
                || connectionGenes.size() != genome.getConnectionGenes().size()) {
            return false;
        }
        for (int i = 0; i < connectionGenes.size(); i++) {
            if (connectionGenes.get(i).getWeight() != genome.getConnectionGenes().get(i).getWeight()) {
                return false;
            }
        }
        return true;
    }

    public void reinitializeWeights() {
        for (ConnectionGene connection : connectionGenes) {
            connection.setWeight(config.getWeightInitialization().initializeWeight());
        }
    }

    public NodeGene getNodeById(int id) {
        for (NodeGene node : nodeGenes) {
            if (node.getId() == id) {
                return node;
            }
        }
        return null;
    }

    public Genome copy() {
        ArrayList<NodeGene> nodes = nodeGenes;
        ArrayList<ConnectionGene> connections = connectionGenes;

        ArrayList<NodeGene> newNodes = new ArrayList<>();
        ArrayList<ConnectionGene> newConnections = new ArrayList<>();
        Map<Integer, NodeGene> nodeMapping = new HashMap<>();

        for (NodeGene node : nodes) {
            NodeGene newNode = null;
            if (node instanceof InputNode) {
                newNode = new InputNode(node.getId(), config);
            } else if (node instanceof HiddenNode) {
                newNode = new HiddenNode(node.getId(), config);
            } else if (node instanceof OutputNode) {
                newNode = new OutputNode(node.getId(), config);
            } else if (node instanceof BiasNode) {
                newNode = new BiasNode(node.getId(), config);
            }
            if (newNode != null) {
                newNodes.add(newNode);
                nodeMapping.put(newNode.getId(), newNode);
            }
        }

        for (ConnectionGene connection : connections) {
            NodeGene originalInNode = connection.getInNode();
            NodeGene originalOutNode = connection.getOutNode();
            NodeGene newInNode = nodeMapping.get(originalInNode.getId());
            NodeGene newOutNode = nodeMapping.get(originalOutNode.getId());
            if (newInNode != null && newOutNode != null) {
                ConnectionGene newConnection = new ConnectionGene(
                        newInNode,
                        newOutNode,
                        connection.getWeight(),
                        connection.isEnabled(),
                        connection.getInnovationNumber(),
                        connection.isRecurrent(),
                        config
                );
                newConnections.add(newConnection);
            }
        }
        return new Genome(newNodes, newConnections, config, populationId);
    }


    public JSONObject toJSON() {
        JSONObject jsonGenome = new JSONObject();
        jsonGenome.put("id", this.ID);

        JSONArray nodeGenesArray = new JSONArray();
        for (NodeGene node : this.nodeGenes) {
            JSONObject nodeJson = new JSONObject();
            nodeJson.put("id", node.getId());
            nodeJson.put("type", getNodeTypeString(node));
            nodeGenesArray.put(nodeJson);
        }
        jsonGenome.put("nodeGenes", nodeGenesArray);

        JSONArray connectionGenesArray = new JSONArray();
        for (ConnectionGene connection : this.connectionGenes) {
            JSONObject connJson = new JSONObject();
            connJson.put("innovationNumber", connection.getInnovationNumber());
            connJson.put("inNodeId", connection.getInNode().getId());
            connJson.put("outNodeId", connection.getOutNode().getId());
            connJson.put("enabled", connection.isEnabled());
            connJson.put("weight", connection.getWeight());
            connJson.put("recurrent", connection.isRecurrent());
            connectionGenesArray.put(connJson);
        }
        jsonGenome.put("connectionGenes", connectionGenesArray);

        jsonGenome.put("fitness", this.fitness);
        jsonGenome.put("populationId", this.populationId);

        return jsonGenome;
    }

    private String getNodeTypeString(NodeGene node) {
        if (node instanceof InputNode) return "INPUT";
        if (node instanceof HiddenNode) return "HIDDEN";
        if (node instanceof OutputNode) return "OUTPUT";
        if (node instanceof BiasNode) return "BIAS";
        return "UNKNOWN";
    }

    public void saveGenome(String filePath) {
        try {
            JSONObject jsonGenome = this.toJSON();
            Files.write(Paths.get(filePath), jsonGenome.toString(4).getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Config getConfig() {
        return config;
    }

    public void setConfig(Config config) {
        this.config = config;
    }

    public int getID() {
        return ID;
    }

    public void setId(int id) {
        this.ID = id;
    }

    public double getFitness() {
        return fitness;
    }

    public void setFitness(double fitness) {
        this.fitness = fitness;
    }

    public double getAdjustedFitness() {
        return adjustedFitness;
    }

    public void setAdjustedFitness(double adjustedFitness) {
        this.adjustedFitness = adjustedFitness;
    }

    public ArrayList<NodeGene> getNodeGenes() {
        return nodeGenes;
    }

    public ArrayList<ConnectionGene> getConnectionGenes() {
        return connectionGenes;
    }
}