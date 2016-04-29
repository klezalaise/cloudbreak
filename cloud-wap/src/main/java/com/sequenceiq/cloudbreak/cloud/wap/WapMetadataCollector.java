package com.sequenceiq.cloudbreak.cloud.wap;

import com.sequenceiq.cloudbreak.cloud.MetadataCollector;

import java.util.List;

import com.sequenceiq.cloudbreak.cloud.context.AuthenticatedContext;
import com.sequenceiq.cloudbreak.cloud.model.CloudInstance;
import com.sequenceiq.cloudbreak.cloud.model.CloudResource;
import com.sequenceiq.cloudbreak.cloud.model.CloudVmMetaDataStatus;

public class WapMetadataCollector implements MetadataCollector{

	@Override
    public List<CloudVmMetaDataStatus> collect(AuthenticatedContext authenticatedContext, List<CloudResource> resources, List<CloudInstance> vms){
    	return null;
    }
}