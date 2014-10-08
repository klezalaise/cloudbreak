package com.sequenceiq.cloudbreak.service.stack.connector.aws;

import static com.sequenceiq.cloudbreak.domain.CloudPlatform.AWS;

import java.util.Set;

import org.springframework.stereotype.Service;

import com.sequenceiq.cloudbreak.domain.CloudPlatform;
import com.sequenceiq.cloudbreak.domain.Resource;
import com.sequenceiq.cloudbreak.domain.Stack;
import com.sequenceiq.cloudbreak.service.stack.connector.CloudPlatformRollbackHandler;

@Service
public class AwsCloudPlatformRollbackHandler implements CloudPlatformRollbackHandler {
    @Override
    public void rollback(Stack stack, Set<Resource> resourceSet) {
        return;
    }

    @Override
    public CloudPlatform getCloudPlatform() {
        return AWS;
    }
}