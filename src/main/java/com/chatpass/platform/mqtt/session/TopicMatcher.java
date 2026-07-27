package com.chatpass.platform.mqtt.session;

public final class TopicMatcher {

    private TopicMatcher() {
    }

    public static boolean matches(String filter, String topic) {
        String[] filterParts = filter.split("/");
        String[] topicParts = topic.split("/");

        for (int i = 0; i < filterParts.length; i++) {
            String filterPart = filterParts[i];
            if ("#".equals(filterPart)) {
                return i == filterParts.length - 1;
            }
            if (i >= topicParts.length) {
                return false;
            }
            if (!"+".equals(filterPart) && !filterPart.equals(topicParts[i])) {
                return false;
            }
        }
        return filterParts.length == topicParts.length;
    }
}
