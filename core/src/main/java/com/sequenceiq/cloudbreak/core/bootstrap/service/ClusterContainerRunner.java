package com.sequenceiq.cloudbreak.core.bootstrap.service;

import static com.sequenceiq.cloudbreak.core.bootstrap.service.StackDeletionBasedExitCriteriaModel.stackDeletionBasedExitCriteriaModel;
import static com.sequenceiq.cloudbreak.orchestrator.containers.DockerContainer.AMBARI_AGENT;
import static com.sequenceiq.cloudbreak.orchestrator.containers.DockerContainer.AMBARI_DB;
import static com.sequenceiq.cloudbreak.orchestrator.containers.DockerContainer.AMBARI_SERVER;
import static com.sequenceiq.cloudbreak.orchestrator.containers.DockerContainer.CONSUL_WATCH;
import static com.sequenceiq.cloudbreak.orchestrator.containers.DockerContainer.HAVEGED;
import static com.sequenceiq.cloudbreak.orchestrator.containers.DockerContainer.KERBEROS;
import static com.sequenceiq.cloudbreak.orchestrator.containers.DockerContainer.LOGROTATE;
import static com.sequenceiq.cloudbreak.orchestrator.containers.DockerContainer.REGISTRATOR;
import static com.sequenceiq.cloudbreak.orchestrator.security.KerberosConfiguration.DOMAIN_REALM;
import static com.sequenceiq.cloudbreak.orchestrator.security.KerberosConfiguration.REALM;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.sequenceiq.cloudbreak.api.model.InstanceGroupType;
import com.sequenceiq.cloudbreak.cloud.scheduler.CancellationException;
import com.sequenceiq.cloudbreak.core.CloudbreakException;
import com.sequenceiq.cloudbreak.core.flow.context.ClusterScalingContext;
import com.sequenceiq.cloudbreak.core.flow.context.DefaultFlowContext;
import com.sequenceiq.cloudbreak.core.flow.context.ProvisioningContext;
import com.sequenceiq.cloudbreak.domain.Cluster;
import com.sequenceiq.cloudbreak.domain.InstanceGroup;
import com.sequenceiq.cloudbreak.domain.InstanceMetaData;
import com.sequenceiq.cloudbreak.domain.Orchestrator;
import com.sequenceiq.cloudbreak.domain.Stack;
import com.sequenceiq.cloudbreak.orchestrator.ContainerOrchestrator;
import com.sequenceiq.cloudbreak.orchestrator.exception.CloudbreakOrchestratorCancelledException;
import com.sequenceiq.cloudbreak.orchestrator.exception.CloudbreakOrchestratorException;
import com.sequenceiq.cloudbreak.orchestrator.model.ContainerConstraint;
import com.sequenceiq.cloudbreak.orchestrator.model.OrchestrationCredential;
import com.sequenceiq.cloudbreak.orchestrator.model.port.TcpPortBinding;
import com.sequenceiq.cloudbreak.orchestrator.security.KerberosConfiguration;
import com.sequenceiq.cloudbreak.repository.StackRepository;
import com.sequenceiq.cloudbreak.service.TlsSecurityService;
import com.sequenceiq.cloudbreak.service.cluster.ClusterService;
import com.sequenceiq.cloudbreak.service.stack.connector.VolumeUtils;

@Component
public class ClusterContainerRunner {
    private static final String CONTAINER_VOLUME_PATH = "/var/log";
    private static final String HOST_VOLUME_PATH = VolumeUtils.getLogVolume("logs");
    private static final String HOST_NETWORK_MODE = "host";
    private static final int AMBARI_PORT = 8080;

    @Inject
    private ClusterService clusterService;

    @Inject
    private StackRepository stackRepository;

    @Inject
    private ContainerConfigService containerConfigService;

    @Inject
    private ContainerOrchestratorResolver containerOrchestratorResolver;

    @Inject
    private TlsSecurityService tlsSecurityService;

