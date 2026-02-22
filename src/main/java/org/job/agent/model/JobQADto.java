package org.job.agent.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class JobQADto {

    private String id;
    private String qa;
    private String role;
    private LocalDateTime createdAt;
}
