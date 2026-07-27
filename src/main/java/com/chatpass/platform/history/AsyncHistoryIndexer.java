package com.chatpass.platform.history;

import com.chatpass.platform.message.MessageProcessingResult;
import jakarta.annotation.PreDestroy;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class AsyncHistoryIndexer {

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final RestTemplate restTemplate = new RestTemplate();
    private final HistoryElasticsearchProperties properties;

    public AsyncHistoryIndexer(HistoryElasticsearchProperties properties) {
        this.properties = properties;
    }

    public void index(MessageProcessingResult result) {
        if (!properties.isEnabled() || result == null || result.getMessage() == null) {
            return;
        }
        executorService.submit(() -> doIndex(result));
    }

    @PreDestroy
    public void stop() {
        executorService.shutdownNow();
    }

    private void doIndex(MessageProcessingResult result) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("message", result.getMessage());
        payload.put("decision", result.getDecision());
        payload.put("actionResult", result.getActionResult());
        payload.put("outboundMessage", result.getOutboundMessage());
        payload.put("indexedAt", Instant.now().toString());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        restTemplate.postForEntity(
            properties.getEndpoint() + "/" + properties.getIndex() + "/_doc",
            new HttpEntity<>(payload, headers),
            String.class
        );
    }
}
