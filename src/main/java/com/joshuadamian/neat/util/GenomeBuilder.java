package com.joshuadamian.neat.util;

import com.joshuadamian.neat.core.genome.Genome;
import com.joshuadamian.neat.config.Config;
import com.joshuadamian.neat.core.genome.genes.connectiongene.ConnectionGene;
import com.joshuadamian.neat.core.genome.genes.nodegene.*;
import com.joshuadamian.neat.util.trackers.innovationtracker.InnovationData;
import com.joshuadamian.neat.util.trackers.innovationtracker.InnovationType;
import com.joshuadamian.neat.weightinitialization.ConstantWeightInitialization;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GenomeBuilder {

    public static Genome buildGenome(Config config, boolean connectBias) {
        int numInputs = config.getInputSize();
        int numOutputs = config.getOutputSize();

        NodeGene[] nodeGenes = new NodeGene[numInputs + numOutputs + 1];
        ConnectionGene[] connectionGenes = new ConnectionGene[(numInputs + 1) * numOutputs];

        for (int i = 0; i < numInputs; i++) {
            nodeGenes[i] = new InputNode(StaticManager.getNodeTracker(config).getNextNodeID(), config);
        }

        for (int i = 0; i < numOutputs; i++) {
            nodeGenes[numInputs + i] = new OutputNode(StaticManager.getNodeTracker(config).getNextNodeID(), config);
        }

        int biasIndex = numInputs + numOutputs;
        nodeGenes[biasIndex] = new BiasNode(StaticManager.getNodeTracker(config).getNextNodeID(), config);
        BiasNode biasNode = (BiasNode) nodeGenes[biasIndex];

        int connectionIndex = 0;
        for (int inputIdx = 0; inputIdx < numInputs; inputIdx++) {
            NodeGene inputNode = nodeGenes[inputIdx];

            for (int outputIdx = numInputs; outputIdx < numInputs + numOutputs; outputIdx++) {
                NodeGene outputNode = nodeGenes[outputIdx];
                InnovationData innovationData = StaticManager.getInnovationTracker(config)
                        .trackInnovation(InnovationType.addConnection, inputNode, outputNode);

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

        if (connectBias) {
            for (int outputIdx = numInputs; outputIdx < numInputs + numOutputs; outputIdx++) {
                NodeGene outputNode = nodeGenes[outputIdx];
                InnovationData innovationData = StaticManager.getInnovationTracker(config)
                        .trackInnovation(InnovationType.addConnection, biasNode, outputNode);

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

        return new Genome(nodeGenesList, connectionGenesList, config);
    }

    public static Genome loadGenome(String filePath, Config config) {
        try {
            String content = new String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8);
            JSONObject jsonObject = new JSONObject(content);

            JSONArray nodesArray = jsonObject.getJSONArray("nodes");
            Map<Integer, NodeGene> nodeMapping = new HashMap<>();
            ArrayList<NodeGene> newNodes = new ArrayList<>();
            ArrayList<ConnectionGene> newConnections = new ArrayList<>();

            for (int i = 0; i < nodesArray.length(); i++) {
                JSONObject nodeJson = nodesArray.getJSONObject(i);
                int id = nodeJson.getInt("id");
                String type = nodeJson.getString("type");
                double lastOutput = nodeJson.getDouble("lastOutput");

                NodeGene node = null;
                if (type.equals("InputNode")) {
                    node = new InputNode(id, config);
                } else if (type.equals("HiddenNode")) {
                    node = new HiddenNode(id, config);
                } else if (type.equals("OutputNode")) {
                    node = new OutputNode(id, config);
                } else if (type.equals("BiasNode")) {
                    node = new BiasNode(id, config);
                }

                if (node != null) {
                    node.setLastOutput(lastOutput);
                    newNodes.add(node);
                    nodeMapping.put(id, node);
                }
            }

            JSONArray connectionsArray = jsonObject.getJSONArray("connections");
            for (int i = 0; i < connectionsArray.length(); i++) {
                JSONObject connJson = connectionsArray.getJSONObject(i);
                int inNodeId = connJson.getInt("inNode");
                int outNodeId = connJson.getInt("outNode");
                double weight = connJson.getDouble("weight");
                boolean enabled = connJson.getBoolean("enabled");
                boolean recurrent = connJson.getBoolean("recurrent");
                int innovationNumber = connJson.getInt("innovationNumber");

                NodeGene inNode = nodeMapping.get(inNodeId);
                NodeGene outNode = nodeMapping.get(outNodeId);

                if (inNode != null && outNode != null) {
                    ConnectionGene connection = new ConnectionGene(
                            inNode,
                            outNode,
                            new ConstantWeightInitialization(weight).initializeWeight(),
                            enabled,
                            innovationNumber,
                            recurrent,
                            config
                    );
                    newConnections.add(connection);
                }
            }

            return new Genome(newNodes, newConnections, config);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}