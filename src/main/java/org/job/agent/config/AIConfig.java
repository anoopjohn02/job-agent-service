package org.job.agent.config;

import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.transport.McpTransport;
import dev.langchain4j.mcp.client.transport.http.StreamableHttpMcpTransport;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.tool.ToolProvider;
import lombok.RequiredArgsConstructor;
import org.job.agent.service.AiAssist;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class AIConfig {

    @Value("${service.mcp.server-url}")
    private String mcpServer;

    private final StreamingChatModel model;

    @Bean
    public AiAssist aiAssist() {
        return AiServices.builder(AiAssist.class)
                .streamingChatModel(model)
                .toolProvider(toolProvider())
                .build();
    }

    @Bean
    public ToolProvider toolProvider() {
        return McpToolProvider.builder()
                .mcpClients(List.of(mcpClient()))
                .build();
    }

    @Bean
    public McpClient mcpClient() {
        return DefaultMcpClient.builder()
                .transport(transport())
                .build();
    }

    @Bean
    public McpTransport transport() {
        return StreamableHttpMcpTransport.builder()
                .url(mcpServer + "/mcp")
                .timeout(Duration.ofMinutes(5))
                .logRequests(true)
                .logResponses(true)
                .build();
    }
}
