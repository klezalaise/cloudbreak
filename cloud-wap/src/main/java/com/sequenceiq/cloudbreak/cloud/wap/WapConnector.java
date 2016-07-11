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
		private WapPlatformParameters platformParameters;
		@Inject 
		private WapCredentialConnector wapCredentialConnector;
		@Inject
		private WapAuthenticator wapAuthenticator;
		@Inject
		private WapResourceConnector resourceConnector;
		@Inject
		private WapSetup wapSetup;
		@Inject
		private WapMetadataCollector wapMetadataConnector;
		
		
	    @Override
	    public Authenticator authentication() {
		    return wapAuthenticator; 
	    }

	    @Override
	    public Setup setup() {
		    return wapSetup;

	    }

	    @Override
	    public CredentialConnector credentials() {
		    return wapCredentialConnector;
	    }

	    @Override
	    public ResourceConnector resources() {
	    	
	    	return resourceConnector;
	    }

	    @Override
	    public InstanceConnector instances() {
	    	throw new UnsupportedOperationException("Instance operation is not supported on WAP stacks.");
	    }

	    @Override
	    public MetadataCollector metadata() {
	    	return wapMetadataConnector;
	    }

	    @Override
	    public PlatformParameters parameters() {
	    	return platformParameters;
		    //throw new UnsupportedOperationException("Platform operation is not supported on WAP stacks.");

	    }

	    @Override
	    public Platform platform() {
	    	return WapConstants.WAP_PLATFORM;
		    //throw new UnsupportedOperationException("Authentication operation is not supported on WAP stacks.");

	    }

	    @Override
	    public Variant variant() {
	    	return WapConstants.WAP_VARIANT;
		    //throw new UnsupportedOperationException("Authentication operation is not supported on WAP stacks.");

	    }

}
