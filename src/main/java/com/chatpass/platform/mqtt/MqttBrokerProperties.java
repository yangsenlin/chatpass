package com.chatpass.platform.mqtt;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "chatpass.mqtt")
public class MqttBrokerProperties {

    private boolean enabled = true;

    private String nodeId = "chatpass-node-1";

    private String host = "0.0.0.0";

    private int port = 1883;

    private boolean websocketEnabled = true;

    private int websocketPort = 8083;

    private String websocketPath = "/mqtt";

    private boolean clusterEnabled = true;

    private boolean offlineEnabled = true;

    private boolean bridgeIngress = true;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
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