    public void runClusterContainers(ProvisioningContext context) throws CloudbreakException {
        try {
            initializeClusterContainers(context, false, Collections.<String>emptySet());
        } catch (CloudbreakOrchestratorCancelledException e) {
            throw new CancellationException(e.getMessage());
        } catch (CloudbreakOrchestratorException e) {
            throw new CloudbreakException(e);
        }
    }

    public void addClusterContainers(ClusterScalingContext context) throws CloudbreakException {
        try {
            initializeClusterContainers(context, true, context.getUpscaleCandidateAddresses());
        } catch (CloudbreakOrchestratorCancelledException e) {
            throw new CancellationException(e.getMessage());
        } catch (CloudbreakOrchestratorException e) {
            throw new CloudbreakException(e);
        }
    }

    private void initializeClusterContainers(DefaultFlowContext context, Boolean add, Set<String> candidateAddresses) throws CloudbreakException,
            CloudbreakOrchestratorException {
        String cloudPlatform = context.getCloudPlatform().value();
        Stack stack = stackRepository.findOneWithLists(context.getStackId());

        Orchestrator orchestrator = stack.getOrchestrator();
        OrchestrationCredential credential = new OrchestrationCredential(orchestrator.getApiEndpoint(), orchestrator.getAttributes().getMap());

        ContainerOrchestrator containerOrchestrator = containerOrchestratorResolver.get(orchestrator.getType());

        InstanceMetaData gatewayInstance = stack.getGatewayInstanceGroup().getInstanceMetaData().iterator().next();
        String gatewayHostname = gatewayInstance.getDiscoveryName();

        if (!add) {
            Cluster cluster = clusterService.retrieveClusterByStackId(stack.getId());

            // create constraints based on constraints on hostgroups
            ContainerConstraint registratorConstraint = getRegistratorConstraint(gatewayHostname);
            containerOrchestrator.runContainer(containerConfigService.get(stack, REGISTRATOR), credential, registratorConstraint,
                    stackDeletionBasedExitCriteriaModel(stack.getId()));

            ContainerConstraint ambariServerDbConstraint = getAmbariServerDbConstraint(gatewayHostname);
            containerOrchestrator.runContainer(containerConfigService.get(stack, AMBARI_DB), credential, ambariServerDbConstraint,
                    stackDeletionBasedExitCriteriaModel(stack.getId()));

            ContainerConstraint ambariServerConstraint = getAmbariServerConstraint(cloudPlatform, gatewayHostname);
            containerOrchestrator.runContainer(containerConfigService.get(stack, AMBARI_SERVER), credential, ambariServerConstraint,
                    stackDeletionBasedExitCriteriaModel(stack.getId()));

            if (cluster.isSecure()) {
                ContainerConstraint havegedConstraint = getHavegedConstraint(gatewayHostname);
                containerOrchestrator.runContainer(containerConfigService.get(stack, HAVEGED), credential, havegedConstraint,
                        stackDeletionBasedExitCriteriaModel(stack.getId()));

                ContainerConstraint kerberosServerConstraint = getKerberosServerConstraint(cluster, gatewayHostname);
                containerOrchestrator.runContainer(containerConfigService.get(stack, KERBEROS), credential, kerberosServerConstraint,
                        stackDeletionBasedExitCriteriaModel(stack.getId()));
            }
        }

        runAmbariAgentContainers(add, candidateAddresses, containerOrchestrator, cloudPlatform, stack, credential);

        List<String> hosts = getHosts(add, stack, candidateAddresses);
        ContainerConstraint consulWatchConstraint = getConsulWatchConstraint(hosts);
        containerOrchestrator.runContainer(containerConfigService.get(stack, CONSUL_WATCH), credential, consulWatchConstraint,
                stackDeletionBasedExitCriteriaModel(stack.getId()));

        ContainerConstraint logrotateConstraint = getLogrotateConstraint(hosts);
        containerOrchestrator.runContainer(containerConfigService.get(stack, LOGROTATE), credential, logrotateConstraint,
                stackDeletionBasedExitCriteriaModel(stack.getId()));
    }

