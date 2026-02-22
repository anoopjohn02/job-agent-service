package org.job.agent.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class JobQASummaryDto {

    private String id;
    private String summary;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
}
