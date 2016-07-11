package com.sequenceiq.cloudbreak.cloud.wap;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import com.sequenceiq.cloudbreak.api.model.AdjustmentType;
import com.sequenceiq.cloudbreak.cloud.ResourceConnector;
import com.sequenceiq.cloudbreak.cloud.context.AuthenticatedContext;
import com.sequenceiq.cloudbreak.cloud.model.CloudCredential;
import com.sequenceiq.cloudbreak.cloud.model.CloudInstance;
import com.sequenceiq.cloudbreak.cloud.model.CloudResource;
import com.sequenceiq.cloudbreak.cloud.model.CloudResourceStatus;
import com.sequenceiq.cloudbreak.cloud.model.CloudStack;
import com.sequenceiq.cloudbreak.cloud.model.Group;
import com.sequenceiq.cloudbreak.cloud.model.ResourceStatus;
import com.sequenceiq.cloudbreak.cloud.notification.PersistenceNotifier;
import com.sequenceiq.cloudbreak.cloud.wap.client.WapClient;
import com.sequenceiq.cloudbreak.cloud.wap.view.WapCredentialView;
import com.sequenceiq.cloudbreak.common.type.ResourceType;

import net.minidev.json.parser.ParseException;


@Service
public class WapResourceConnector implements ResourceConnector{

	
	private static final Logger LOGGER = LoggerFactory.getLogger(WapResourceConnector.class);
	@Inject
	WapClient wapClient;
	
	@Override
	public List<CloudResourceStatus> launch(AuthenticatedContext authenticatedContext, CloudStack stack,
			PersistenceNotifier persistenceNotifier, AdjustmentType adjustmentType, Long threshold) throws Exception {
		// TODO Auto-generated method stub
		for (Group group : stack.getGroups()){
			LOGGER.debug(group.getName());
		}
		LOGGER.debug(stack.getParameters().toString());
		LOGGER.debug(stack.getImage().toString());
		LOGGER.debug(stack.getNetwork().toString());
		
		List<CloudResourceStatus> resourceStatus = new ArrayList<>();

		CloudCredential credential = authenticatedContext.getCloudCredential();
        WapCredentialView wapCredential = new WapCredentialView(credential);
        String endpoint = wapCredential.getEndpoint();
        
        String subscriptionId = wapCredential.getSubscriptionId();  
        String password = wapCredential.getPassword();
        
		for(Group group :stack.getGroups()){
			
			LOGGER.debug(group.getName());
			LOGGER.debug(group.getType().toString());
			for(CloudInstance instance : group.getInstances()){
				LOGGER.debug(instance.getInstanceId());
				LOGGER.debug(instance.getTemplate().toString());
				String id = wapClient.createVM(endpoint,subscriptionId,password,group.getName());
				LOGGER.debug(id);
				CloudResource resource = new CloudResource.Builder().type(ResourceType.WAP_TEMPLATE).name(id).reference(instance.getTemplate().getPrivateId().toString()).build();
				persistenceNotifier.notifyAllocation(resource, authenticatedContext.getCloudContext());
				CloudResourceStatus crs = new CloudResourceStatus(resource, ResourceStatus.IN_PROGRESS);
				LOGGER.debug(crs.toString());
				resourceStatus.add(crs);
			}
			
			
		}
		
		
		
		
		return resourceStatus;
	}

	@Override
	public List<CloudResourceStatus> check(AuthenticatedContext authenticatedContext, List<CloudResource> resources) {
		CloudCredential credential = authenticatedContext.getCloudCredential();
        WapCredentialView wapCredential = new WapCredentialView(credential);
        String endpoint = wapCredential.getEndpoint();
        
        String subscriptionId = wapCredential.getSubscriptionId();  
        String password = wapCredential.getPassword();
        List<CloudResourceStatus> resourceStatus = new ArrayList<>();
        
		for (CloudResource resource : resources){
			String id = resource.getName();
			try {
				String status = wapClient.checkVM(endpoint, subscriptionId, id, password);
				if(status.equals("Running")){
					resourceStatus.add(new CloudResourceStatus(resource,ResourceStatus.CREATED));
				}else{
					resourceStatus.add(new CloudResourceStatus(resource,ResourceStatus.IN_PROGRESS));
				}
			} catch (UnrecoverableKeyException | KeyManagementException | KeyStoreException | NoSuchAlgorithmException
					| CertificateException | IOException | ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return resourceStatus;
	}

	@Override
	public List<CloudResourceStatus> terminate(AuthenticatedContext authenticatedContext, CloudStack stack,
			List<CloudResource> cloudResources) throws Exception {
		CloudCredential credential = authenticatedContext.getCloudCredential();
        WapCredentialView wapCredential = new WapCredentialView(credential);
        String endpoint = wapCredential.getEndpoint();
        
        String subscriptionId = wapCredential.getSubscriptionId();  
        String password = wapCredential.getPassword();
        List<CloudResourceStatus> resourceStatus = new ArrayList<>();
		for(CloudResource resource : cloudResources){
			wapClient.deleteVM(endpoint, subscriptionId, password, resource.getName());
			CloudResourceStatus cloudResourceStatus = new CloudResourceStatus(resource,ResourceStatus.DELETED);
			resourceStatus.add(cloudResourceStatus);
		}
		return resourceStatus;
	}

	@Override
	public List<CloudResourceStatus> update(AuthenticatedContext authenticatedContext, CloudStack stack,
			List<CloudResource> resources) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<CloudResourceStatus> upscale(AuthenticatedContext authenticatedContext, CloudStack stack,
			List<CloudResource> resources) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<CloudResourceStatus> downscale(AuthenticatedContext authenticatedContext, CloudStack stack,
			List<CloudResource> resources, List<CloudInstance> vms) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
	
}
