package com.sequenceiq.cloudbreak.cloud.wap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.sequenceiq.cloudbreak.cloud.Setup;
import com.sequenceiq.cloudbreak.cloud.context.AuthenticatedContext;
import com.sequenceiq.cloudbreak.cloud.model.CloudStack;
import com.sequenceiq.cloudbreak.cloud.model.FileSystem;
import com.sequenceiq.cloudbreak.cloud.model.Image;
import com.sequenceiq.cloudbreak.cloud.notification.PersistenceNotifier;
import com.sequenceiq.cloudbreak.common.type.ImageStatus;
import com.sequenceiq.cloudbreak.common.type.ImageStatusResult;


@Service
public class WapSetup implements Setup{
	
    private static final Logger LOGGER = LoggerFactory.getLogger(WapSetup.class);

	
	@Override
    public void prepareImage(AuthenticatedContext authenticatedContext, CloudStack stack, Image image){
    	LOGGER.debug("Setup [1]");
    }

    @Override
    public ImageStatusResult checkImageStatus(AuthenticatedContext authenticatedContext, CloudStack stack, Image image){
    	 return new ImageStatusResult(ImageStatus.CREATE_FINISHED, ImageStatusResult.COMPLETED);
    }
   
    @Override
    public void prerequisites(AuthenticatedContext authenticatedContext, CloudStack stack, PersistenceNotifier persistenceNotifier){
    
    }

    @Override
    public void validateFileSystem(FileSystem fileSystem) throws Exception{
    	
    }
}

