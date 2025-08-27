package com.gopi.Email_microservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class EmailEventConsumer {
    
    @Autowired
    private EmailService emailService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @KafkaListener(topics = "sendEmail", groupId = "email-service-group")
    public void consumeEmailEvent(String message) {
        try {
            System.out.println("📨 Received Email Event: " + message);
            
            // Parse the JSON message
            EmailEvent emailEvent = objectMapper.readValue(message, EmailEvent.class);
            
            System.out.println("📧 Processing Email Event: " + emailEvent);
            
            // Send email using AWS SES
            emailService.sendWelcomeEmail(emailEvent);
            
        } catch (Exception e) {
            System.err.println("❌ Error processing email event: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
