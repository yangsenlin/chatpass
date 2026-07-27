package com.chatpass.platform.mqtt;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "chatpass.mqtt")
public class MqttBrokerProperties {

    private boolean enabled = true;

    private String clusterId = "mqtt-cluster-local";

    private String region = "local";

    private String zone = "local-a";

    private String publicEndpoint = "ws://localhost:8083/mqtt";

    private String nodeId = "chatpass-node-1";

    private String host = "0.0.0.0";

    private int port = 1883;

    private boolean websocketEnabled = true;

    private int websocketPort = 8083;

    private String websocketPath = "/mqtt";

    private boolean clusterEnabled = true;

    private String clusterBusChannelPrefix = "chatpass:mqtt:cluster";

    private long heartbeatIntervalMs = 10000;

    private long heartbeatTtlMs = 30000;

    private int routeShardCount = 1024;

    private boolean offlineEnabled = true;

    private boolean bridgeIngress = true;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public String getPublicEndpoint() {
        return publicEndpoint;
    }

    public void setPublicEndpoint(String publicEndpoint) {
        this.publicEndpoint = publicEndpoint;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isWebsocketEnabled() {
        return websocketEnabled;
    }

    public void setWebsocketEnabled(boolean websocketEnabled) {
        this.websocketEnabled = websocketEnabled;
    }

    public int getWebsocketPort() {
        return websocketPort;
    }

    public void setWebsocketPort(int websocketPort) {
        this.websocketPort = websocketPort;
    }

    public String getWebsocketPath() {
        return websocketPath;
    }

    public void setWebsocketPath(String websocketPath) {
        this.websocketPath = websocketPath;
    }

    public boolean isClusterEnabled() {
        return clusterEnabled;
    }

    public void setClusterEnabled(boolean clusterEnabled) {
        this.clusterEnabled = clusterEnabled;
    }

    public String getClusterBusChannelPrefix() {
        return clusterBusChannelPrefix;
    }

    public void setClusterBusChannelPrefix(String clusterBusChannelPrefix) {
        this.clusterBusChannelPrefix = clusterBusChannelPrefix;
    }

    public long getHeartbeatIntervalMs() {
        return heartbeatIntervalMs;
    }

    public void setHeartbeatIntervalMs(long heartbeatIntervalMs) {
        this.heartbeatIntervalMs = heartbeatIntervalMs;
    }

    public long getHeartbeatTtlMs() {
        return heartbeatTtlMs;
    }

    public void setHeartbeatTtlMs(long heartbeatTtlMs) {
        this.heartbeatTtlMs = heartbeatTtlMs;
    }

    public int getRouteShardCount() {
        return routeShardCount;
    }

    public void setRouteShardCount(int routeShardCount) {
        this.routeShardCount = routeShardCount;
    }

    public boolean isOfflineEnabled() {
        return offlineEnabled;
    }

    public void setOfflineEnabled(boolean offlineEnabled) {
        this.offlineEnabled = offlineEnabled;
    }

    public boolean isBridgeIngress() {
        return bridgeIngress;
    }

    public void setBridgeIngress(boolean bridgeIngress) {
        this.bridgeIngress = bridgeIngress;
    }
}
