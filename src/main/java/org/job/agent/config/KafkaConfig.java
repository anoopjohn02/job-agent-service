package org.job.agent.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Value("${kafka.topic.ai.resume-analysis}")
    private String resumeAnalysisTopic;

    @Value("${kafka.topic.ai.qa}")
    private String qaTopic;

    @Bean
    public NewTopic resumeAnalysisTopic() {
        return TopicBuilder.name(resumeAnalysisTopic)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic qaTopic() {
        return TopicBuilder.name(qaTopic)
                .partitions(1)
                .replicas(1)
                .build();
    }
}
