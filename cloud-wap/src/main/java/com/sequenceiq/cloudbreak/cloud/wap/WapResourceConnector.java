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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.xml.parsers.ParserConfigurationException;

import org.omg.CORBA.PUBLIC_MEMBER;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import com.sequenceiq.cloudbreak.api.model.AdjustmentType;
import com.sequenceiq.cloudbreak.api.model.InstanceGroupType;
import com.sequenceiq.cloudbreak.cloud.ResourceConnector;
import com.sequenceiq.cloudbreak.cloud.context.AuthenticatedContext;
import com.sequenceiq.cloudbreak.cloud.model.CloudCredential;
import com.sequenceiq.cloudbreak.cloud.model.CloudInstance;
import com.sequenceiq.cloudbreak.cloud.model.CloudResource;
import com.sequenceiq.cloudbreak.cloud.model.CloudResourceStatus;
import com.sequenceiq.cloudbreak.cloud.model.CloudStack;
import com.sequenceiq.cloudbreak.cloud.model.Group;
import com.sequenceiq.cloudbreak.cloud.model.InstanceStatus;
import com.sequenceiq.cloudbreak.cloud.model.ResourceStatus;
import com.sequenceiq.cloudbreak.cloud.model.SecurityRule;
import com.sequenceiq.cloudbreak.cloud.notification.PersistenceNotifier;
import com.sequenceiq.cloudbreak.cloud.wap.client.WapClient;
import com.sequenceiq.cloudbreak.cloud.wap.util.WapPublicIP;
import com.sequenceiq.cloudbreak.cloud.wap.view.WapCredentialView;
import com.sequenceiq.cloudbreak.common.type.ResourceType;

import net.minidev.json.parser.ParseException;


@Service
public class WapResourceConnector implements ResourceConnector{

	
	private static final Logger LOGGER = LoggerFactory.getLogger(WapResourceConnector.class);
	@Inject
	WapClient wapClient;
	@Inject
	WapPublicIP wapNATRule;
	
	
	/*
	 * (non-Javadoc)
	 * @see com.sequenceiq.cloudbreak.cloud.ResourceConnector#launch(com.sequenceiq.cloudbreak.cloud.context.AuthenticatedContext, com.sequenceiq.cloudbreak.cloud.model.CloudStack, com.sequenceiq.cloudbreak.cloud.notification.PersistenceNotifier, com.sequenceiq.cloudbreak.api.model.AdjustmentType, java.lang.Long)
	 */
	@Override
	public List<CloudResourceStatus> launch(AuthenticatedContext authenticatedContext, CloudStack stack,
			PersistenceNotifier persistenceNotifier, AdjustmentType adjustmentType, Long threshold) throws Exception {
		// TODO Auto-generated method stub

		
		LOGGER.debug(stack.getParameters().toString());
		LOGGER.debug(stack.getImage().toString());
		LOGGER.debug(stack.getNetwork().toString());
		
		
		Map<String,Object> params = new HashMap<String,Object>();
		
		String vnetworkID = stack.getNetwork().getParameter("network_id", String.class);
		String public_ip = stack.getNetwork().getParameter("public_ip",String.class);
		
		LOGGER.debug(public_ip);
		
		
		
		List<CloudResourceStatus> resourceStatus = new ArrayList<>();
	
		CloudCredential credential = authenticatedContext.getCloudCredential();
        WapCredentialView wapCredential = new WapCredentialView(credential);
        String endpoint = wapCredential.getEndpoint();
        String subscriptionId = wapCredential.getSubscriptionId();  
        String password = wapCredential.getPassword();
        
		for(Group group :stack.getGroups()){
			int n = 1;
			for(CloudInstance instance : group.getInstances()){
	
				String id = wapClient.createVM(endpoint,subscriptionId,password,group.getName()+n);
				
				LOGGER.debug(id);
				
				if(group.getType().equals(InstanceGroupType.GATEWAY)){					
					List<String> natIDs = setSecurity(stack, vnetworkID, endpoint, subscriptionId, password, id);
					params.put("natIDs", (Object)natIDs);
				}
				
				
				CloudResourceStatus crs = getResourceStatus(authenticatedContext, persistenceNotifier, params,
						public_ip, instance, id);
				
				LOGGER.debug(crs.toString());
				
				resourceStatus.add(crs);
				n++;
			}
			
			
		}
		
		
		
		
		return resourceStatus;
	}

	/**
	 * @param authenticatedContext
	 * @param persistenceNotifier
	 * @param params
	 * @param public_ip
	 * @param instance
	 * @param id
	 * @return
	 */
	private CloudResourceStatus getResourceStatus(AuthenticatedContext authenticatedContext,
			PersistenceNotifier persistenceNotifier, Map<String, Object> params, String public_ip,
			CloudInstance instance, String id) {
		CloudResource resource;
		wapNATRule.setPublicIP(public_ip);
		resource = new CloudResource.Builder().type(ResourceType.WAP_TEMPLATE).name(id).reference(instance.getTemplate().getPrivateId().toString()).params(params).build();
		persistenceNotifier.notifyAllocation(resource, authenticatedContext.getCloudContext());
		CloudResourceStatus crs = new CloudResourceStatus(resource, ResourceStatus.IN_PROGRESS);
		return crs;
	}

