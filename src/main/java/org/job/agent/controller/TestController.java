package org.job.agent.controller;

import lombok.RequiredArgsConstructor;
import org.job.agent.service.KafkaProducer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/test")
public class TestController {

    private final KafkaProducer producer;

    @GetMapping
    public void getUserProfile() {
        producer.sendTestAnalysis();
    }

}
