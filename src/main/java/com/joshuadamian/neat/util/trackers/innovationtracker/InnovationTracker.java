package com.joshuadamian.neat.util.trackers.innovationtracker;

import com.joshuadamian.neat.core.genome.genes.nodegene.NodeGene;

import java.util.HashMap;
import java.util.Map;

public class InnovationTracker {
    private Map<String, InnovationData> innovationMap;
    private int innovationCounter = 0;

    public InnovationTracker() {
        innovationMap = new HashMap<>();
    }

    public void reset() {
        innovationMap.clear();
    }

    public InnovationData trackInnovation(InnovationType innovationType, NodeGene inNode, NodeGene outNode) {
        String mutationKey = generateMutationKey(innovationType, inNode.getId(), outNode.getId());

        if (innovationMap.containsKey(mutationKey)) {
            return innovationMap.get(mutationKey);
        } else {
            InnovationData innovationData = new InnovationData(innovationType, inNode, outNode);
            innovationData.setInnovationNumber(innovationCounter);
            innovationMap.put(mutationKey, innovationData);
            if(innovationType == innovationType.addNode) {
                innovationCounter+=2;
            } else {
                innovationCounter++;
            }
            return innovationData;
        }
    }

    private String generateMutationKey(InnovationType innovationType, int inNodeID, int outNodeID) {
        return innovationType + "-" + inNodeID + "-" + outNodeID;
    }
}
