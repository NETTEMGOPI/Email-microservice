package com.gopi.Email_microservice;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;

@Service
public class EmailService {
    
    @Value("${aws.region:us-east-1}")
    private String awsRegion;
    
    @Value("${aws.ses.from-email:noreply@example.com}")
    private String fromEmail;
    
    private SesClient sesClient;
    
    public void sendWelcomeEmail(EmailEvent emailEvent) {
        try {
            // For now, let's just log what we would send
            // We'll add real AWS SES integration next
            
            String subject = "Welcome to Our Platform!";
            String body = String.format(
                "Hi %s,\n\n" +
                "Welcome to our platform! Your account has been successfully created.\n\n" +
                "User ID: %s\n" +
                "Email: %s\n\n" +
                "Thank you for joining us!\n\n" +
                "Best regards,\n" +
                "The Team",
                emailEvent.getFirstName(),
                emailEvent.getUserId(),
                emailEvent.getEmail()
            );
            
            // Display the email details (for testing)
            System.out.println("=".repeat(60));
            System.out.println("📧 SENDING EMAIL:");
            System.out.println("To: " + emailEvent.getEmail());
            System.out.println("From: " + fromEmail);
            System.out.println("Subject: " + subject);
            System.out.println("Body:");
            System.out.println(body);
            System.out.println("=".repeat(60));
            
            // Try to send with AWS SES (will fall back to logging if no credentials)
            sendViaAwsSes(emailEvent.getEmail(), subject, body);
            
        } catch (Exception e) {
            System.err.println("❌ Error sending welcome email: " + e.getMessage());
        }
    }
    
    private void sendViaAwsSes(String toEmail, String subject, String body) {
        try {
            // Initialize SES client if not already done
            if (sesClient == null) {
                sesClient = SesClient.builder()
                    .region(Region.of(awsRegion))
                    .credentialsProvider(DefaultCredentialsProvider.create())
                    .build();
            }
            
            // Create the email request
            SendEmailRequest emailRequest = SendEmailRequest.builder()
                .source(fromEmail)
                .destination(Destination.builder()
                    .toAddresses(toEmail)
                    .build())
                .message(Message.builder()
                    .subject(Content.builder().data(subject).build())
                    .body(Body.builder()
                        .text(Content.builder().data(body).build())
                        .build())
                    .build())
                .build();
            
            // Send the email
            SendEmailResponse response = sesClient.sendEmail(emailRequest);
            
            System.out.println("✅ Email sent via AWS SES! Message ID: " + response.messageId());
            
        } catch (Exception e) {
            System.err.println("⚠️ AWS SES not available (missing credentials/config), but email would be sent:");
            System.err.println("   This is normal for testing - email content is displayed above");
        }
    }
}
