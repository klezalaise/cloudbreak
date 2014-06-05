package com.sequenceiq.cloudbreak.converter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.controller.json.ClusterRequest;
import com.sequenceiq.cloudbreak.controller.json.ClusterResponse;
import com.sequenceiq.cloudbreak.controller.json.JsonHelper;
import com.sequenceiq.cloudbreak.domain.Blueprint;
import com.sequenceiq.cloudbreak.domain.Cluster;
import com.sequenceiq.cloudbreak.domain.Status;
import com.sequenceiq.cloudbreak.repository.BlueprintRepository;

@Component
public class ClusterConverter {

    private static final int HOURS_PER_DAY = 24;
    private static final int SECONDS_PER_MINUTE = 60;
    private static final int MILLIS_PER_SECOND = 1000;

    @Autowired
    private BlueprintRepository blueprintRepository;

    @Autowired
    private JsonHelper jsonHelper;

    public Cluster convert(ClusterRequest clusterRequest) {
        Cluster cluster = new Cluster();
        Blueprint blueprint = blueprintRepository.findOne(clusterRequest.getBlueprintId());
        cluster.setBlueprint(blueprint);
        cluster.setName(clusterRequest.getClusterName());
        cluster.setStatus(Status.REQUESTED);
        return cluster;
    }

    public ClusterResponse convert(Cluster cluster, String clusterJson) {
        ClusterResponse clusterResponse = new ClusterResponse();
        clusterResponse.setCluster(jsonHelper.createJsonFromString(clusterJson));
        clusterResponse.setId(cluster.getId());
        clusterResponse.setStatus(cluster.getStatus().name());
        if (cluster.getCreationFinished() != null) {
            long createFinished = cluster.getCreationFinished();
            int minutes = (int) ((createFinished / (MILLIS_PER_SECOND * SECONDS_PER_MINUTE)) % SECONDS_PER_MINUTE);
            int hours = (int) ((createFinished / (MILLIS_PER_SECOND * SECONDS_PER_MINUTE * SECONDS_PER_MINUTE)));
            clusterResponse.setHoursUp(hours);
            clusterResponse.setMinutesUp(minutes);
        } else {
            clusterResponse.setHoursUp(0);
            clusterResponse.setMinutesUp(0);
        }
        return clusterResponse;
    }

}