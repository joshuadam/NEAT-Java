package com.joshuadamian.neat.util.trackers.innovationtracker;

import com.joshuadamian.neat.core.genome.genes.nodegene.NodeGene;
import com.joshuadamian.neat.util.trackers.NodeTracker;
import java.util.HashMap;
import java.util.Map;

public class InnovationTracker {
    private Map<String, Object> innovationMap;
    private int innovationCounter = 0;

    public InnovationTracker() {
        innovationMap = new HashMap<>();
    }

    public void reset() {
        innovationMap.clear();
    }

    public InnovationData trackInnovation(int inNodeId, int outNodeId) {
        String mutationKey = generateMutationKey(InnovationType.addConnection, inNodeId, outNodeId);

        if (innovationMap.containsKey(mutationKey) && innovationMap.get(mutationKey) instanceof InnovationData) {
            return (InnovationData) innovationMap.get(mutationKey);
        } else {
            InnovationData innovationData = new InnovationData(InnovationType.addConnection, inNodeId, outNodeId);
            innovationData.setInnovationNumber(innovationCounter);
            innovationMap.put(mutationKey, innovationData);
            innovationCounter++;
            return innovationData;
        }
    }

    public Map<String, Object> trackAddNodeInnovation(NodeGene inNode, NodeGene outNode, NodeTracker nodeTracker) {
        String mutationKey = generateMutationKey(InnovationType.addNode, inNode.getId(), outNode.getId());

        Integer newNodeId;
        if (innovationMap.containsKey(mutationKey)) {
            newNodeId = (Integer) innovationMap.get(mutationKey);
        } else {
            newNodeId = nodeTracker.getNextNodeId();
            innovationMap.put(mutationKey, newNodeId);
        }

        int sourceNodeId = inNode.getId();
        int targetNodeId = outNode.getId();

        InnovationData inToNewInnovation = trackInnovation(sourceNodeId, newNodeId);
        InnovationData newToOutInnovation = trackInnovation(newNodeId, targetNodeId);

        Map<String, Object> result = new HashMap<>();
        result.put("inToNew", inToNewInnovation);
        result.put("newToOut", newToOutInnovation);
        result.put("newNodeId", newNodeId);

        return result;
    }

    private String generateMutationKey(InnovationType innovationType, int inNodeID, int outNodeID) {
        return innovationType + "-" + inNodeID + "-" + outNodeID;
    }
}