    private ContainerConstraint getRegistratorConstraint(String gatewayHostname) {
        return new ContainerConstraint.Builder()
                .withName(REGISTRATOR.getName())
                .networkMode(HOST_NETWORK_MODE)
                .addVolumeBindings(ImmutableMap.of("/var/run/docker.sock", "/tmp/docker.sock"))
                .addHosts(ImmutableList.of(gatewayHostname))
                .cmd(new String[]{"consul://127.0.0.1:8500"})
                .build();
    }

    private ContainerConstraint getAmbariServerDbConstraint(String gatewayHostname) {
        return new ContainerConstraint.Builder()
                .withName(AMBARI_DB.getName())
                .networkMode(HOST_NETWORK_MODE)
                .addVolumeBindings(ImmutableMap.of("/data/ambari-server/pgsql/data", "/var/lib/postgresql/data",
                        HOST_VOLUME_PATH + "/consul-watch", HOST_VOLUME_PATH + "/consul-watch"))
                .addHosts(ImmutableList.of(gatewayHostname))
                .addEnv(Arrays.asList(String.format("POSTGRES_PASSWORD=%s", "bigdata"), String.format("POSTGRES_USER=%s", "ambari")))
                .build();
    }

    private ContainerConstraint getAmbariServerConstraint(String cloudPlatform, String gatewayHostname) {
        return new ContainerConstraint.Builder()
                .withName(AMBARI_SERVER.getName())
                .networkMode(HOST_NETWORK_MODE)
                .tcpPortBinding(new TcpPortBinding(AMBARI_PORT, "0.0.0.0", AMBARI_PORT))
                .addVolumeBindings(ImmutableMap.of(HOST_VOLUME_PATH, CONTAINER_VOLUME_PATH, "/etc/krb5.conf", "/etc/krb5.conf"))
                .addHosts(ImmutableList.of(gatewayHostname))
                .addEnv(Arrays.asList("SERVICE_NAME=ambari-8080"))
                .cmd(new String[]{String.format("systemd.setenv=POSTGRES_DB=localhost systemd.setenv=CLOUD_PLATFORM=%s", cloudPlatform)})
                .build();
    }

    private ContainerConstraint getHavegedConstraint(String gatewayHostname) {
        return new ContainerConstraint.Builder()
                .withName(HAVEGED.getName())
                .addHosts(ImmutableList.of(gatewayHostname))
                .build();
    }

    private ContainerConstraint getKerberosServerConstraint(Cluster cluster, String gatewayHostname) {
        KerberosConfiguration kerberosConf = new KerberosConfiguration(cluster.getKerberosMasterKey(), cluster.getKerberosAdmin(),
                cluster.getKerberosPassword());
        return new ContainerConstraint.Builder()
                .withName(KERBEROS.getName())
                .networkMode(HOST_NETWORK_MODE)
                .addVolumeBindings(ImmutableMap.of(HOST_VOLUME_PATH, CONTAINER_VOLUME_PATH, "/etc/krb5.conf", "/etc/krb5.conf"))
                .addHosts(ImmutableList.of(gatewayHostname))
                .addEnv(Arrays.asList(String.format("SERVICE_NAME=%s", KERBEROS.getName()),
                        "NAMESERVER_IP=127.0.0.1",
                        String.format("REALM=%s", REALM),
                        String.format("DOMAIN_REALM=%s", DOMAIN_REALM),
                        String.format("KERB_MASTER_KEY=%s", kerberosConf.getMasterKey()),
                        String.format("KERB_ADMIN_USER=%s", kerberosConf.getUser()),
                        String.format("KERB_ADMIN_PASS=%s", kerberosConf.getPassword())))
                .build();
    }

