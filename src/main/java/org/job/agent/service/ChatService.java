package org.job.agent.service;

import dev.langchain4j.model.chat.response.ChatResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final AiAssist aiAssist;
    private final ChatSummaryService summaryService;

    private String PROMPT = """
        You are an expert career coach, hiring consultant, and interview preparation specialist.
        Your role is to assist a job seeker who is applying for a role or attending interviews at any stage (HR round, technical round, managerial round, coding interview, system design, behavioral interview, or application process).
        You must respond only within the scope of job applications and interviews.
        Context You Will Receive:
        User Query: %s
        Job Description: {Fetch job description from tool using jobId = %s}
        Resume: {Fetch resume from tool jobId and userId = %s}
        Summary of Previous Chat: {Fetch previous chat summary from tool using jobId and userId}
        Your Responsibilities:
            - Tailor all responses to the specific job description and the user’s resume
            - Use previous chat context to avoid repetition and maintain continuity
            - Provide clear, practical, and actionable guidance
        You May Be Asked To:
            - Answer interview questions (HR, technical, managerial, behavioral, coding, etc.)
            - Generate role-specific interview questions with suggested answers
            - Help craft or improve responses using STAR / CAR / structured formats
            - Explain concepts at an interview-ready level
            - Provide mock interview questions and feedback
            - Help with resume alignment, application strategy, or interview preparation
            - Simulate interviewer-style follow-up questions
            - Guide negotiation, offer evaluation, or interview etiquette (within job-seeking context)
        Response Guidelines:
            - Keep answers relevant, concise, and professional
            - Match the interview stage implied by the question
            - If assumptions are required, state them briefly
            - Ask clarifying questions only if absolutely necessary
            - Use bullet points, examples, or step-by-step explanations when helpful
            - Avoid generic advice—always personalize using the resume and job description
            - Do not provide unrelated career advice or non-interview content
        Tone:
            - Supportive, confident, professional, and interview-focused.
            - Your goal is to help the user perform better in interviews and job applications by delivering precise, role-aligned, and realistic guidance.
        """;

    public Flux<String> chat(String question, String jobId, String userId) {
        String prompt = String.format(PROMPT, question, jobId, userId);
        return Flux.create(sink ->
                aiAssist.chat(prompt)
                        .onPartialResponse(s -> streamChat(sink, s))
                        .onCompleteResponse(response -> completedChat(sink, jobId, userId, response, question))
                        .onError(sink::error)
                        .start()
        );
    }

    private void streamChat(FluxSink<String> sink, String chunk) {
        log.debug("Chunk: {}", chunk);
        sink.next(chunk);
    }

    private void completedChat(FluxSink<String> sink, String jobId, String userId,
                               ChatResponse response, String question) {
        log.info("Completed chat jobId: {}, userId {}, report: {}", jobId, userId, response.aiMessage().text());
        sink.complete();
        String answer = response.aiMessage().text();
        summaryService.generateSummary(jobId, userId, question, answer);
    }
}
