package org.job.agent.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.job.agent.model.ChatRequest;
import org.job.agent.model.LoggedInUser;
import org.job.agent.service.ChatService;
import org.job.agent.service.ResumeAnalysisService;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/jobs")
@Tag(name = "Job API", description = "Job Streaming Endpoints")
@PreAuthorize("hasRole('ai-user')")
public class JobController {

    private final ResumeAnalysisService resumeAnalysisService;
    private final ChatService chatService;

    @Operation(
            summary = "Match profile",
            description = "Match profile with job description. The results will be a stream"
    )
    @GetMapping(value = "/{jobId}/match", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> match(@PathVariable String jobId,
                      @AuthenticationPrincipal LoggedInUser user) {
        return resumeAnalysisService.matchResume(jobId, user.getId());
    }

    @Operation(
            summary = "Chat with AI Assist",
            description = "Chat with AI Assist with the context of user profile and job description. The results will be a stream"
    )
    @PostMapping(value = "/{jobId}/questions", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chat(@PathVariable String jobId,
                           @RequestBody @Valid ChatRequest chatRequest,
                           @AuthenticationPrincipal LoggedInUser user) {
        return chatService.chat(chatRequest.getQuestion(), jobId, user.getId());
    }
}
