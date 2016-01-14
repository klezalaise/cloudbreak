package com.sequenceiq.cloudbreak.cloud.byos;

import com.sequenceiq.cloudbreak.cloud.Setup;
import com.sequenceiq.cloudbreak.cloud.context.AuthenticatedContext;
import com.sequenceiq.cloudbreak.cloud.model.CloudStack;
import com.sequenceiq.cloudbreak.cloud.model.Image;
import com.sequenceiq.cloudbreak.cloud.notification.PersistenceNotifier;
import com.sequenceiq.cloudbreak.common.type.ImageStatusResult;

/**
 * Created by msereg on 1/12/16.
 */
public class BYOSSetup implements Setup {
    @Override
    public void prepareImage(AuthenticatedContext authenticatedContext, Image image) {

    }

    @Override
    public ImageStatusResult checkImageStatus(AuthenticatedContext authenticatedContext, Image image) {
        return null;
    }

    @Override
    public void prerequisites(AuthenticatedContext authenticatedContext, CloudStack stack, PersistenceNotifier persistenceNotifier) {

    }
}
