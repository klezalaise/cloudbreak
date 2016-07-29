package com.sequenceiq.cloudbreak.cloud.wap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import com.sequenceiq.cloudbreak.cloud.InstanceConnector;
import com.sequenceiq.cloudbreak.cloud.context.AuthenticatedContext;
import com.sequenceiq.cloudbreak.cloud.exception.CloudOperationNotSupportedException;
import com.sequenceiq.cloudbreak.cloud.model.CloudCredential;
import com.sequenceiq.cloudbreak.cloud.model.CloudInstance;
import com.sequenceiq.cloudbreak.cloud.model.CloudResource;
import com.sequenceiq.cloudbreak.cloud.model.CloudVmInstanceStatus;
import com.sequenceiq.cloudbreak.cloud.model.InstanceStatus;
import com.sequenceiq.cloudbreak.cloud.wap.client.WapClient;
import com.sequenceiq.cloudbreak.cloud.wap.view.WapCredentialView;
import com.sequenceiq.cloudbreak.common.type.ResourceType;


@Service
public class WapInstanceConnector implements InstanceConnector{

	@Inject
	WapClient wapClient;
	
	@Override
	public List<CloudVmInstanceStatus> start(AuthenticatedContext authenticatedContext, List<CloudResource> resources,
			List<CloudInstance> vms) throws Exception {
		CloudCredential cloudCredential = authenticatedContext.getCloudCredential();
		WapCredentialView wapCredentialView = new WapCredentialView(cloudCredential);
		String endpoint = wapCredentialView.getEndpoint();
		String password = wapCredentialView.getPassword();
		String subscription = wapCredentialView.getSubscriptionId();
		List<CloudVmInstanceStatus> list= new ArrayList<>();
        Map<String,CloudResource> privateIdMap = groupByPrivateId(resources);

		for(CloudInstance instance : vms){
			CloudResource resource = privateIdMap.get(instance.getTemplate().getPrivateId().toString());
			String instanceName = resource.getName();
			wapClient.startVM(endpoint, subscription, password, instanceName);
			CloudVmInstanceStatus cloudVmInstanceStatus = new CloudVmInstanceStatus(instance, InstanceStatus.STARTED);
			list.add(cloudVmInstanceStatus);
		}
		
		return list;
	}

	@Override
	public List<CloudVmInstanceStatus> stop(AuthenticatedContext authenticatedContext, List<CloudResource> resources,
			List<CloudInstance> vms) throws Exception {
		CloudCredential cloudCredential = authenticatedContext.getCloudCredential();
		WapCredentialView wapCredentialView = new WapCredentialView(cloudCredential);
		String endpoint = wapCredentialView.getEndpoint();
		String password = wapCredentialView.getPassword();
		String subscription = wapCredentialView.getSubscriptionId();
		List<CloudVmInstanceStatus> list= new ArrayList<>();
        Map<String,CloudResource> privateIdMap = groupByPrivateId(resources);

		for(CloudInstance instance : vms){
			CloudResource resource = privateIdMap.get(instance.getTemplate().getPrivateId().toString());
			String instanceName = resource.getName();
			wapClient.stopVM(endpoint, subscription, password, instanceName);
			CloudVmInstanceStatus cloudVmInstanceStatus = new CloudVmInstanceStatus(instance, InstanceStatus.STOPPED);
			list.add(cloudVmInstanceStatus);
		}
		
		return list;
		
		
	}

	@Override
	public List<CloudVmInstanceStatus> check(AuthenticatedContext authenticatedContext, List<CloudInstance> vms) {
		
		return null;
	}

	@Override
	public String getConsoleOutput(AuthenticatedContext authenticatedContext, CloudInstance vm) {
		// TODO Auto-generated method stub
		throw new CloudOperationNotSupportedException("WAP doesn't provide access to the VM console output yet.");
	}
	private Map<String,CloudResource> groupByPrivateId (List<CloudResource> resources){
		Map<String,CloudResource> privateIdMap = new HashMap<>();
		for (CloudResource resource : resources){
			if(ResourceType.WAP_TEMPLATE == resource.getType()){
				String resourceRef = resource.getReference();
				privateIdMap.put(resourceRef,resource);
			}
		}
		return privateIdMap;
	}

}
