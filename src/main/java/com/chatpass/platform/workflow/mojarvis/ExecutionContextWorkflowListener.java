package com.chatpass.platform.workflow.mojarvis;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class ExecutionContextWorkflowListener implements WorkFlowEventListener {

    public static final String EVENTS_VARIABLE = "_workflowEvents";

    @Override
    public void onEvent(WorkflowEvent event, Workflow workflow) {
        workflow.getExecutionContext().variables()
            .computeIfAbsent(EVENTS_VARIABLE, key -> new ArrayList<Map<String, Object>>());
        Object events = workflow.getExecutionContext().variables().get(EVENTS_VARIABLE);
        if (events instanceof List<?> list) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> typedEvents = (List<Map<String, Object>>) list;
            typedEvents.add(eventPayload(event));
        }
    }

    private Map<String, Object> eventPayload(WorkflowEvent event) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", event.getType().name());
        payload.put("workflowId", event.getWorkflowId());
        payload.put("nodeId", event.getNodeId());
        payload.put("message", event.getMessage());
        payload.put("createdAt", event.getCreatedAt().toString());
        return payload;
    }
}
