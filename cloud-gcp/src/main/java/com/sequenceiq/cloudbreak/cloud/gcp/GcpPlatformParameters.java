package com.sequenceiq.cloudbreak.cloud.gcp;

import static com.sequenceiq.cloudbreak.cloud.model.DiskType.diskType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.api.client.util.Lists;
import com.sequenceiq.cloudbreak.cloud.PlatformParameters;
import com.sequenceiq.cloudbreak.cloud.gcp.model.MachineDefinitionView;
import com.sequenceiq.cloudbreak.cloud.gcp.model.MachineDefinitionWrapper;
import com.sequenceiq.cloudbreak.cloud.gcp.model.ZoneDefinitionView;
import com.sequenceiq.cloudbreak.cloud.gcp.model.ZoneDefinitionWrapper;
import com.sequenceiq.cloudbreak.cloud.model.AvailabilityZone;
import com.sequenceiq.cloudbreak.cloud.model.AvailabilityZones;
import com.sequenceiq.cloudbreak.cloud.model.DiskType;
import com.sequenceiq.cloudbreak.cloud.model.DiskTypes;
import com.sequenceiq.cloudbreak.cloud.model.Region;
import com.sequenceiq.cloudbreak.cloud.model.Regions;
import com.sequenceiq.cloudbreak.cloud.model.ScriptParams;
import com.sequenceiq.cloudbreak.cloud.model.StackParamValidation;
import com.sequenceiq.cloudbreak.cloud.model.VmType;
import com.sequenceiq.cloudbreak.cloud.model.VmTypes;
import com.sequenceiq.cloudbreak.util.FileReaderUtils;
import com.sequenceiq.cloudbreak.util.JsonUtil;

@Service
public class GcpPlatformParameters implements PlatformParameters {
    private static final Integer START_LABEL = Integer.valueOf(97);
    private static final ScriptParams SCRIPT_PARAMS = new ScriptParams("sd", START_LABEL);
    private static final Long TB_64 = 64L;

    @Value("${cb.gcp.vm.parameter.definition.path:}")
    private String gcpVmParameterDefinitionPath;

    @Value("${cb.gcp.zone.parameter.definition.path:}")
    private String gcpZoneParameterDefinitionPath;

    private Map<Region, List<AvailabilityZone>> regions = new HashMap<>();
    private Map<AvailabilityZone, List<VmType>> vmTypes = new HashMap<>();
    private Region defaultRegion;
    private VmType defaultVmType;

    @PostConstruct
    public void init() {
        regions = readRegions();
        vmTypes = readVmTypes();

        defaultRegion = regions.keySet().iterator().next();
        defaultVmType = vmTypes.get(vmTypes.keySet().iterator().next()).get(14);
    }

    private Map<AvailabilityZone, List<VmType>> readVmTypes() {
        Map<AvailabilityZone, List<VmType>> vmTypes = new HashMap<>();
        String vm = resourceDefinition("vm");
        try {
            MachineDefinitionWrapper machineDefinitionWrapper = JsonUtil.readValue(vm, MachineDefinitionWrapper.class);
            for (Map.Entry<String, Object> object : machineDefinitionWrapper.getItems().entrySet()) {
                Map value = (Map) object.getValue();
                List<Object> machineTpes = (List<Object>) value.get("machineTypes");
                for (Object machineType : machineTpes) {
                    MachineDefinitionView machineDefinitionView = new MachineDefinitionView((Map) machineType);
                    AvailabilityZone availabilityZone = new AvailabilityZone(machineDefinitionView.getZone());
                    if (!vmTypes.containsKey(availabilityZone)) {
                        List<VmType> vmTypeList = new ArrayList<>();
                        vmTypes.put(availabilityZone, vmTypeList);
                    }
                    vmTypes.get(availabilityZone).add(VmType.vmType(machineDefinitionView.getName()));
                }


            }
        } catch (IOException e) {
            return vmTypes;
        }
        return vmTypes;
    }

    private Map<Region, List<AvailabilityZone>> readRegions() {
        Map<Region, List<AvailabilityZone>> regions = new HashMap<>();
        String zone = resourceDefinition("zone");
        try {
            ZoneDefinitionWrapper zoneDefinitionWrapper = JsonUtil.readValue(zone, ZoneDefinitionWrapper.class);
            for (ZoneDefinitionView object : zoneDefinitionWrapper.getItems()) {
                String region = object.getRegion();
                String avZone = object.getSelfLink();

                String[] splitRegion = region.split("/");
                String[] splitZone = avZone.split("/");

                Region regionObject = Region.region(splitRegion[splitRegion.length - 1]);
                AvailabilityZone availabilityZoneObject = AvailabilityZone.availabilityZone(splitZone[splitZone.length - 1]);
                if (!regions.keySet().contains(regionObject)) {
                    List<AvailabilityZone> availabilityZones = new ArrayList<>();
                    regions.put(regionObject, availabilityZones);
                }
                regions.get(regionObject).add(availabilityZoneObject);

            }
        } catch (IOException e) {
            return regions;
        }
        return regions;
    }

    @Override
    public ScriptParams scriptParams() {
        return SCRIPT_PARAMS;
    }

    @Override
    public DiskTypes diskTypes() {
        return new DiskTypes(getDiskTypes(), defaultDiskType());
    }

