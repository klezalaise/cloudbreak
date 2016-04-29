package com.sequenceiq.cloudbreak.cloud.wap;

import com.sequenceiq.cloudbreak.cloud.PlatformParameters;

import java.util.List;

import com.sequenceiq.cloudbreak.cloud.model.AvailabilityZones;
import com.sequenceiq.cloudbreak.cloud.model.DiskTypes;
import com.sequenceiq.cloudbreak.cloud.model.Regions;
import com.sequenceiq.cloudbreak.cloud.model.ScriptParams;
import com.sequenceiq.cloudbreak.cloud.model.StackParamValidation;
import com.sequenceiq.cloudbreak.cloud.model.VmTypes;


public class WapPlatformParameters implements PlatformParameters{
	 
	@Override
    public ScriptParams scriptParams(){
		return null;
	}


	@Override
    public DiskTypes diskTypes(){
    	return null;
    }


	
	@Override
    public Regions regions(){
		return null;
	}

 
	@Override
	public VmTypes vmTypes(){
		return null;
	}

	
	@Override
    public AvailabilityZones availabilityZones(){
		return null;
	}


	@Override
    public String resourceDefinition(String resource){
		return null;
	}


	@Override
    public List<StackParamValidation> additionalStackParameters(){
		return null;
	}
}