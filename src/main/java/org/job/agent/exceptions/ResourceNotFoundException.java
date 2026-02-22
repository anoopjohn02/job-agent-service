package org.job.agent.exceptions;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resource) {
        super("Not found: " + resource);
    }
}