    private void runAmbariAgentContainers(Boolean add, Set<String> candidateAddresses, ContainerOrchestrator orchestrator, String cloudPlatform, Stack stack,
            OrchestrationCredential cred) throws CloudbreakOrchestratorException {
        //TODO: iterate hostgroups instead
        for (InstanceGroup instanceGroup : stack.getInstanceGroups()) {
            if (InstanceGroupType.CORE.equals(instanceGroup.getInstanceGroupType())) {
                int volumeCount = instanceGroup.getTemplate().getVolumeCount();
                Map<String, String> dataVolumeBinds = getDataVolumeBinds(volumeCount);
                List<String> hosts = getHosts(add, candidateAddresses, instanceGroup);
                ImmutableMap<String, String> volumeBinds = ImmutableMap.of("/data/jars", "/data/jars", HOST_VOLUME_PATH, CONTAINER_VOLUME_PATH);
                dataVolumeBinds.putAll(volumeBinds);
                ContainerConstraint ambariAgentConstraint = getAmbariAgentConstraint(cloudPlatform, dataVolumeBinds, hosts);
                orchestrator.runContainer(containerConfigService.get(stack, AMBARI_AGENT), cred, ambariAgentConstraint,
                        stackDeletionBasedExitCriteriaModel(stack.getId()));
            }
        }
    }

    private Map<String, String> getDataVolumeBinds(long volumeCount) {
        Map<String, String> dataVolumeBinds = new HashMap<>();
        for (int i = 1; i <= volumeCount; i++) {
            String dataVolumePath = VolumeUtils.VOLUME_PREFIX + i;
            dataVolumeBinds.put(dataVolumePath, dataVolumePath);
        }
        return dataVolumeBinds;
    }

    private ContainerConstraint getAmbariAgentConstraint(String cloudPlatform, Map<String, String> dataVolumeBinds, List<String> hosts) {
        return new ContainerConstraint.Builder()
                .withName(AMBARI_AGENT.getName())
                .networkMode(HOST_NETWORK_MODE)
                .addVolumeBindings(dataVolumeBinds)
                .addHosts(hosts)
                .cmd(new String[]{String.format("systemd.setenv=CLOUD_PLATFORM=%s", cloudPlatform)})
                .build();
    }

    private ContainerConstraint getConsulWatchConstraint(List<String> hosts) {
        return new ContainerConstraint.Builder()
                .withName(CONSUL_WATCH.getName())
                .addEnv(ImmutableList.of("CONSUL_HOST=127.0.0.1"))
                .networkMode(HOST_NETWORK_MODE)
                .addVolumeBindings(ImmutableMap.of("/var/run/docker.sock", "/var/run/docker.sock"))
                .addHosts(hosts)
                .build();
    }

    private ContainerConstraint getLogrotateConstraint(List<String> hosts) {
        return new ContainerConstraint.Builder()
                .withName(LOGROTATE.getName())
                .networkMode(HOST_NETWORK_MODE)
                .addVolumeBindings(ImmutableMap.of("/var/lib/docker/containers", "/var/lib/docker/containers"))
                .addHosts(hosts)
                .build();
    }

    private List<String> getHosts(Boolean add, Stack stack, Set<String> candidateAddresses) {
        List<String> hosts = new ArrayList<>();
        for (InstanceMetaData instanceMetaData : stack.getRunningInstanceMetaData()) {
            if (!add || candidateAddresses.contains(instanceMetaData.getPrivateIp())) {
                hosts.add(instanceMetaData.getDiscoveryName());
            }
        }
        return hosts;
    }

    private List<String> getHosts(Boolean add, Set<String> candidateAddresses, InstanceGroup instanceGroup) {
        List<String> hosts = new ArrayList<>();
        for (InstanceMetaData instanceMetaData : instanceGroup.getInstanceMetaData()) {
            String privateIp = instanceMetaData.getPrivateIp();
            if (!add || candidateAddresses.contains(privateIp)) {
                hosts.add(instanceMetaData.getDiscoveryName());
            }
        }
        return hosts;
    }
}
