package org.smart.apply.ai.model;

public record ErrorResponse(
        int status,
        String message
) {}

