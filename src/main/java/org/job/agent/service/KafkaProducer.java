package org.job.agent.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.job.agent.model.JobQAMessage;
import org.job.agent.model.JobResumeAnalysisDto;
import org.job.agent.model.JobResumeAnalysisMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducer {

    @Value("${kafka.topic.ai.resume-analysis}")
    private String resumeAnalysisTopic;

    @Value("${kafka.topic.ai.qa}")
    private String qaTopic;

    private final KafkaTemplate<String, JobQAMessage> qaKafkaTemplate;
    private final KafkaTemplate<String, JobResumeAnalysisMessage> analysisKafkaTemplate;

    public void sendAnalysis(JobResumeAnalysisMessage message) {
        analysisKafkaTemplate.send(resumeAnalysisTopic, message);
    }

    public void sendQA(JobQAMessage message) {
        qaKafkaTemplate.send(qaTopic, message);
    }

    public void sendTestAnalysis() {
        analysisKafkaTemplate.send("ai-resume-analysis-test", JobResumeAnalysisMessage.builder()
                        .jobId("job Id")
                        .userId("user Id")
                        .analysis(JobResumeAnalysisDto.builder()
                                .report("Test report by Anoop")
                                .build())
                .build());
    }
}
