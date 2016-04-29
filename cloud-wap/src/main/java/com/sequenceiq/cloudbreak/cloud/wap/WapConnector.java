package com.sequenceiq.cloudbreak.cloud.wap;


import javax.inject.Inject;

import org.springframework.stereotype.Service;

import com.sequenceiq.cloudbreak.cloud.Authenticator;
import com.sequenceiq.cloudbreak.cloud.CloudConnector;
import com.sequenceiq.cloudbreak.cloud.CredentialConnector;
import com.sequenceiq.cloudbreak.cloud.InstanceConnector;
import com.sequenceiq.cloudbreak.cloud.MetadataCollector;
import com.sequenceiq.cloudbreak.cloud.PlatformParameters;
import com.sequenceiq.cloudbreak.cloud.ResourceConnector;
import com.sequenceiq.cloudbreak.cloud.Setup;
import com.sequenceiq.cloudbreak.cloud.model.Platform;
import com.sequenceiq.cloudbreak.cloud.model.Variant;

@Service
public class WapConnector implements CloudConnector {
	
	
	@Inject
    private WapAuthenticator authenticator;
    @Inject
    private WapProvisionSetup provisionSetup;
    @Inject
    private WapInstanceConnector instanceConnector;
    @Inject
    private WapResourceConnector resourceConnector;
    @Inject
    private WapCredentialConnector wapCredentialConnector;
    @Inject
    private WapPlatformParameters wapPlatformParameters;
    @Inject
    private WapMetadataCollector metadataCollector;

	
	 @Override
	    public Authenticator authentication() {
	        return null;
	    }

	    @Override
	    public Setup setup() {
	        return null;
	    }

	    @Override
	    public CredentialConnector credentials() {
	        return null;
	    }

	    @Override
	    public ResourceConnector resources() {
	        return null;
	    }

	    @Override
	    public InstanceConnector instances() {
	        return null;
	    }

	    @Override
	    public MetadataCollector metadata() {
	        return null;
	    }

	    @Override
	    public PlatformParameters parameters() {
	        return null;
	    }

	    @Override
	    public Platform platform() {
	        return null;
	    }

	    @Override
	    public Variant variant() {
	        return null;
	    }

}