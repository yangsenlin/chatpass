package com.chatpass.platform.mqtt.control;

import com.chatpass.platform.mqtt.MqttBrokerProperties;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "chatpass.mqtt", name = "enabled", havingValue = "true", matchIfMissing = true)
public class LocalMqttClusterRegistrar {

    private final MqttBrokerProperties properties;
    private final MqttClusterControlService clusterControlService;

    public LocalMqttClusterRegistrar(MqttBrokerProperties properties, MqttClusterControlService clusterControlService) {
        this.properties = properties;
        this.clusterControlService = clusterControlService;
    }

    @PostConstruct
    public void register() {
        MqttCluster cluster = new MqttCluster();
        cluster.setClusterId(properties.getClusterId());
        cluster.setRegion(properties.getRegion());
        cluster.setZone(properties.getZone());
        cluster.setEndpoint(properties.getPublicEndpoint());
        cluster.setStatus(MqttClusterStatus.ACTIVE);
        clusterControlService.saveCluster(cluster);
    }
}
