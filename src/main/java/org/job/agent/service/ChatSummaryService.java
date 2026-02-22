package org.job.agent.service;

import dev.langchain4j.model.chat.response.ChatResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.job.agent.model.JobQADto;
import org.job.agent.model.JobQAMessage;
import org.job.agent.model.JobQASummaryDto;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatSummaryService {

    private final AiAssist aiAssist;
    private final KafkaProducer kafkaProducer;

    private String PROMPT_SUMMARY = """
            You are an AI assistant tasked with summarizing a conversation.
        Create a concise, factual summary of the interaction by focusing on:
            - The user’s current question
            - The assistant’s latest answer
            - Any relevant context from the previous chat history
        The summary should:
            - Preserve important details, decisions, and outcomes
            - Exclude small talk, repetition, or irrelevant information
            - Be written in clear, neutral language
            - Be suitable for use as context in a future conversation turn
        Current Question:
        %s
        Assistant Answer:
        %s
        Previous Chat History:
        {Fetch previous chat summary from tool using jobId = %s and userId = %s}
        Output:
        A brief paragraph summarizing the conversation so far.
        """;

    @Async
    public void generateSummary(String jobId, String userId,
                                String question, String answer) {
        String prompt = String.format(PROMPT_SUMMARY, question, answer, jobId, userId);
        aiAssist.chat(prompt)
                .onPartialResponse(s -> log.debug("Summary chunk: {}", s))
                .onCompleteResponse(summary -> completedChatSummary(jobId, userId, summary, question, answer))
                .onError(throwable -> log.error("Error: ", throwable))
                .start();
    }

    private void completedChatSummary(String jobId, String userId,
                                      ChatResponse response, String question, String answer) {
        log.info("Completed summary jobId: {}, userId {}, summary: {}", jobId, userId, response.aiMessage().text());
        List<JobQADto> qa = List.of(
                JobQADto.builder().qa(question).role("human").build(),
                JobQADto.builder().qa(answer).role("ai").build()
        );
        kafkaProducer.sendQA(JobQAMessage.builder()
                .jobId(jobId)
                .userId(userId)
                .summary(JobQASummaryDto.builder()
                        .summary(response.aiMessage().text())
                        .build())
                .qa(qa)
                .build());
    }
}
