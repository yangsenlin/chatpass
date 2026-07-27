package com.chatpass.platform.api;

import com.chatpass.platform.stream.RedisStreamStore;
import com.chatpass.platform.stream.StreamMessage;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/streams")
public class StreamController {

    private final RedisStreamStore streamStore;

    public StreamController(RedisStreamStore streamStore) {
        this.streamStore = streamStore;
    }

    @GetMapping("/{streamId}")
    public Map<Object, Object> state(@PathVariable String streamId) {
        return streamStore.state(streamId);
    }

    @GetMapping("/{streamId}/content")
    public Map<String, Object> content(@PathVariable String streamId) {
        return Map.of("streamId", streamId, "content", streamStore.content(streamId));
    }

    @GetMapping("/{streamId}/chunks")
    public List<StreamMessage> chunks(
        @PathVariable String streamId,
        @RequestParam(defaultValue = "-1") int afterSequence
    ) {
        return streamStore.chunksAfter(streamId, afterSequence);
    }
}
