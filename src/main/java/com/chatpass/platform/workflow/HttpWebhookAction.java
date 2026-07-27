package com.chatpass.platform.workflow;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class HttpWebhookAction implements WorkflowAction {

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public WorkflowType type() {
        return WorkflowType.HTTP_WEBHOOK;
    }

    @Override
    public ActionResult execute(ActionExecutionRequest request) {
        Map<String, Object> parameters = request.getRule() == null || request.getRule().getWorkflow() == null
            ? Map.of()
            : request.getRule().getWorkflow().getParameters();
        String url = required(parameters, "url");
        HttpMethod method = HttpMethod.valueOf(String.valueOf(parameters.getOrDefault("method", "POST")));
        HttpHeaders headers = headers(parameters);
        Object body = parameters.getOrDefault("body", request.getMessage());

        ResponseEntity<String> response = restTemplate.exchange(
            url,
            method,
            new HttpEntity<>(body, headers),
            String.class
        );

        ActionResult result = ActionResult.success(response.getBody());
        result.getOutputs().put("statusCode", response.getStatusCode().value());
        result.getOutputs().put("body", response.getBody());
        result.getOutputs().put("headers", response.getHeaders());
        return result;
    }

    private HttpHeaders headers(Map<String, Object> parameters) {
        HttpHeaders headers = new HttpHeaders();
        Object configuredHeaders = parameters.get("headers");
        if (configuredHeaders instanceof Map<?, ?> map) {
            map.forEach((key, value) -> headers.add(String.valueOf(key), String.valueOf(value)));
        }
        return headers;
    }

    private String required(Map<String, Object> parameters, String key) {
        Object value = parameters.get(key);
        if (value == null || String.valueOf(value).isBlank()) {
            throw new IllegalArgumentException("HTTP webhook action requires parameter: " + key);
        }
        return String.valueOf(value);
    }
}
