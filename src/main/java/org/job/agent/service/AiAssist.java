package org.job.agent.service;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;

public interface AiAssist {
    @SystemMessage("""
        You are an assistant that can call external MCP tools
        when needed to answer the user.
        """)
    TokenStream chat(@UserMessage String prompt);
}
