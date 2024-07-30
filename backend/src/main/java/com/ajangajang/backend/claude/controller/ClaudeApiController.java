package com.ajangajang.backend.claude.controller;

import com.ajangajang.backend.claude.dto.PromptConditionDto;
import com.ajangajang.backend.claude.model.service.ClaudeApi;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ClaudeApiController {

    private final ClaudeApi claudeApi;

    @PostMapping("/api/claude")
    public ResponseEntity<?> getBoardContent(@RequestParam("tone") String tone,
                                             @RequestBody PromptConditionDto condition) {
        String result = claudeApi.callClaudeApi(tone, condition);
        return ResponseEntity.ok(Map.of("content", result));
    }

}