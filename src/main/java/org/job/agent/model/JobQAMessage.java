package org.job.agent.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class JobQAMessage {

    private String jobId;
    private String userId;
    private List<JobQADto> qa;
    private JobQASummaryDto summary;
}
