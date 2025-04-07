package com.joshuadamian.neat.util;

import com.joshuadamian.neat.core.genome.Genome;
import com.joshuadamian.neat.config.Config;
import com.joshuadamian.neat.core.genome.genes.connectiongene.ConnectionGene;
import com.joshuadamian.neat.core.genome.genes.nodegene.*;
import com.joshuadamian.neat.util.trackers.NodeTracker;
import com.joshuadamian.neat.util.trackers.innovationtracker.InnovationData;
import com.joshuadamian.neat.util.trackers.innovationtracker.InnovationTracker;
import com.joshuadamian.neat.util.trackers.innovationtracker.InnovationType;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GenomeBuilder {

    public static Genome buildGenome(Config config, int populationId) {
        int numInputs = config.getInputSize();
        int numOutputs = config.getOutputSize();

        NodeTracker nodeTracker = StaticManager.getNodeTracker(populationId);
        InnovationTracker innovationTracker = StaticManager.getInnovationTracker(populationId);

        NodeGene[] nodeGenes = new NodeGene[numInputs + numOutputs + 1];
        ConnectionGene[] connectionGenes = new ConnectionGene[(numInputs + 1) * numOutputs];

        for (int i = 0; i < numInputs; i++) {
            nodeGenes[i] = new InputNode(StaticManager.getNodeTracker(populationId).getNextNodeId(), config);
        }

        for (int i = 0; i < numOutputs; i++) {
            nodeGenes[numInputs + i] = new OutputNode(StaticManager.getNodeTracker(populationId).getNextNodeId(), config);
        }

        int biasIndex = numInputs + numOutputs;
        nodeGenes[biasIndex] = new BiasNode(StaticManager.getNodeTracker(populationId).getNextNodeId(), config);
        BiasNode biasNode = (BiasNode) nodeGenes[biasIndex];

        int connectionIndex = 0;
        for (int inputIdx = 0; inputIdx < numInputs; inputIdx++) {
            NodeGene inputNode = nodeGenes[inputIdx];

            for (int outputIdx = numInputs; outputIdx < numInputs + numOutputs; outputIdx++) {
                NodeGene outputNode = nodeGenes[outputIdx];
                InnovationData innovationData = innovationTracker.trackInnovation(
                        inputNode.getId(),
                        outputNode.getId()
                );

                connectionGenes[connectionIndex] = new ConnectionGene(
                        inputNode,
                        outputNode,
                        config.getWeightInitialization().initializeWeight(),
                        true,
                        innovationData.getInnovationNumber(),
                        false,
                        config
                );
                connectionIndex++;
            }
        }

        if (config.getConnectBias()) {
            for (int outputIdx = numInputs; outputIdx < numInputs + numOutputs; outputIdx++) {
                NodeGene outputNode = nodeGenes[outputIdx];
                InnovationData innovationData = innovationTracker.trackInnovation(
                        biasNode.getId(),
                        outputNode.getId()
                );

                connectionGenes[connectionIndex] = new ConnectionGene(
                        biasNode,
                        outputNode,
                        config.getWeightInitialization().initializeWeight(),
                        true,
                        innovationData.getInnovationNumber(),
                        false,
                        config
                );
                connectionIndex++;
            }
        }

        ArrayList<NodeGene> nodeGenesList = new ArrayList<>();
        for (NodeGene nodeGene : nodeGenes) {
            nodeGenesList.add(nodeGene);
        }

        ArrayList<ConnectionGene> connectionGenesList = new ArrayList<>();
        for (ConnectionGene connectionGene : connectionGenes) {
            connectionGenesList.add(connectionGene);
        }

        return new Genome(nodeGenesList, connectionGenesList, config, populationId);
    }

    public static Genome loadGenome(String filePath, Config config) {
        try {
            String content = new String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8);
            JSONObject jsonObject = new JSONObject(content);

            ArrayList<NodeGene> nodeGenes = new ArrayList<>();
            JSONArray nodeGenesArray = jsonObject.getJSONArray("nodeGenes");

            for (int i = 0; i < nodeGenesArray.length(); i++) {
                JSONObject nodeData = nodeGenesArray.getJSONObject(i);
                int id = nodeData.getInt("id");
                String type = nodeData.getString("type");

                NodeGene node = null;
                switch (type) {
                    case "INPUT":
                        node = new InputNode(id, config);
                        break;
                    case "HIDDEN":
                        node = new HiddenNode(id, config);
                        break;
                    case "OUTPUT":
                        node = new OutputNode(id, config);
                        break;
                    case "BIAS":
                        node = new BiasNode(id, config);
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown node type: " + type);
                }

                nodeGenes.add(node);
            }

            ArrayList<ConnectionGene> connectionGenes = new ArrayList<>();
            JSONArray connectionGenesArray = jsonObject.getJSONArray("connectionGenes");

            for (int i = 0; i < connectionGenesArray.length(); i++) {
                JSONObject connData = connectionGenesArray.getJSONObject(i);
                int inNodeId = connData.getInt("inNodeId");
                int outNodeId = connData.getInt("outNodeId");

                NodeGene inNode = findNodeById(nodeGenes, inNodeId);
                NodeGene outNode = findNodeById(nodeGenes, outNodeId);

                if (inNode == null || outNode == null) {
                    throw new IllegalStateException("Connection refers to a non-existing node");
                }

                ConnectionGene connection = new ConnectionGene(
                        inNode,
                        outNode,
                        connData.getDouble("weight"),
                        connData.getBoolean("enabled"),
                        connData.getInt("innovationNumber"),
                        connData.getBoolean("recurrent"),
                        config
                );

                connectionGenes.add(connection);
            }

            int populationId = jsonObject.getInt("populationId");
            Genome genome = new Genome(nodeGenes, connectionGenes, config, populationId);

            if (jsonObject.has("fitness")) {
                genome.setFitness(jsonObject.getDouble("fitness"));
            }

            if (jsonObject.has("id")) {
                genome.setId(jsonObject.getInt("id"));
            }

            return genome;
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static NodeGene findNodeById(List<NodeGene> nodes, int id) {
        for (NodeGene node : nodes) {
            if (node.getId() == id) {
                return node;
            }
        }
        return null;
    }
}