package com.chatpass.platform.workflow;

import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class ActionRegistry {

    private final Map<WorkflowType, WorkflowAction> actions = new EnumMap<>(WorkflowType.class);

    public ActionRegistry(List<WorkflowAction> workflowActions) {
        workflowActions.forEach(action -> actions.put(action.type(), action));
    }

    public Optional<WorkflowAction> find(WorkflowType type) {
        return Optional.ofNullable(actions.get(type));
    }
}
