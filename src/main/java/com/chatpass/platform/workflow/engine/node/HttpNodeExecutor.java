package com.chatpass.platform.workflow.engine.node;

import com.chatpass.platform.workflow.engine.NodeExecutionResult;
import com.chatpass.platform.workflow.engine.NodeType;
import com.chatpass.platform.workflow.engine.WorkflowExecutionContext;
import com.chatpass.platform.workflow.engine.WorkflowNodeDefinition;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class HttpNodeExecutor implements WorkflowNodeExecutor {

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public NodeType type() {
        return NodeType.HTTP;
    }

    @Override
    public NodeExecutionResult execute(WorkflowNodeDefinition node, WorkflowExecutionContext context) {
        String url = required(node, "url");
        HttpMethod method = HttpMethod.valueOf(String.valueOf(node.getParameters().getOrDefault("method", "GET")));
        HttpHeaders headers = headers(node);
        Object body = node.getParameters().get("body");

        ResponseEntity<String> response = restTemplate.exchange(
            url,
            method,
            new HttpEntity<>(body, headers),
            String.class
        );

        Map<String, Object> outputs = new LinkedHashMap<>();
        outputs.put("statusCode", response.getStatusCode().value());
        outputs.put("body", response.getBody());
        outputs.put("headers", response.getHeaders());
        return NodeExecutionResult.completed(outputs);
    }

    private HttpHeaders headers(WorkflowNodeDefinition node) {
        HttpHeaders headers = new HttpHeaders();
        Object configuredHeaders = node.getParameters().get("headers");
        if (configuredHeaders instanceof Map<?, ?> map) {
            map.forEach((key, value) -> headers.add(String.valueOf(key), String.valueOf(value)));
        }
        return headers;
    }

    private String required(WorkflowNodeDefinition node, String key) {
        Object value = node.getParameters().get(key);
        if (value == null || String.valueOf(value).isBlank()) {
            throw new IllegalArgumentException("HTTP node requires parameter: " + key);
        }
        return String.valueOf(value);
    }
}
