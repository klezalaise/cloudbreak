package com.sequenceiq.cloudbreak.cloud.wap;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.sequenceiq.cloudbreak.cloud.MetadataCollector;
import com.sequenceiq.cloudbreak.cloud.context.AuthenticatedContext;
import com.sequenceiq.cloudbreak.cloud.model.CloudCredential;
import com.sequenceiq.cloudbreak.cloud.model.CloudInstance;
import com.sequenceiq.cloudbreak.cloud.model.CloudInstanceMetaData;
import com.sequenceiq.cloudbreak.cloud.model.CloudResource;
import com.sequenceiq.cloudbreak.cloud.model.CloudVmInstanceStatus;
import com.sequenceiq.cloudbreak.cloud.model.CloudVmMetaDataStatus;
import com.sequenceiq.cloudbreak.cloud.model.InstanceStatus;
import com.sequenceiq.cloudbreak.cloud.wap.client.WapClient;
import com.sequenceiq.cloudbreak.cloud.wap.view.WapCredentialView;
import com.sequenceiq.cloudbreak.common.type.ResourceType;

@Service
public class WapMetadataCollector implements MetadataCollector{
	
	
	private static final Logger LOGGER = LoggerFactory.getLogger(WapMetadataCollector.class);

	@Inject
	private WapClient wapClient;

	@Override
	public List<CloudVmMetaDataStatus> collect(AuthenticatedContext authenticatedContext, List<CloudResource> resources,
			List<CloudInstance> vms) {
		CloudCredential credential = authenticatedContext.getCloudCredential();
        WapCredentialView wapCredential = new WapCredentialView(credential);
        String endpoint = wapCredential.getEndpoint();
        Map<String,CloudResource> privateIdMap = groupByPrivateId(resources);
        LOGGER.debug("PrivateIDMAp {}",privateIdMap.toString());
        String subscriptionId = wapCredential.getSubscriptionId();  
        String password = wapCredential.getPassword();
        List<CloudVmMetaDataStatus> listMdstatus = new ArrayList<>();
        for(CloudInstance vm : vms){
        	LOGGER.debug(vm.getTemplate().toString());
        }
        for (CloudInstance vm : vms){
        	LOGGER.debug(vm.getTemplate().getPrivateId().toString());
        	CloudResource cloudResource = privateIdMap.get(vm.getTemplate().getPrivateId().toString());
        	LOGGER.debug(cloudResource.toString());
        	CloudInstance cloudinstance = new CloudInstance(cloudResource.getName(), vm.getTemplate());
        	CloudVmInstanceStatus cloudVmInstanceStatus = new CloudVmInstanceStatus(cloudinstance, InstanceStatus.CREATED);
        	
        	CloudVmMetaDataStatus cloudVmMetaDataStatus = new CloudVmMetaDataStatus(cloudVmInstanceStatus, CloudInstanceMetaData.EMPTY_METADATA);
        	listMdstatus.add(cloudVmMetaDataStatus);
        }
     
        return listMdstatus;
	}
	
	private Map<String, CloudResource> groupByInstanceName(List<CloudResource> resources) {
        Map<String, CloudResource> instanceNameMap = new HashMap<>();
        for (CloudResource resource : resources) {
            if (ResourceType.WAP_TEMPLATE == resource.getType()) {
                String resourceName = resource.getName();
                instanceNameMap.put(resourceName, resource);
            }
        }
        return instanceNameMap;
    }
	
	private Map<String,CloudResource> groupByPrivateId (List<CloudResource> resources){
		Map<String,CloudResource> privateIdMap = new HashMap<>();
		for (CloudResource resource : resources){
			if(ResourceType.WAP_TEMPLATE == resource.getType()){
				String resourceRef = resource.getReference();
				LOGGER.debug(resourceRef);
				privateIdMap.put(resourceRef,resource);
			}
		}
		LOGGER.debug(privateIdMap.toString());
		return privateIdMap;
	}
	

}
