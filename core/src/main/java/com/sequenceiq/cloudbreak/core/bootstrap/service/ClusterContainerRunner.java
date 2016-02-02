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

import javax.inject.Inject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.sequenceiq.cloudbreak.cloud.scheduler.CancellationException;
import com.sequenceiq.cloudbreak.core.CloudbreakException;
import com.sequenceiq.cloudbreak.core.flow.context.ClusterScalingContext;
import com.sequenceiq.cloudbreak.core.flow.context.DefaultFlowContext;
import com.sequenceiq.cloudbreak.core.flow.context.ProvisioningContext;
import com.sequenceiq.cloudbreak.domain.Cluster;
import com.sequenceiq.cloudbreak.domain.Constraint;
import com.sequenceiq.cloudbreak.domain.Container;
import com.sequenceiq.cloudbreak.domain.HostGroup;
import com.sequenceiq.cloudbreak.domain.InstanceGroup;
import com.sequenceiq.cloudbreak.domain.InstanceMetaData;
import com.sequenceiq.cloudbreak.domain.Orchestrator;
import com.sequenceiq.cloudbreak.domain.Stack;
import com.sequenceiq.cloudbreak.orchestrator.ContainerOrchestrator;
import com.sequenceiq.cloudbreak.orchestrator.exception.CloudbreakOrchestratorCancelledException;
import com.sequenceiq.cloudbreak.orchestrator.exception.CloudbreakOrchestratorException;
import com.sequenceiq.cloudbreak.orchestrator.model.ContainerConstraint;
import com.sequenceiq.cloudbreak.orchestrator.model.ContainerInfo;
import com.sequenceiq.cloudbreak.orchestrator.model.OrchestrationCredential;
import com.sequenceiq.cloudbreak.orchestrator.model.port.TcpPortBinding;
import com.sequenceiq.cloudbreak.orchestrator.security.KerberosConfiguration;
import com.sequenceiq.cloudbreak.repository.HostGroupRepository;
import com.sequenceiq.cloudbreak.repository.InstanceMetaDataRepository;
import com.sequenceiq.cloudbreak.repository.StackRepository;
import com.sequenceiq.cloudbreak.service.cluster.ClusterService;
import com.sequenceiq.cloudbreak.service.cluster.ContainerService;
import com.sequenceiq.cloudbreak.service.stack.connector.VolumeUtils;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Component;

@Component
public class ClusterContainerRunner {
    private static final String CONTAINER_VOLUME_PATH = "/var/log";
    private static final String HOST_VOLUME_PATH = VolumeUtils.getLogVolume("logs");
    private static final String HOST_NETWORK_MODE = "host";
    private static final int AMBARI_PORT = 8080;
    private static final String NONE = "none";

    @Inject
    private ClusterService clusterService;

    @Inject
    private StackRepository stackRepository;

    @Inject
    private HostGroupRepository hostGroupRepository;

    @Inject
    private InstanceMetaDataRepository instanceMetaDataRepository;

    @Inject
    private ContainerConfigService containerConfigService;

    @Inject
    private ContainerOrchestratorResolver containerOrchestratorResolver;

    @Inject
    private ContainerService containerService;

    @Inject
    private ConversionService conversionService;

    public void runClusterContainers(ProvisioningContext context) throws CloudbreakException {
        try {
            Stack stack = stackRepository.findOneWithLists(context.getStackId());
            List<ContainerInfo> containerInfo = initializeClusterContainers(stack, cloudPlatform(context), false, Collections.<String>emptySet());
            containerService.save(convert(containerInfo, stack.getCluster()));
        } catch (CloudbreakOrchestratorCancelledException e) {
            throw new CancellationException(e.getMessage());
        } catch (CloudbreakOrchestratorException e) {
            throw new CloudbreakException(e);
        }
    }

