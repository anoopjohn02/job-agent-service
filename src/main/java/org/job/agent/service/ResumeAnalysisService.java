package org.job.agent.service;

import dev.langchain4j.model.chat.response.ChatResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.job.agent.model.JobResumeAnalysisDto;
import org.job.agent.model.JobResumeAnalysisMessage;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeAnalysisService {

    private final AiAssist aiAssist;
    private final KafkaProducer kafkaProducer;

    private String PROMPT = """
        You are an expert recruiter and professional resume writer in Sweden who personally authored the given job description.
        Your task is to evaluate how well the provided resume matches the job description, and then rebuild the resume to maximize its relevance, clarity, and chances of shortlisting.
        Think like a recruiter who knows exactly what hiring managers want to see.
        Instructions:
        Analyze the job description and resume, then perform the following:
        1. Overall Match Evaluation
        	- Assign a Job Fit Score (0–100) indicating how well the resume aligns with the job requirements.
        	- Explain the reasoning behind the score.
        	- State the success likelihood of this resume being shortlisted (e.g., High chance of interview, Moderate fit, Low relevance).
        2. Relevance & Skill Alignment
        	- Identify which parts of the resume are directly relevant to the job and which are not.
        	- Highlight key strengths that align with the job description.
        	- Point out irrelevant, outdated, or redundant content and suggest removing or simplifying it.
        3. Missing & Weak Skills
        	- List hard skills (tools, technologies, frameworks, certifications) and soft skills (communication, problem-solving, teamwork, etc.) mentioned in the JD but missing or underrepresented in the resume.
        	- Recommend how to incorporate these skills naturally into the rewritten version.
        4. Improvement Areas
        	- Suggest specific ways to enhance bullet points, making them results-driven, quantifiable, and action-oriented.
        	- Replace vague statements with strong verbs and measurable achievements.
        	- Suggest removal or revision of low-impact or irrelevant points.
        5. Keyword & ATS Optimization
        	- Identify important keywords and phrases from the job description.
        	- Indicate which are missing from the current resume.
        	- Suggest how to integrate them to improve ATS ranking and recruiter search visibility.
        6. Formatting & Layout Recommendations
        	- Provide suggestions for layout, structure, and formatting to improve readability and professionalism.
        	- Recommend section order, bullet structure, and whitespace balance.
        	- Suggest additional sections like Key Skills, Selected Projects, or Achievements if beneficial.
        7. Rebuilt Resume (High-Impact Version)
            Based on the analysis above, rebuild the resume with the following goals:
                - Minimum 5 achievments in bullet points for first 4 companies aligned to the job description.
                - Minimum 5 roles and responsibilities in bullet points for selected projects that aligned to the job description.
                - Focus on relevant experience, projects, and achievements aligned to the job description.
                - Integrate missing or weak skills naturally throughout the resume.
                - Maintain a clean, recruiter-friendly layout with strong action verbs and quantifiable results.
                - Ensure it passes ATS filters and reads persuasively to human reviewers.
            Output a complete, ready-to-use resume with:
                - Professional summary tailored to the job
                - Optimized skill and experience sections
                - Highlighted relevant projects or accomplishments
                - Updated bullet points emphasizing measurable outcomes
        8. Final Recruiter Verdict
        	Conclude with:
                - Top 3 priorities for further improvement (if any)
                - Predicted success rate of the rebuilt resume for this job
                - A one-line recruiter summary: “Ready to submit”, “Needs minor refinement”, or “Requires additional content.”
        9. Tailored Cover Letter Generation in humanized form
            After rebuilding the resume, generate a professional cover letter that is specifically tailored to the provided job description. The cover letter should:
                - Be personalized for the company, job title, and key qualifications.
                - Highlight the most relevant experiences, skills, and achievements from the rebuilt resume.
                - Emphasize fit, motivation, and alignment with the company’s goals or culture.
                - Maintain a professional yet conversational tone, concise (3–4 short paragraphs).
                - Include a strong closing statement that expresses enthusiasm and invites next steps (e.g., interview).
            Output a complete, ready-to-use cover letter that complements the rebuilt resume and maximizes interview success probability.
        Input Format:
        Job description:
        Fetch job description from tool where jobId = %s
        Resume:
        Fetch resume from tool using jobId and userId = %s
        Output Format:
        Provide results in this order:
            1. Job Fit Score & Summary
            2. Missing Skills & Gaps
            3. Improvement Suggestions
            4. Keyword Optimization
            5. Layout Recommendations
            6. Rebuilt Resume (Full Version)
            7. Final Recruiter Verdict
            8. Cover Letter
       """;

    public Flux<String> matchResume(String jobId, String userId) {

        String prompt = String.format(PROMPT, jobId, userId);
        return Flux.create(sink ->
                aiAssist.chat(prompt)
                        .onPartialResponse(s -> streamResumeAnalysis(sink, s))
                        .onCompleteResponse(response -> completedResumeAnalysis(sink, jobId, userId, response))
                        .onError(sink::error)
                        .start()
        );
    }

    private void streamResumeAnalysis(FluxSink<String> sink, String chunk) {
        log.debug("Chunk: {}", chunk);
        sink.next(chunk);
    }

    private void completedResumeAnalysis(FluxSink<String> sink, String jobId, String userId, ChatResponse response) {
        log.info("Completed analysis jobId: {}, userId {}, report: {}", jobId, userId, response.aiMessage().text());
        sink.complete();
        kafkaProducer.sendAnalysis(JobResumeAnalysisMessage.builder()
                .jobId(jobId)
                .userId(userId)
                .analysis(JobResumeAnalysisDto.builder().report(response.aiMessage().text()).build())
                .build());
    }
}
