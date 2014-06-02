package com.sequenceiq.provisioning.converter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sequenceiq.provisioning.controller.json.StackJson;
import com.sequenceiq.provisioning.domain.Stack;
import com.sequenceiq.provisioning.domain.StackDescription;
import com.sequenceiq.provisioning.domain.Status;
import com.sequenceiq.provisioning.repository.CredentialRepository;
import com.sequenceiq.provisioning.repository.TemplateRepository;

@Component
public class StackConverter extends AbstractConverter<StackJson, Stack> {

    @Autowired
    private TemplateRepository templateRepository;

    @Autowired
    private CredentialRepository credentialRepository;

    @Override
    public StackJson convert(Stack entity) {
        StackJson stackJson = new StackJson();
        stackJson.setTemplateId(entity.getTemplate().getId());
        stackJson.setClusterSize(entity.getClusterSize());
        stackJson.setName(entity.getName());
        stackJson.setId(entity.getId());
        stackJson.setCloudPlatform(entity.getTemplate().cloudPlatform());
        stackJson.setCredentialId(entity.getCredential().getId());
        stackJson.setStatus(entity.getStatus());
        stackJson.setAmbariServerIp(entity.getAmbariIp());
        return stackJson;
    }

    public StackJson convert(Stack entity, StackDescription description) {
        StackJson stackJson = new StackJson();
        stackJson.setTemplateId(entity.getTemplate().getId());
        stackJson.setClusterSize(entity.getClusterSize());
        stackJson.setId(entity.getId());
        stackJson.setName(entity.getName());
        stackJson.setCredentialId(entity.getCredential().getId());
        stackJson.setCloudPlatform(entity.getTemplate().cloudPlatform());
        stackJson.setDescription(description);
        stackJson.setStatus(entity.getStatus());
        stackJson.setAmbariServerIp(entity.getAmbariIp());
        return stackJson;
    }

    @Override
    public Stack convert(StackJson json) {
        Stack stack = new Stack();
        stack.setClusterSize(json.getClusterSize());
        stack.setName(json.getName());
        stack.setCredential(credentialRepository.findOne(json.getCredentialId()));
        stack.setTemplate(templateRepository.findOne(Long.valueOf(json.getTemplateId())));
        stack.setStatus(Status.CREATE_IN_PROGRESS);
        return stack;
    }
}