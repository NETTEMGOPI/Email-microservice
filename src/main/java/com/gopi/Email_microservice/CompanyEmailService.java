package com.gopi.Email_microservice;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;

@Service
public class CompanyEmailService {

    @Value("${aws.region:us-east-1}")
    private String awsRegion;

    @Value("${aws.ses.from-email:gopinettiem01@gmail.com}")
    private String fromEmail;

    @Value("${aws.ses.admin-email:gopinettiem01@gmail.com}")
    private String adminEmail;

    private SesClient sesClient;

    public void processCompanySubmission(CompanyEvent companyEvent) {
        try {
            // Send customer acknowledgment email
            sendCustomerAcknowledgmentEmail(companyEvent);

            // Send admin notification email
            sendAdminNotificationEmail(companyEvent);

        } catch (Exception e) {
            System.err.println("Error in company email processing: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void sendCustomerAcknowledgmentEmail(CompanyEvent companyEvent) {
        try {
            String subject = "Company Information Received - " + companyEvent.getCompanyName();
            String body = buildCustomerEmailBody(companyEvent);

            System.out.println("=".repeat(60));
            System.out.println("SENDING CUSTOMER ACKNOWLEDGMENT:");
            System.out.println("To: " + companyEvent.getContactEmail());
            System.out.println("From: " + fromEmail);
            System.out.println("Subject: " + subject);
            System.out.println("Body:");
            System.out.println(body);
            System.out.println("=".repeat(60));

            // Send via AWS SES
            sendViaAwsSes(companyEvent.getContactEmail(), subject, body, "CUSTOMER");

        } catch (Exception e) {
            System.err.println("Error sending customer acknowledgment: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void sendAdminNotificationEmail(CompanyEvent companyEvent) {
        try {
            String subject = "New Company Submission - " + companyEvent.getCompanyName();
            String body = buildAdminEmailBody(companyEvent);

            System.out.println("=".repeat(60));
            System.out.println("SENDING ADMIN NOTIFICATION:");
            System.out.println("To: " + adminEmail);
            System.out.println("From: " + fromEmail);
            System.out.println("Subject: " + subject);
            System.out.println("Body:");
            System.out.println(body);
            System.out.println("=".repeat(60));

            // Send via AWS SES
            sendViaAwsSes(adminEmail, subject, body, "ADMIN");

        } catch (Exception e) {
            System.err.println("Error sending admin notification: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String buildCustomerEmailBody(CompanyEvent companyEvent) {
        return String.format(
                "Dear %s,\n\n" +
                        "Thank you for submitting your company information to our system.\n\n" +
                        "SUBMISSION CONFIRMATION\n" +
                        "=======================\n" +
                        "Company Name: %s\n" +
                        "Contact Person: %s (%s)\n" +
                        "Submission ID: %s\n" +
                        "Submission Time: %s\n\n" +
                        "SUBMITTED INFORMATION\n" +
                        "=====================\n" +
                        "Company: %s%s\n" +
                        "Address: %s\n" +
                        "         %s\n" +
                        "         %s, %s %s\n" +
                        "         %s\n" +
                        "Contact: %s\n" +
                        "Email: %s\n" +
                        "Phone: %s\n\n" +
                        "NEXT STEPS\n" +
                        "==========\n" +
                        "Your submission has been received and is being processed by our team.\n" +
                        "We will review your information and contact you within 2-3 business days.\n\n" +
                        "If you have any questions, please contact our support team.\n\n" +
                        "Best regards,\n" +
                        "Customer Success Team",
                companyEvent.getContactName(),
                companyEvent.getCompanyName(),
                companyEvent.getContactName(),
                companyEvent.getContactTitle(),
                companyEvent.getCompanyId(),
                companyEvent.getSubmissionTime(),
                companyEvent.getCompanyName(),
                (companyEvent.getDba() != null && !companyEvent.getDba().isEmpty()) ? " (DBA: " + companyEvent.getDba() + ")" : "",
                companyEvent.getStreet1(),
                (companyEvent.getStreet2() != null && !companyEvent.getStreet2().isEmpty()) ? companyEvent.getStreet2() : "",
                companyEvent.getCity(),
                companyEvent.getState(),
                companyEvent.getZipCode(),
                companyEvent.getCountry(),
                companyEvent.getContactName() + " - " + companyEvent.getContactTitle(),
                companyEvent.getContactEmail(),
                companyEvent.getContactPhone()
        );
    }

    private String buildAdminEmailBody(CompanyEvent companyEvent) {
        return String.format(
                "NEW COMPANY SUBMISSION ALERT\n" +
                        "============================\n\n" +
                        "A new company has submitted their information through the system.\n\n" +
                        "SUBMISSION DETAILS\n" +
                        "==================\n" +
                        "Submission ID: %s\n" +
                        "Submission Time: %s\n" +
                        "Event Type: %s\n\n" +
                        "COMPANY INFORMATION\n" +
                        "===================\n" +
                        "Company Name: %s\n" +
                        "DBA: %s\n" +
                        "Website: %s\n\n" +
                        "ADDRESS INFORMATION\n" +
                        "===================\n" +
                        "Street 1: %s\n" +
                        "Street 2: %s\n" +
                        "City: %s\n" +
                        "State: %s\n" +
                        "ZIP Code: %s\n" +
                        "Country: %s\n\n" +
                        "CONTACT INFORMATION\n" +
                        "===================\n" +
                        "Name: %s\n" +
                        "Title: %s\n" +
                        "Email: %s\n" +
                        "Phone: %s\n\n" +
                        "ACTION REQUIRED\n" +
                        "===============\n" +
                        "Please review this submission and follow up as necessary.\n" +
                        "The customer expects contact within 2-3 business days.\n\n" +
                        "System Administrator\n" +
                        "Automated Notification System",
                companyEvent.getCompanyId(),
                companyEvent.getSubmissionTime(),
                companyEvent.getEventType(),
                companyEvent.getCompanyName(),
                companyEvent.getDba() != null ? companyEvent.getDba() : "N/A",
                companyEvent.getCompanyUrl() != null ? companyEvent.getCompanyUrl() : "N/A",
                companyEvent.getStreet1(),
                companyEvent.getStreet2() != null ? companyEvent.getStreet2() : "N/A",
                companyEvent.getCity(),
                companyEvent.getState(),
                companyEvent.getZipCode(),
                companyEvent.getCountry(),
                companyEvent.getContactName(),
                companyEvent.getContactTitle(),
                companyEvent.getContactEmail(),
                companyEvent.getContactPhone()
        );
    }

    private void sendHtmlEmailViaAwsSes(String toEmail, String subject, String htmlBody, String textBody, String emailType) {
        try {
            // Initialize SES client if not already done
            if (sesClient == null) {
                sesClient = SesClient.builder()
                        .region(Region.of(awsRegion))
                        .credentialsProvider(DefaultCredentialsProvider.create())
                        .build();
            }

            // Create the email request with both HTML and text versions
            SendEmailRequest emailRequest = SendEmailRequest.builder()
                    .source(fromEmail)
                    .destination(Destination.builder()
                            .toAddresses(toEmail)
                            .build())
                    .message(Message.builder()
                            .subject(Content.builder().data(subject).build())
                            .body(Body.builder()
                                    .html(Content.builder().data(htmlBody).build())  // HTML version
                                    .text(Content.builder().data(textBody).build())  // Text fallback
                                    .build())
                            .build())
                    .build();

            // Send the email
            SendEmailResponse response = sesClient.sendEmail(emailRequest);

            System.out.println("✅ Enhanced " + emailType + " email sent via AWS SES!");
            System.out.println("📧 Message ID: " + response.messageId());
            System.out.println("🎨 Format: HTML with CSS animations + Text fallback");

        } catch (Exception e) {
            System.err.println("⚠️ AWS SES error occurred for " + emailType + " email:");
            System.err.println("   Error: " + e.getMessage());
            System.err.println("   This could be due to:");
            System.err.println("   - Recipient email not verified in SES Sandbox mode");
            System.err.println("   - Region mismatch");
            System.err.println("   - Permission issues");
            System.err.println("   Email content displayed above shows what would be sent");
        }
    }

    // Add this method to your CompanyEmailService class:
    private void sendViaAwsSes(String toEmail, String subject, String body, String emailType) {
        try {
            // Initialize SES client if not already done
            if (sesClient == null) {
                sesClient = SesClient.builder()
                        .region(Region.of(awsRegion))
                        .credentialsProvider(DefaultCredentialsProvider.create())
                        .build();
            }

            // Create simple text email request
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

            System.out.println("✅ " + emailType + " email sent via AWS SES!");
            System.out.println("📧 Message ID: " + response.messageId());
            System.out.println("📝 Format: Plain text");

        } catch (Exception e) {
            System.err.println("⚠️ AWS SES error occurred for " + emailType + " email:");
            System.err.println("   Error: " + e.getMessage());
            System.err.println("   This could be due to:");
            System.err.println("   - Recipient email not verified in SES Sandbox mode");
            System.err.println("   - Region mismatch");
            System.err.println("   - Permission issues");
        }
    }
}