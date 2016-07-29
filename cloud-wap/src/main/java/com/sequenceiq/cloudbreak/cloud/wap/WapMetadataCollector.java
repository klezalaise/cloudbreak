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
import com.sequenceiq.cloudbreak.cloud.wap.util.WapPublicIP;
import com.sequenceiq.cloudbreak.cloud.wap.view.WapCredentialView;

import net.minidev.json.parser.ParseException;

@Service
public class WapMetadataCollector implements MetadataCollector{
	
	
	private static final Logger LOGGER = LoggerFactory.getLogger(WapMetadataCollector.class);

	@Inject
	private WapClient wapClient;
	@Inject
	private WapPublicIP wapNATRule;
	
	@Override
	public List<CloudVmMetaDataStatus> collect(AuthenticatedContext authenticatedContext, List<CloudResource> resources,
			List<CloudInstance> vms) {
		
		
		CloudCredential credential = authenticatedContext.getCloudCredential();
        WapCredentialView wapCredential = new WapCredentialView(credential);
        
        String endpoint = wapCredential.getEndpoint();
        String subscriptionId = wapCredential.getSubscriptionId();  
        String password = wapCredential.getPassword();
        String publicIP= wapNATRule.getPublicIP();
       
        Map<String,CloudResource> privateIdMap = groupByPrivateId(resources);

        
        List<CloudVmMetaDataStatus> listMdstatus = new ArrayList<>();

        for (CloudInstance vm : vms){
        	
        	CloudInstance cloudinstance = createCloudInstance(privateIdMap, vm);
        	
        	CloudVmInstanceStatus cloudVmInstanceStatus = new CloudVmInstanceStatus(cloudinstance, InstanceStatus.CREATED);
        	
        	String privateIP;
			
        	try {
				CloudInstanceMetaData cloudInstanceMetaData = getInstanceMetaData(endpoint, subscriptionId, password,
						publicIP, cloudinstance);
				
	        	CloudVmMetaDataStatus cloudVmMetaDataStatus = new CloudVmMetaDataStatus(cloudVmInstanceStatus, cloudInstanceMetaData);
	        	
	        	listMdstatus.add(cloudVmMetaDataStatus);

        	} catch (UnrecoverableKeyException | KeyManagementException | KeyStoreException | NoSuchAlgorithmException
					| CertificateException | IOException | ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

        }
     
        return listMdstatus;
	}

	/**
	 * @param endpoint
	 * @param subscriptionId
	 * @param password
	 * @param publicIP
	 * @param cloudinstance
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
	private CloudInstanceMetaData getInstanceMetaData(String endpoint, String subscriptionId, String password,
			String publicIP, CloudInstance cloudinstance)
			throws UnrecoverableKeyException, KeyManagementException, MalformedURLException, KeyStoreException,
			NoSuchAlgorithmException, CertificateException, ProtocolException, IOException, ParseException {
		
		String privateIP = wapClient.getPrivateIP(endpoint, subscriptionId, password, cloudinstance.getInstanceId());
		LOGGER.debug(publicIP);
		LOGGER.debug(privateIP);
		CloudInstanceMetaData cloudInstanceMetaData = new CloudInstanceMetaData(privateIP, publicIP);
		return cloudInstanceMetaData;
	}

	/**
	 * @param privateIdMap
	 * @param vm
	 * @return
	 */
	private CloudInstance createCloudInstance(Map<String, CloudResource> privateIdMap, CloudInstance vm) {
		LOGGER.debug(vm.getTemplate().getPrivateId().toString());
		CloudResource cloudResource = privateIdMap.get(vm.getTemplate().getPrivateId().toString());
		LOGGER.debug(cloudResource.toString());
		CloudInstance cloudinstance = new CloudInstance(cloudResource.getName(), vm.getTemplate());
		return cloudinstance;
	}

	private Map<String,CloudResource> groupByPrivateId (List<CloudResource> resources){
		Map<String,CloudResource> privateIdMap = new HashMap<>();
		for (CloudResource resource : resources){
			String resourceRef = resource.getReference();
			LOGGER.debug(resourceRef);
			privateIdMap.put(resourceRef,resource);
			
		}
		LOGGER.debug(privateIdMap.toString());
		return privateIdMap;
	}
	
	
	

}
