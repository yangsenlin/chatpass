package com.chatpass.platform.workflow.engine.node;

import com.chatpass.platform.workflow.engine.NodeExecutionResult;
import com.chatpass.platform.workflow.engine.NodeType;
import com.chatpass.platform.workflow.engine.WorkflowExecutionContext;
import com.chatpass.platform.workflow.engine.WorkflowNodeDefinition;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class LoopNodeExecutor implements WorkflowNodeExecutor {

    @Override
    public NodeType type() {
        return NodeType.LOOP;
    }

    @Override
    public NodeExecutionResult execute(WorkflowNodeDefinition node, WorkflowExecutionContext context) {
        Object values = node.getParameters().get("values");
        int count = resolveCount(node, values);
        String outputPrefix = String.valueOf(node.getParameters().getOrDefault("outputPrefix", "loop"));

        Map<String, Object> outputs = new LinkedHashMap<>();
        outputs.put("count", count);
        if (values instanceof List<?> list) {
            for (int i = 0; i < list.size(); i++) {
                outputs.put(outputPrefix + "." + i, list.get(i));
            }
        }
        return NodeExecutionResult.completed(outputs);
    }

    private int resolveCount(WorkflowNodeDefinition node, Object values) {
        if (values instanceof List<?> list) {
            return list.size();
        }
        Object times = node.getParameters().get("times");
        if (times instanceof Number number) {
            return number.intValue();
        }
        if (times != null) {
            return Integer.parseInt(String.valueOf(times));
        }
        return 0;
    }
}
