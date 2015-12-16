package com.sequenceiq.cloudbreak.service.stack.event;

import java.util.Set;

import com.sequenceiq.cloudbreak.cloud.model.Platform;
import com.sequenceiq.cloudbreak.domain.Resource;

public class ProvisionComplete extends ProvisionEvent {

    private Set<Resource> resources;

    public ProvisionComplete(Platform cloudPlatform, Long stackId, Set<Resource> resources) {
        super(cloudPlatform, stackId);
        this.resources = resources;
    }

    public Set<Resource> getResources() {
        return resources;
    }

    public void setResources(Set<Resource> resources) {
        this.resources = resources;
    }

}