    private Collection<DiskType> getDiskTypes() {
        Collection<DiskType> disks = Lists.newArrayList();
        for (GcpDiskType diskType : GcpDiskType.values()) {
            disks.add(diskType(diskType.value()));
        }
        return disks;
    }

    private DiskType defaultDiskType() {
        return diskType(GcpDiskType.HDD.value());
    }

    @Override
    public Regions regions() {
        return new Regions(regions.keySet(), defaultRegion);
    }

    @Override
    public AvailabilityZones availabilityZones() {
        return new AvailabilityZones(regions);
    }

    @Override
    public String resourceDefinition(String resource) {
        return FileReaderUtils.readFileFromClasspathQuietly("definitions/gcp-" + resource + ".json");
    }

    @Override
    public List<StackParamValidation> additionalStackParameters() {
        return Collections.emptyList();
    }

    @Override
    public VmTypes vmTypes() {
        Set<VmType> lists = new LinkedHashSet<>();
        for (List<VmType> vmTypeList : vmTypes.values()) {
            lists.addAll(vmTypeList);
        }
        return new VmTypes(lists, defaultVirtualMachine());
    }

    private VmType defaultVirtualMachine() {
        return defaultVmType;
    }

    public enum GcpDiskType {
        SSD("pd-ssd"), HDD("pd-standard");

        private final String value;

        private GcpDiskType(String value) {
            this.value = value;
        }

        public String value() {
            return value;
        }

        public String getUrl(String projectId, AvailabilityZone zone) {
            return getUrl(projectId, zone, value);
        }

        public static String getUrl(String projectId, AvailabilityZone zone, String volumeId) {
            return String.format("https://www.googleapis.com/compute/v1/projects/%s/zones/%s/diskTypes/%s", projectId, zone.value(), volumeId);
        }
    }



/*
    // https://cloud.google.com/compute/docs/machine-types
    public enum GcpVmType {

        N1_STANDARD_1("n1-standard-1", 32, TB_64, TB_64, 1, 3.75),
        N1_STANDARD_2("n1-standard-2", 64, TB_64, TB_64, 2, 7.5),
        N1_STANDARD_4("n1-standard-4", 64, TB_64, TB_64, 4, 15d),
        N1_STANDARD_8("n1-standard-8", 128, TB_64, TB_64, 8, 30d),
        N1_STANDARD_16("n1-standard-16", 128, TB_64, TB_64, 16 , 60d),
        N1_STANDARD_32("n1-standard-32", 128, TB_64, TB_64, 30 ,120d),
        N1_HIGHMEM_2("n1-highmem-2", 64, TB_64, TB_64, 2, 13d),
        N1_HIGHMEM_4("n1-highmem-4", 64, TB_64, TB_64, 4, 26d),
        N1_HIGHMEM_8("n1-highmem-8", 128, TB_64, TB_64, 8, 52d),
        N1_HIGHMEM_16("n1-highmem-16", 128, TB_64, TB_64, 16, 104d),
        N1_HIGHMEM_32("n1-highmem-32", 128, TB_64, TB_64, 32, 208d),
        N1_HIGHCPU_2("n1-highcpu-2", 64, TB_64, TB_64, 2, 1.8d),
        N1_HIGHCPU_4("n1-highcpu-4", 64, TB_64, TB_64, 4, 3.6d),
        N1_HIGHCPU_8("n1-highcpu-8", 128, TB_64, TB_64, 8, 7.2d),
        N1_HIGHCPU_16("n1-highcpu-16", 128, TB_64, TB_64, 16, 14.4d),
        N1_HIGHCPU_32("n1-highcpu-32", 128, TB_64, TB_64, 32, 28.8d);

        private final String value;
        private final Integer numberOfCPUs;
        private final Double amountOfRam;
        private final Integer maximumNumberOfDisks;
        private final Long maximumSizePerDisk;
        private final Long totalDiskSize;

        private GcpVmType(String value, Integer maximumNumberOfDisks, Long maximumSizePerDisk, Long totalDiskSize, Integer numberOfCPUs, Double amountOfRam) {
            this.value = value;
            this.maximumNumberOfDisks = maximumNumberOfDisks;
            this.maximumSizePerDisk = maximumSizePerDisk;
            this.totalDiskSize = totalDiskSize;
            this.numberOfCPUs = numberOfCPUs;
            this.amountOfRam = amountOfRam;
        }

        public static List<GcpVmType> haswellGcpVmTypes() {
            return Arrays.asList(GcpVmType.values());
        }

        public static List<GcpVmType> ivyBridgeGcpVmTypes() {
            return Arrays.asList(GcpVmType.values());
        }

        public static List<GcpVmType> sandyBridgeGcpVmTypes() {
            List<GcpVmType> gcpVmTypes = Arrays.asList(GcpVmType.values());
            gcpVmTypes.remove(GcpVmType.N1_HIGHCPU_32);
            gcpVmTypes.remove(GcpVmType.N1_HIGHMEM_32);
            gcpVmTypes.remove(GcpVmType.N1_STANDARD_32);
            return gcpVmTypes;
        }

        public String value() {
            return value;
        }

        public Integer maximumNumberOfDisks() {
            return maximumNumberOfDisks;
        }

        public Long maximumSizePerDisk() {
            return maximumSizePerDisk;
        }

        public Long totalDiskSize() {
            return totalDiskSize;
        }

        public Integer numberOfCPUs() {
            return numberOfCPUs;
        }

        public Double amountOfRam() {
            return amountOfRam;
        }
    }*/
}
