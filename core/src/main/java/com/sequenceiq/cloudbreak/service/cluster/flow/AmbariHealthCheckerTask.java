package com.sequenceiq.cloudbreak.service.cluster.flow;

import com.sequenceiq.cloudbreak.service.CloudbreakServiceException;
import com.sequenceiq.cloudbreak.service.ClusterBasedStatusCheckerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AmbariHealthCheckerTask extends ClusterBasedStatusCheckerTask<AmbariClientPollerObject> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AmbariHealthCheckerTask.class);

    @Override
    public boolean checkStatus(AmbariClientPollerObject ambariClientPollerObject) {
        try {
            String ambariHealth = ambariClientPollerObject.getAmbariClient().healthCheck();
            if ("RUNNING".equals(ambariHealth)) {
                return true;
            }
            return false;
        } catch (Exception e) {
            LOGGER.info("Ambari is not running yet: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public void handleTimeout(AmbariClientPollerObject t) {
        throw new CloudbreakServiceException(String.format("Operation timed out. Ambari server could not start %s", t.getAmbariClient().getAmbari().getUri()));
    }

    @Override
    public String successMessage(AmbariClientPollerObject t) {
        return String.format("Ambari server successfully started '%s'", t.getAmbariClient().getAmbari().getUri());
    }

}
