package org.job.agent.model;

import lombok.Data;

import java.util.List;

@Data
public class LoggedInUser {
    private final String id;
    private final String email;
    private final String name;
    private final List<String> roles;
}