    public void addClusterContainers(ClusterScalingContext context) throws CloudbreakException {
        try {
            Stack stack = stackRepository.findOneWithLists(context.getStackId());
            initializeClusterContainers(stack, cloudPlatform(context), true, context.getUpscaleCandidateAddresses());
        } catch (CloudbreakOrchestratorCancelledException e) {
            throw new CancellationException(e.getMessage());
        } catch (CloudbreakOrchestratorException e) {
            throw new CloudbreakException(e);
        }
    }

    private String cloudPlatform(DefaultFlowContext context) {
        String cloudPlatform = NONE;
        if (context.getCloudPlatform() != null) {
            cloudPlatform = context.getCloudPlatform().value();
        }
        return cloudPlatform;
    }

    private List<ContainerInfo> initializeClusterContainers(Stack stack, String cloudPlatform, Boolean add, Set<String> candidateAddresses)
            throws CloudbreakException, CloudbreakOrchestratorException {

        Orchestrator orchestrator = stack.getOrchestrator();
        OrchestrationCredential credential = new OrchestrationCredential(orchestrator.getApiEndpoint(), orchestrator.getAttributes().getMap());

        ContainerOrchestrator containerOrchestrator = containerOrchestratorResolver.get(orchestrator.getType());

        String gatewayHostname = "";
        if (stack.getInstanceGroups() != null && !stack.getInstanceGroups().isEmpty()) {
            InstanceMetaData gatewayInstance = stack.getGatewayInstanceGroup().getInstanceMetaData().iterator().next();
            gatewayHostname = gatewayInstance.getDiscoveryName();
        }

        List<ContainerInfo> containers = new ArrayList<>();
        if (!add) {
            Cluster cluster = clusterService.retrieveClusterByStackId(stack.getId());

            if ("SWARM".equals(orchestrator.getType())) {
                ContainerConstraint registratorConstraint = getRegistratorConstraint(gatewayHostname);
                containers.addAll(containerOrchestrator.runContainer(containerConfigService.get(stack, REGISTRATOR), credential, registratorConstraint,
                        stackDeletionBasedExitCriteriaModel(stack.getId())));
            }

            ContainerConstraint ambariServerDbConstraint = getAmbariServerDbConstraint(gatewayHostname);
            ContainerInfo dbContainer = containerOrchestrator.runContainer(containerConfigService.get(stack, AMBARI_DB), credential, ambariServerDbConstraint,
                    stackDeletionBasedExitCriteriaModel(stack.getId())).get(0);
            containers.add(dbContainer);

            ContainerConstraint ambariServerConstraint = getAmbariServerConstraint(dbContainer.getHost(), gatewayHostname, cloudPlatform);
            containers.addAll(containerOrchestrator.runContainer(containerConfigService.get(stack, AMBARI_SERVER), credential, ambariServerConstraint,
                    stackDeletionBasedExitCriteriaModel(stack.getId())));

            if (cluster.isSecure()) {
                ContainerConstraint havegedConstraint = getHavegedConstraint(gatewayHostname);
                containers.addAll(containerOrchestrator.runContainer(containerConfigService.get(stack, HAVEGED), credential, havegedConstraint,
                        stackDeletionBasedExitCriteriaModel(stack.getId())));

                ContainerConstraint kerberosServerConstraint = getKerberosServerConstraint(cluster, gatewayHostname);
                containers.addAll(containerOrchestrator.runContainer(containerConfigService.get(stack, KERBEROS), credential, kerberosServerConstraint,
                        stackDeletionBasedExitCriteriaModel(stack.getId())));
            }
        }

        containers.addAll(runAmbariAgentContainers(add, candidateAddresses, containerOrchestrator, cloudPlatform, stack, credential));

        if ("SWARM".equals(orchestrator.getType())) {
            List<String> hosts = getHosts(add, stack, candidateAddresses);
            ContainerConstraint consulWatchConstraint = getConsulWatchConstraint(hosts);
            containers.addAll(containerOrchestrator.runContainer(containerConfigService.get(stack, CONSUL_WATCH), credential, consulWatchConstraint,
                    stackDeletionBasedExitCriteriaModel(stack.getId())));

            ContainerConstraint logrotateConstraint = getLogrotateConstraint(hosts);
            containers.addAll(containerOrchestrator.runContainer(containerConfigService.get(stack, LOGROTATE), credential, logrotateConstraint,
                    stackDeletionBasedExitCriteriaModel(stack.getId())));
        }

        return containers;
    }

