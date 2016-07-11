package com.sequenceiq.cloudbreak.cloud.wap.view;

import com.sequenceiq.cloudbreak.cloud.model.CloudCredential;

public class WapCredentialView {

    private CloudCredential cloudCredential;

    public WapCredentialView(CloudCredential cloudCredential) {
        this.cloudCredential = cloudCredential;
    }

    public String getPublicKey() {
        return cloudCredential.getPublicKey();
    }

    public String getName() {
        return cloudCredential.getName();
    }

    public String getSubscriptionId() {
        return cloudCredential.getParameter("subscriptionId", String.class);
    }

    public String getCertificate() {
        return cloudCredential.getParameter("certificate", String.class);
    }

    public String getEndpoint() {
        return cloudCredential.getParameter("url", String.class);
    }

    public Long getId() {
        return cloudCredential.getId();
    }
    
    public String getPassword(){
    	return cloudCredential.getParameter("password",String.class);
    
    }

}