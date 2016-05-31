package com.sequenceiq.cloudbreak.cloud.wap;

import com.sequenceiq.cloudbreak.cloud.PlatformParameters;
import static com.sequenceiq.cloudbreak.cloud.model.VmType.vmType;
import static com.sequenceiq.cloudbreak.cloud.model.Region.region;
import com.sequenceiq.cloudbreak.util.FileReaderUtils;


import org.springframework.stereotype.Service;


import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;


import com.google.common.collect.Lists;
import com.sequenceiq.cloudbreak.cloud.model.AvailabilityZones;
import com.sequenceiq.cloudbreak.cloud.model.AvailabilityZone;
import com.sequenceiq.cloudbreak.cloud.model.DiskType;
import com.sequenceiq.cloudbreak.cloud.model.DiskTypes;
import com.sequenceiq.cloudbreak.cloud.model.Region;
import com.sequenceiq.cloudbreak.cloud.model.Regions;
import com.sequenceiq.cloudbreak.cloud.model.ScriptParams;
import com.sequenceiq.cloudbreak.cloud.model.StackParamValidation;
import com.sequenceiq.cloudbreak.cloud.model.VmTypes;
import com.sequenceiq.cloudbreak.cloud.model.VmType;

@Service
public class WapPlatformParameters implements PlatformParameters{
	 
	@Override
    public ScriptParams scriptParams(){
        return new ScriptParams("", 0);
	}


	@Override
    public DiskTypes diskTypes(){
        return new DiskTypes(Collections.<DiskType>emptyList(), DiskType.diskType(""));
    }


	
	@Override
    public Regions regions(){
		return new Regions(getRegions(), defaultRegion());
	}
	
	private Collection<Region> getRegions() {
        Collection<Region> regions = new ArrayList<>();
        regions.add(region("local"));
        return regions;
    }

    private Region defaultRegion() {
        return region("local");
    }
 
	@Override
	public VmTypes vmTypes(){
		return new VmTypes(virtualMachines(),defaultVirtualMachine());
	}

	private Collection<VmType> virtualMachines(){
		Collection<VmType> vmTypes = Lists.newArrayList();
		for(WapVmType vmType : WapVmType.values()){
			vmTypes.add(vmType(vmType.value));
		}
		return vmTypes;
	}

	private VmType defaultVirtualMachine(){
		return vmType(WapVmType.LARGE.value);
	}
	
	@Override
    public AvailabilityZones availabilityZones(){
        return new AvailabilityZones(Collections.<Region, List<AvailabilityZone>>emptyMap());
	}


	@Override
    public String resourceDefinition(String resource){
        return FileReaderUtils.readFileFromClasspathQuietly("definitions/wap-" + resource + ".json");
	}


	@Override
    public List<StackParamValidation> additionalStackParameters(){
        return Collections.emptyList();
	}

	private enum WapVmType{

		SMALL("Small"),
		MEDIUM("Medium"),
		LARGE("Large"),
		EXTRALARGE("ExtraLarge");

		private final String value;

		private WapVmType(String value){
			this.value=value;
		}
	}

}
