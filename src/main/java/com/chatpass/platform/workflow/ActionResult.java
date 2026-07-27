package com.chatpass.platform.workflow;

import java.util.LinkedHashMap;
import java.util.Map;

public class ActionResult {

    private ActionStatus status;

    private String outputText;

    private Map<String, Object> outputs = new LinkedHashMap<>();

    private String errorMessage;

    public static ActionResult success(String outputText) {
        ActionResult result = new ActionResult();
        result.setStatus(ActionStatus.SUCCESS);
        result.setOutputText(outputText);
        return result;
    }

    public static ActionResult skipped(String reason) {
        ActionResult result = new ActionResult();
        result.setStatus(ActionStatus.SKIPPED);
        result.setErrorMessage(reason);
        return result;
    }

    public static ActionResult failed(String message) {
        ActionResult result = new ActionResult();
        result.setStatus(ActionStatus.FAILED);
        result.setErrorMessage(message);
        return result;
    }

    public ActionStatus getStatus() {
        return status;
    }

    public void setStatus(ActionStatus status) {
        this.status = status;
    }

    public String getOutputText() {
        return outputText;
    }

    public void setOutputText(String outputText) {
        this.outputText = outputText;
    }

    public Map<String, Object> getOutputs() {
        return outputs;
    }

    public void setOutputs(Map<String, Object> outputs) {
        this.outputs = outputs == null ? new LinkedHashMap<>() : outputs;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
