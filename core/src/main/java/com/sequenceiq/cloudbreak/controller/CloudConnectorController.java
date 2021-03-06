package com.sequenceiq.cloudbreak.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.api.endpoint.ConnectorEndpoint;
import com.sequenceiq.cloudbreak.cloud.model.PlatformDisks;
import com.sequenceiq.cloudbreak.cloud.model.PlatformRegions;
import com.sequenceiq.cloudbreak.cloud.model.PlatformVariants;
import com.sequenceiq.cloudbreak.cloud.model.PlatformVirtualMachines;
import com.sequenceiq.cloudbreak.api.model.JsonEntity;
import com.sequenceiq.cloudbreak.api.model.PlatformDisksJson;
import com.sequenceiq.cloudbreak.api.model.PlatformRegionsJson;
import com.sequenceiq.cloudbreak.api.model.PlatformVariantsJson;
import com.sequenceiq.cloudbreak.api.model.PlatformVirtualMachinesJson;
import com.sequenceiq.cloudbreak.api.model.VmTypeJson;
import com.sequenceiq.cloudbreak.service.stack.CloudParameterService;

@Component
public class CloudConnectorController implements ConnectorEndpoint {

    @Autowired
    private CloudParameterService cloudParameterService;

    @Autowired
    @Qualifier("conversionService")
    private ConversionService conversionService;

    @Override
    public Map<String, JsonEntity> getPlatforms() {
        PlatformVariants pv = cloudParameterService.getPlatformVariants();
        PlatformDisks diskTypes = cloudParameterService.getDiskTypes();
        PlatformVirtualMachines vmtypes = cloudParameterService.getVmtypes();
        PlatformRegions regions = cloudParameterService.getRegions();

        Map<String, JsonEntity> map = new HashMap<>();

        map.put("variants", conversionService.convert(pv, PlatformVariantsJson.class));
        map.put("disks", conversionService.convert(diskTypes, PlatformDisksJson.class));
        map.put("virtualMachines", conversionService.convert(vmtypes, PlatformVirtualMachinesJson.class));
        map.put("regions", conversionService.convert(regions, PlatformRegionsJson.class));

        return map;
    }

    @Override
    public PlatformVariantsJson getPlatformVariants() {
        PlatformVariants pv = cloudParameterService.getPlatformVariants();
        return conversionService.convert(pv, PlatformVariantsJson.class);
    }

    @Override
    public Collection<String> getPlatformVariantByType(String type) {
        PlatformVariants pv = cloudParameterService.getPlatformVariants();
        Collection<String> strings = conversionService.convert(pv, PlatformVariantsJson.class).getPlatformToVariants().get(type.toUpperCase());
        return strings == null ? new ArrayList<String>() : strings;
    }

    @Override
    public PlatformDisksJson getDisktypes() {
        PlatformDisks dts = cloudParameterService.getDiskTypes();
        return conversionService.convert(dts, PlatformDisksJson.class);
    }

    @Override
    public Collection<String> getDisktypeByType(String type) {
        PlatformDisks diskTypes = cloudParameterService.getDiskTypes();
        Collection<String> strings = conversionService.convert(diskTypes, PlatformDisksJson.class)
                .getDiskTypes().get(type.toUpperCase());
        return strings == null ? new ArrayList<String>() : strings;
    }

    @Override
    public PlatformVirtualMachinesJson getVmTypes() {
        PlatformVirtualMachines vmtypes = cloudParameterService.getVmtypes();
        return conversionService.convert(vmtypes, PlatformVirtualMachinesJson.class);
    }

    @Override
    public Collection<VmTypeJson> getVmTypeByType(String type) {
        PlatformVirtualMachines vmtypes = cloudParameterService.getVmtypes();
        Collection<VmTypeJson> vmTypes = conversionService.convert(vmtypes, PlatformVirtualMachinesJson.class)
                .getVirtualMachines().get(type.toUpperCase());
        return vmTypes == null ? new ArrayList<VmTypeJson>() : vmTypes;
    }

    @Override
    public PlatformRegionsJson getRegions() {
        PlatformRegions pv = cloudParameterService.getRegions();
        return conversionService.convert(pv, PlatformRegionsJson.class);
    }

    @Override
    public Collection<String> getRegionRByType(String type) {
        PlatformRegions pv = cloudParameterService.getRegions();
        Collection<String> regions = conversionService.convert(pv, PlatformRegionsJson.class)
                .getRegions().get(type.toUpperCase());
        return regions == null ? new ArrayList<String>() : regions;
    }

    @Override
    public Map<String, Collection<String>> getRegionAvByType(String type) {
        PlatformRegions pv = cloudParameterService.getRegions();
        Map<String, Collection<String>> azs = conversionService.convert(pv, PlatformRegionsJson.class)
                .getAvailabilityZones().get(type.toUpperCase());
        return azs == null ? new HashMap<String, Collection<String>>() : azs;
    }

}
