package org.job.agent.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class JobResumeAnalysisDto {

    private String id;
    private String resume;
    private String jobDescription;
    private String report;
    private LocalDateTime createdAt;
}
