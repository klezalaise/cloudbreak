package com.sequenceiq.cloudbreak.cloud.wap;

import com.sequenceiq.cloudbreak.cloud.ResourceConnector;

import java.util.List;

import com.sequenceiq.cloudbreak.api.model.AdjustmentType;
import com.sequenceiq.cloudbreak.cloud.context.AuthenticatedContext;
import com.sequenceiq.cloudbreak.cloud.model.CloudInstance;
import com.sequenceiq.cloudbreak.cloud.model.CloudResource;
import com.sequenceiq.cloudbreak.cloud.model.CloudResourceStatus;
import com.sequenceiq.cloudbreak.cloud.model.CloudStack;
import com.sequenceiq.cloudbreak.cloud.notification.PersistenceNotifier;


public class WapResourceConnector implements ResourceConnector{
	
	public List<CloudResourceStatus> launch(AuthenticatedContext authenticatedContext, CloudStack stack, PersistenceNotifier persistenceNotifier,
            AdjustmentType adjustmentType, Long threshold) throws Exception{
		return null;
	}

   
    public List<CloudResourceStatus> check(AuthenticatedContext authenticatedContext, List<CloudResource> resources){
    	return null;
    }

   
    public List<CloudResourceStatus> terminate(AuthenticatedContext authenticatedContext, CloudStack stack, List<CloudResource> cloudResources) throws Exception{
    	return null;
    }
    
    public List<CloudResourceStatus> update(AuthenticatedContext authenticatedContext, CloudStack stack, List<CloudResource> resources) throws Exception{
    	return null;
    }
    
    public List<CloudResourceStatus> upscale(AuthenticatedContext authenticatedContext, CloudStack stack, List<CloudResource> resources) throws Exception{
    	return null;
    }
   
    public List<CloudResourceStatus> downscale(AuthenticatedContext authenticatedContext,
            CloudStack stack, List<CloudResource> resources, List<CloudInstance> vms) throws Exception{
    	return null;
    }

}