	/**
	 * @param stack
	 * @param vnetworkID
	 * @param endpoint
	 * @param subscriptionId
	 * @param password
	 * @param id
	 * @return
	 * @throws UnrecoverableKeyException
	 * @throws KeyManagementException
	 * @throws MalformedURLException
	 * @throws KeyStoreException
	 * @throws NoSuchAlgorithmException
	 * @throws CertificateException
	 * @throws ProtocolException
	 * @throws IOException
	 * @throws ParseException
	 */
	private List<String> setSecurity(CloudStack stack, String vnetworkID, String endpoint, String subscriptionId,
			String password, String id)
			throws UnrecoverableKeyException, KeyManagementException, MalformedURLException, KeyStoreException,
			NoSuchAlgorithmException, CertificateException, ProtocolException, IOException, ParseException {
		List<SecurityRule> rules = stack.getSecurity().getRules();
		LOGGER.debug(stack.getSecurity().toString());
		int i = 0;
		LOGGER.debug(rules.toString());
		List<String> natIDs = new ArrayList<>();
		for(SecurityRule rule :rules){
			
			i++;
			int port= Integer.parseInt(rule.getPorts()[0]);
			String nameNATRule = "CBrule"+i;
			String natID = wapClient.addNATRule(endpoint, subscriptionId, password, id, port, nameNATRule,vnetworkID);
			natIDs.add(natID);
		}
		return natIDs;
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
			LOGGER.debug(resource.getParameter("public_ip", String.class));
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
		for(CloudResourceStatus resource : resourceStatus){
			LOGGER.debug("after"+resource.getCloudResource().getStringParameter("public_ip"));
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
        	//Doesnt work
			if (resource.getType().equals(ResourceType.WAP_GATEWAY)){
				List<String> natRules = resource.getParameter("natIDs", ArrayList.class);
				LOGGER.debug(natRules.toString());
				for(String natRule : natRules){
					wapClient.deleteNATRule(endpoint, subscriptionId, password, resource.getName(), natRule);
				}
			}
			
			wapClient.deleteVM(endpoint, subscriptionId, password, resource.getName());
			CloudResourceStatus cloudResourceStatus = new CloudResourceStatus(resource,ResourceStatus.DELETED);
			resourceStatus.add(cloudResourceStatus);
		}
		
		return resourceStatus;
	}

	@Override
	public List<CloudResourceStatus> update(AuthenticatedContext authenticatedContext, CloudStack stack,
			List<CloudResource> resources) throws Exception {

		return null;
	}

	/**
	 * Never tested
	 */
	@Override
	public List<CloudResourceStatus> upscale(AuthenticatedContext authenticatedContext, CloudStack stack,
			List<CloudResource> resources) throws Exception {
		CloudCredential credential = authenticatedContext.getCloudCredential();
        WapCredentialView wapCredential = new WapCredentialView(credential);
        String endpoint = wapCredential.getEndpoint();
        
        String subscriptionId = wapCredential.getSubscriptionId();  
        String password = wapCredential.getPassword();
        List<CloudResourceStatus> list = new ArrayList<>();
		for(Group group :stack.getGroups()){
			int n = 0;
			String groupName = group.getName();
			for(CloudInstance instance : group.getInstances()){
				if(instance.getTemplate().getStatus().equals(InstanceStatus.CREATE_REQUESTED)){
					String vmId = wapClient.createVM(endpoint, subscriptionId, password, groupName+n);
					CloudResource cloudResource = new CloudResource.Builder().name(vmId).reference(instance.getTemplate().getPrivateId().toString()).build();
					CloudResourceStatus cloudResourceStatus = new CloudResourceStatus(cloudResource, ResourceStatus.IN_PROGRESS);
					list.add(cloudResourceStatus);
					n++;
				}
				
			}
		}
		for(CloudResource resource : resources){
			list.add(new CloudResourceStatus(resource, ResourceStatus.UPDATED));
		}
		return list;
	}

	
	@Override
	public List<CloudResourceStatus> downscale(AuthenticatedContext authenticatedContext, CloudStack stack,
			List<CloudResource> resources, List<CloudInstance> vms) throws UnrecoverableKeyException, KeyManagementException, MalformedURLException, KeyStoreException, NoSuchAlgorithmException, CertificateException, ProtocolException, ParseException, IOException {
		CloudCredential credential = authenticatedContext.getCloudCredential();
        WapCredentialView wapCredential = new WapCredentialView(credential);
        String endpoint = wapCredential.getEndpoint();
        
        String subscriptionId = wapCredential.getSubscriptionId();  
        String password = wapCredential.getPassword();
        List<CloudResourceStatus> list = new ArrayList<>();
		for(CloudInstance vm :vms){
			wapClient.deleteVM(endpoint, subscriptionId, password, vm.getInstanceId());
		}
		return null;
	}
	
	
}