    private ContainerConstraint getRegistratorConstraint(String gatewayHostname) {
        return new ContainerConstraint.Builder()
                .withName(REGISTRATOR.getName())
                .networkMode(HOST_NETWORK_MODE)
                .instances(1)
                .addVolumeBindings(ImmutableMap.of("/var/run/docker.sock", "/tmp/docker.sock"))
                .addHosts(ImmutableList.of(gatewayHostname))
                .cmd(new String[]{"consul://127.0.0.1:8500"})
                .build();
    }

    private ContainerConstraint getAmbariServerDbConstraint(String gatewayHostname) {
        ContainerConstraint.Builder builder = new ContainerConstraint.Builder()
                .withName(AMBARI_DB.getName())
                .instances(1)
                .networkMode(HOST_NETWORK_MODE)
                .addVolumeBindings(ImmutableMap.of("/data/ambari-server/pgsql/data", "/var/lib/postgresql/data",
                        HOST_VOLUME_PATH + "/consul-watch", HOST_VOLUME_PATH + "/consul-watch"))
                .addEnv(ImmutableMap.of("POSTGRES_PASSWORD", "bigdata", "POSTGRES_USER", "ambari"));
        if (gatewayHostname != null) {
            builder.addHosts(ImmutableList.of(gatewayHostname));
        }
        return builder.build();
    }

    private ContainerConstraint getAmbariServerConstraint(String dbHostname, String gatewayHostname, String cloudPlatform) {
        ContainerConstraint.Builder builder = new ContainerConstraint.Builder()
                .withName(AMBARI_SERVER.getName())
                .instances(1)
                .networkMode(HOST_NETWORK_MODE)
                .tcpPortBinding(new TcpPortBinding(AMBARI_PORT, "0.0.0.0", AMBARI_PORT))
                .addVolumeBindings(ImmutableMap.of(HOST_VOLUME_PATH, CONTAINER_VOLUME_PATH, "/etc/krb5.conf", "/etc/krb5.conf"))
                .addEnv(ImmutableMap.of("SERVICE_NAME", "ambari-8080"))
                .cmd(new String[]{String.format("systemd.setenv=POSTGRES_DB=%s systemd.setenv=CLOUD_PLATFORM=%s", dbHostname, cloudPlatform)});
        if (gatewayHostname != null) {
            builder.addHosts(ImmutableList.of(gatewayHostname));
        }
        return builder.build();
    }

    private ContainerConstraint getHavegedConstraint(String gatewayHostname) {
        return new ContainerConstraint.Builder()
                .withName(HAVEGED.getName())
                .instances(1)
                .addHosts(ImmutableList.of(gatewayHostname))
                .build();
    }

    private ContainerConstraint getKerberosServerConstraint(Cluster cluster, String gatewayHostname) {
        KerberosConfiguration kerberosConf = new KerberosConfiguration(cluster.getKerberosMasterKey(), cluster.getKerberosAdmin(),
                cluster.getKerberosPassword());

        Map<String, String> env = new HashMap<>();
        env.put("SERVICE_NAME", KERBEROS.getName());
        env.put("NAMESERVER_IP", "127.0.0.1");
        env.put("REALM", REALM);
        env.put("DOMAIN_REALM", DOMAIN_REALM);
        env.put("KERB_MASTER_KEY", kerberosConf.getMasterKey());
        env.put("KERB_ADMIN_USER", kerberosConf.getUser());
        env.put("KERB_ADMIN_PASS", kerberosConf.getPassword());

        return new ContainerConstraint.Builder()
                .withName(KERBEROS.getName())
                .instances(1)
                .networkMode(HOST_NETWORK_MODE)
                .addVolumeBindings(ImmutableMap.of(HOST_VOLUME_PATH, CONTAINER_VOLUME_PATH, "/etc/krb5.conf", "/etc/krb5.conf"))
                .addHosts(ImmutableList.of(gatewayHostname))
                .addEnv(env)
                .build();
    }

