package com.chatpass.platform.api;

import com.chatpass.platform.routing.TriggerRule;
import com.chatpass.platform.routing.repository.TriggerRuleRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/rules")
public class RuleController {

    private final TriggerRuleRepository ruleRepository;

    public RuleController(TriggerRuleRepository ruleRepository) {
        this.ruleRepository = ruleRepository;
    }

    @GetMapping
    public List<TriggerRule> list() {
        return ruleRepository.findAll();
    }

    @GetMapping("/{id}")
    public TriggerRule get(@PathVariable String id) {
        return ruleRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rule not found: " + id));
    }

    @PostMapping
    public TriggerRule save(@RequestBody TriggerRule rule) {
        return ruleRepository.save(rule);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        ruleRepository.deleteById(id);
    }
}
