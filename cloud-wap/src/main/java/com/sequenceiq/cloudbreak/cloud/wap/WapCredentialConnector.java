package com.sequenceiq.cloudbreak.cloud.wap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.sequenceiq.cloudbreak.cloud.CredentialConnector;

import com.sequenceiq.cloudbreak.cloud.context.AuthenticatedContext;
import com.sequenceiq.cloudbreak.cloud.model.CloudCredentialStatus;
import com.sequenceiq.cloudbreak.cloud.model.CredentialStatus;

/**
 * Manages the credentials e.g public keys on Cloud Plarform side
 *
 */
@Service
public class WapCredentialConnector implements CredentialConnector {

	private static final Logger LOGGER = LoggerFactory.getLogger(WapCredentialConnector.class);

    /**
     * Check whether the credential (e.g public key) associated with a stack (cluster) has present on Cloud provider.
     *
     * @param authenticatedContext the authenticated context which holds the client object
     * @return the status respone of method call
     */
    @Override
   public CloudCredentialStatus verify(AuthenticatedContext authenticatedContext){
	    LOGGER.info("very credenial");
	    return new CloudCredentialStatus(authenticatedContext.getCloudCredential(), CredentialStatus.VERIFIED);
    }


    /**
     * Create the credential (e.g public key) associated with a stack (cluster) on Cloud provider.
     *
     * @param authenticatedContext the authenticated context which holds the client object
     * @return the status respone of method call
     */
    @Override
    public CloudCredentialStatus create(AuthenticatedContext authenticatedContext){
   	return new CloudCredentialStatus(authenticatedContext.getCloudCredential(),CredentialStatus.CREATED); 
    }


    /**
     * Delete the credential (e.g public key) associated with a stack (cluster) from Cloud provider.
     *
     * @param authenticatedContext the authenticated context which holds the client object
     * @return the status respone of method call
     */
    @Override
   public CloudCredentialStatus delete(AuthenticatedContext authenticatedContext){
	return new CloudCredentialStatus(authenticatedContext.getCloudCredential(),CredentialStatus.DELETED);
    }

}
