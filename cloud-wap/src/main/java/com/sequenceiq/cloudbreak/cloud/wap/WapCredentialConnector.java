package com.sequenceiq.cloudbreak.cloud.wap;


import com.sequenceiq.cloudbreak.cloud.CredentialConnector;
import com.sequenceiq.cloudbreak.cloud.context.AuthenticatedContext;
import com.sequenceiq.cloudbreak.cloud.model.CloudCredentialStatus;

public class WapCredentialConnector implements CredentialConnector{

	@Override
    public CloudCredentialStatus verify(AuthenticatedContext authenticatedContext) {
        return null;
    }

    @Override
    public CloudCredentialStatus create(AuthenticatedContext authenticatedContext) {
        return null;
    }


    @Override
    public CloudCredentialStatus delete(AuthenticatedContext authenticatedContext) {
        return null;
    }

}