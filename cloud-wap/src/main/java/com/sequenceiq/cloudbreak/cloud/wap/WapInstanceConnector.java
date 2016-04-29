package com.sequenceiq.cloudbreak.cloud.wap;

import com.sequenceiq.cloudbreak.cloud.InstanceConnector;

import java.util.List;

import com.sequenceiq.cloudbreak.cloud.context.AuthenticatedContext;
import com.sequenceiq.cloudbreak.cloud.model.CloudInstance;
import com.sequenceiq.cloudbreak.cloud.model.CloudResource;
import com.sequenceiq.cloudbreak.cloud.model.CloudVmInstanceStatus;


public class WapInstanceConnector implements InstanceConnector{

	@Override
    public List<CloudVmInstanceStatus> start(AuthenticatedContext authenticatedContext, List<CloudResource> resources, List<CloudInstance> vms) throws Exception{
		return null;
	}
    
	@Override
    public List<CloudVmInstanceStatus> stop(AuthenticatedContext authenticatedContext, List<CloudResource> resources, List<CloudInstance> vms) throws Exception{
		return null;
	}
    
	@Override
	public List<CloudVmInstanceStatus> check(AuthenticatedContext authenticatedContext, List<CloudInstance> vms){
		return null;
	}
    
	@Override
	public String getConsoleOutput(AuthenticatedContext authenticatedContext, CloudInstance vm){
		return null;
	}

}