    private List<ContainerInfo> runAmbariAgentContainers(Boolean add, Set<String> candidateAddresses, ContainerOrchestrator orchestrator,
                                                         String cloudPlatform, Stack stack, OrchestrationCredential cred) throws CloudbreakOrchestratorException {
        List<ContainerInfo> containers = new ArrayList<>();
        for (HostGroup hostGroup : hostGroupRepository.findHostGroupsInCluster(stack.getCluster().getId())) {
            ContainerConstraint ambariAgentConstraint = getAmbariAgentConstraint(cloudPlatform, hostGroup.getConstraint(), add, candidateAddresses);
            containers.addAll(orchestrator.runContainer(containerConfigService.get(stack, AMBARI_AGENT), cred, ambariAgentConstraint,
                    stackDeletionBasedExitCriteriaModel(stack.getId())));
        }
        return containers;
    }

    private Map<String, String> getDataVolumeBinds(long volumeCount) {
        Map<String, String> dataVolumeBinds = new HashMap<>();
        for (int i = 1; i <= volumeCount; i++) {
            String dataVolumePath = VolumeUtils.VOLUME_PREFIX + i;
            dataVolumeBinds.put(dataVolumePath, dataVolumePath);
        }
        return dataVolumeBinds;
    }

    private ContainerConstraint getAmbariAgentConstraint(String cloudPlatform, Constraint hgConstraint, Boolean add, Set<String> candidateAddresses) {
        ContainerConstraint.Builder builder = new ContainerConstraint.Builder()
                .withName(AMBARI_AGENT.getName())
                .networkMode(HOST_NETWORK_MODE)
                .cmd(new String[]{String.format("systemd.setenv=CLOUD_PLATFORM=%s", cloudPlatform)});
        if (hgConstraint.getInstanceGroup() != null) {
            InstanceGroup instanceGroup = hgConstraint.getInstanceGroup();
            int volumeCount = instanceGroup.getTemplate().getVolumeCount();
            Map<String, String> dataVolumeBinds = getDataVolumeBinds(volumeCount);
            ImmutableMap<String, String> volumeBinds = ImmutableMap.of("/data/jars", "/data/jars", HOST_VOLUME_PATH, CONTAINER_VOLUME_PATH);
            dataVolumeBinds.putAll(volumeBinds);
            builder.addVolumeBindings(dataVolumeBinds);
            builder.addHosts(getHosts(add, candidateAddresses, instanceGroup));
        }
        if (hgConstraint.getConstraintTemplate() != null) {
            builder.cpus(hgConstraint.getConstraintTemplate().getCpu());
            builder.memory(hgConstraint.getConstraintTemplate().getMemory());
            builder.instances(hgConstraint.getHostCount());
            builder.withDiskSize(hgConstraint.getConstraintTemplate().getDisk());
        }
        return builder.build();
    }

    private ContainerConstraint getConsulWatchConstraint(List<String> hosts) {
        return new ContainerConstraint.Builder()
                .withName(CONSUL_WATCH.getName())
                .addEnv(ImmutableMap.of("CONSUL_HOST", "127.0.0.1"))
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
        for (InstanceMetaData instanceMetaData : instanceMetaDataRepository.findAliveInstancesInInstanceGroup(instanceGroup.getId())) {
            String privateIp = instanceMetaData.getPrivateIp();
            if (!add || candidateAddresses.contains(privateIp)) {
                hosts.add(instanceMetaData.getDiscoveryName());
            }
        }
        return hosts;
    }

    private List<Container> convert(List<ContainerInfo> containerInfo, Cluster cluster) {
        List<Container> containers = new ArrayList<>();
        for (ContainerInfo source : containerInfo) {
            Container container = conversionService.convert(source, Container.class);
            container.setCluster(cluster);
            containers.add(container);
        }
        return containers;
    }
}
