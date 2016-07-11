package com.sequenceiq.cloudbreak.cloud.wap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.sequenceiq.cloudbreak.cloud.model.CloudCredential;
import com.sequenceiq.cloudbreak.cloud.context.AuthenticatedContext;
import com.sequenceiq.cloudbreak.cloud.model.CloudCredentialStatus;
import com.sequenceiq.cloudbreak.cloud.model.CredentialStatus;
import com.sequenceiq.cloudbreak.cloud.wap.client.WapClient;
import com.sequenceiq.cloudbreak.cloud.wap.view.WapCredentialView;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.inject.Inject;

import java.io.FileOutputStream;

import com.sequenceiq.cloudbreak.cloud.CredentialConnector;
/**
 * Manages the credentials e.g public keys on Cloud Plarform side
 *
 */
@Service
public class WapCredentialConnector implements CredentialConnector {
	
	@Inject
	WapClient wapClient;

	private static final Logger LOGGER = LoggerFactory.getLogger(WapCredentialConnector.class);

    /**
     * Check whether the credential (e.g public key) associated with a stack (cluster) has present on Cloud provider.
     *
     * @param authenticatedContext the authenticated context which holds the client object
     * @return the status respone of method call
     */
    @Override
   public CloudCredentialStatus verify(AuthenticatedContext authenticatedContext){
        LOGGER.debug("Verify credential: {}", authenticatedContext.getCloudCredential());
        CloudCredential credential = authenticatedContext.getCloudCredential();
        WapCredentialView wapCredential = new WapCredentialView(credential);
        String endpoint = wapCredential.getEndpoint();
        String subscriptionId = wapCredential.getSubscriptionId();  
        LOGGER.debug("Verify certificate : {}");
        String password = wapCredential.getPassword();
        LOGGER.debug("password : {}",password);
        try {
			wapClient.checkConnect(endpoint,subscriptionId,password);
		} catch (KeyManagementException | KeyStoreException | NoSuchAlgorithmException | CertificateException
					| IOException | UnrecoverableKeyException e) {
				// TODO Auto-generated catch block
			e.printStackTrace();
			return new CloudCredentialStatus(authenticatedContext.getCloudCredential(), CredentialStatus.FAILED);
		}
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
	    LOGGER.debug("Created credential: {}", authenticatedContext.getCloudCredential());
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
	    LOGGER.debug("Deleted credential: {}", authenticatedContext.getCloudCredential());
	return new CloudCredentialStatus(authenticatedContext.getCloudCredential(),CredentialStatus.DELETED);
    }

}
