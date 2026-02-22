package org.job.agent.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChatRequest {

    @NotNull(message = "Question can't be null")
    @Size(min = 1, message = "Question can't be empty")
    private String question;
}
