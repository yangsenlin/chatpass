package com.chatpass.platform.api;

import com.chatpass.platform.mqtt.control.MqttClusterControlService;
import com.chatpass.platform.mqtt.control.MqttEndpointResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tenants/{tenantId}/mqtt")
public class TenantMqttEndpointController {

    private final MqttClusterControlService clusterControlService;

    public TenantMqttEndpointController(MqttClusterControlService clusterControlService) {
        this.clusterControlService = clusterControlService;
    }

    @GetMapping("/endpoint")
    public MqttEndpointResponse endpoint(
        @PathVariable String tenantId,
        @RequestParam(required = false) String conversationId,
        @RequestParam(required = false) String streamId
    ) {
        return clusterControlService.endpoint(tenantId, conversationId, streamId);
    }
}
