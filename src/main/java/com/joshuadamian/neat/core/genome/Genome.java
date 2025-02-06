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
import com.joshuadamian.neat.weightinitialization.ConstantWeightInitialization;
import org.json.JSONArray;
import org.json.JSONException;
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

    public Genome(ArrayList<NodeGene> nodeGenes, ArrayList<ConnectionGene> connectionGenes, Config config) {
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
        this.genomeTracker = StaticManager.getGenomeTracker(config);
        this.nodeTracker = StaticManager.getNodeTracker(config);
        this.innovationTracker = StaticManager.getInnovationTracker(config);
        this.ID = genomeTracker.getNextGenomeID();
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
                    InnovationType.addConnection,
                    fromNode,
                    toNode
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
        HiddenNode newNode = new HiddenNode(nodeTracker.getNextNodeID(), config);
        nodeGenes.add(newNode);

        InnovationData innovationData = innovationTracker.trackInnovation(
                InnovationType.addNode,
                selectedConnection.getInNode(),
                selectedConnection.getOutNode()
        );

        ConnectionGene connection1 = new ConnectionGene(
                selectedConnection.getInNode(),
                newNode,
                new ConstantWeightInitialization(1.0).initializeWeight(),
                true,
                innovationData.getInnovationNumber(),
                false,
                config
        );

        ConnectionGene connection2 = new ConnectionGene(
                newNode,
                selectedConnection.getOutNode(),
                new ConstantWeightInitialization(selectedConnection.getWeight()).initializeWeight(),
                true,
                innovationData.getInnovationNumber() + 1,
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

    public void prune() {
        boolean nodesPruned = true;
        while (nodesPruned) {
            nodesPruned = false;
            Iterator<NodeGene> nodeIterator = nodeGenes.iterator();
            while (nodeIterator.hasNext()) {
                NodeGene node = nodeIterator.next();
                if (node instanceof HiddenNode) {
                    if (node.getIncomingConnections().isEmpty()) {
                        nodeIterator.remove();
                        nodesPruned = true;
                        ArrayList<ConnectionGene> outgoingConnections = node.getOutgoingConnections();
                        connectionGenes.removeAll(outgoingConnections);
                    } else if (node.getOutgoingConnections().isEmpty()) {
                        nodeIterator.remove();
                        nodesPruned = true;
                        ArrayList<ConnectionGene> incomingConnections = node.getIncomingConnections();
                        connectionGenes.removeAll(incomingConnections);
                    }
                }
            }
        }
    }

    public GeneticEncoding getGeneticEncoding() {
        GeneticEncoding geneticEncoding = new GeneticEncoding(config);
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
                        new ConstantWeightInitialization(connection.getWeight()).initializeWeight(),
                        connection.isEnabled(),
                        connection.getInnovationNumber(),
                        connection.isRecurrent(),
                        config
                );
                newConnections.add(newConnection);
            }
        }
        return new Genome(newNodes, newConnections, config);
    }

    public void saveGenome(String filePath) {
        try {
            JSONObject jsonObject = new JSONObject();
            JSONArray nodesArray = new JSONArray();
            JSONArray connectionsArray = new JSONArray();

            for (NodeGene node : nodeGenes) {
                JSONObject nodeJson = new JSONObject();
                nodeJson.put("id", node.getId());
                nodeJson.put("type", node.getClass().getSimpleName());
                nodeJson.put("lastOutput", node.getLastOutput());
                nodesArray.put(nodeJson);
            }

            for (ConnectionGene connection : connectionGenes) {
                JSONObject connJson = new JSONObject();
                connJson.put("inNode", connection.getInNode().getId());
                connJson.put("outNode", connection.getOutNode().getId());
                connJson.put("weight", connection.getWeight());
                connJson.put("enabled", connection.isEnabled());
                connJson.put("recurrent", connection.isRecurrent());
                connJson.put("innovationNumber", connection.getInnovationNumber());
                connectionsArray.put(connJson);
            }

            jsonObject.put("nodes", nodesArray);
            jsonObject.put("connections", connectionsArray);

            Files.write(Paths.get(filePath), jsonObject.toString(4).getBytes(StandardCharsets.UTF_8));
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

    public double getFitness() {
        return fitness;
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