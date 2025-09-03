package com.gopi.Email_microservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class CompanyEventConsumer {

    @Autowired
    private CompanyEmailService companyEmailService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(topics = "companySubmission", groupId = "email-service-group")
    public void consumeCompanyEvent(String message) {
        try {
            System.out.println("Received Company Submission Event: " + message);

            // Parse the JSON message
            CompanyEvent companyEvent = objectMapper.readValue(message, CompanyEvent.class);

            System.out.println("Processing Company Event: " + companyEvent);

            // Send dual emails (customer acknowledgment + admin notification)
            companyEmailService.processCompanySubmission(companyEvent);

        } catch (Exception e) {
            System.err.println("Error processing company event: " + e.getMessage());
            e.printStackTrace();
        }
    }
}