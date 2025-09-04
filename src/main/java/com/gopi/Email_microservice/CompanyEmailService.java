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
            String subject = "🎉 Welcome! Your submission has been received";
            String htmlBody = buildEnhancedCustomerEmailBody(companyEvent);
            String textBody = buildSimpleCustomerEmailBody(companyEvent);

            System.out.println("=".repeat(60));
            System.out.println("📧 SENDING ENHANCED CUSTOMER ACKNOWLEDGMENT:");
            System.out.println("To: " + companyEvent.getContactEmail());
            System.out.println("From: " + fromEmail);
            System.out.println("Subject: " + subject);
            System.out.println("Format: HTML with animations + Text fallback");
            System.out.println("=".repeat(60));

            // Send HTML email via AWS SES (NOT sendViaAwsSes!)
            sendHtmlEmailViaAwsSes(companyEvent.getContactEmail(), subject, htmlBody, textBody, "CUSTOMER");

        } catch (Exception e) {
            System.err.println("❌ Error sending customer acknowledgment: " + e.getMessage());
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
        return String.format("""
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>New Business Inquiry - Mimosa Networks</title>
            <style>
                body { margin: 0; padding: 0; background-color: #f5f5f5; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; }
                .email-container { max-width: 700px; margin: 20px auto; background: #ffffff; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
                .header { background: linear-gradient(135deg, #dc2626 0%%, #ef4444 100%%); padding: 25px; text-align: center; border-radius: 8px 8px 0 0; }
                .alert-badge { background: #ffffff; color: #dc2626; padding: 8px 16px; border-radius: 20px; font-size: 12px; font-weight: bold; display: inline-block; margin-bottom: 15px; }
                .header-title { color: #ffffff; font-size: 22px; font-weight: 600; margin: 0 0 5px 0; }
                .header-subtitle { color: rgba(255,255,255,0.9); font-size: 14px; margin: 0; }
                .content { padding: 30px; }
                .priority-section { background: #fef2f2; border-left: 4px solid #dc2626; padding: 20px; margin-bottom: 25px; border-radius: 0 6px 6px 0; }
                .priority-section h3 { margin: 0 0 10px 0; color: #dc2626; font-size: 16px; }
                .submission-meta { background: #f8fafc; padding: 15px; border-radius: 6px; margin-bottom: 25px; font-size: 13px; color: #6b7280; }
                .data-table { width: 100%%; border-collapse: collapse; margin: 20px 0; }
                .data-table th { background: #1e3a8a; color: white; padding: 12px; text-align: left; font-weight: 600; font-size: 14px; }
                .data-table td { padding: 10px 12px; border-bottom: 1px solid #e5e7eb; font-size: 14px; }
                .data-table tr:nth-child(even) { background: #f9fafb; }
                .category-header { background: #3b82f6 !important; color: white !important; font-weight: bold; text-align: center; }
                .field-name { font-weight: 600; color: #374151; width: 180px; }
                .field-value { color: #1f2937; }
                .empty-value { color: #9ca3af; font-style: italic; }
                .action-section { background: #f0f9ff; border: 1px solid #bae6fd; padding: 20px; border-radius: 6px; margin: 25px 0; }
                .action-section h3 { color: #0369a1; margin: 0 0 15px 0; }
                .action-list { margin: 10px 0; padding-left: 0; }
                .action-list li { list-style: none; margin-bottom: 8px; padding-left: 20px; position: relative; color: #374151; }
                .action-list li:before { content: "▶"; color: #3b82f6; position: absolute; left: 0; }
                .footer { background: #f9fafb; padding: 20px 30px; border-radius: 0 0 8px 8px; border-top: 1px solid #e5e7eb; }
                .system-info { color: #6b7280; font-size: 12px; }
            </style>
        </head>
        <body>
            <div class="email-container">
                <div class="header">
                    <div class="alert-badge">NEW SUBMISSION</div>
                    <h1 class="header-title">Business Inquiry Notification</h1>
                    <p class="header-subtitle">Immediate Review Required</p>
                </div>
                
                <div class="content">
                    <div class="priority-section">
                        <h3>Action Required</h3>
                        <p>A new business inquiry has been submitted through the company website. Customer expects contact within 2-3 business days.</p>
                    </div>
                    
                    <div class="submission-meta">
                        <strong>Submission ID:</strong> %s | 
                        <strong>Received:</strong> %s | 
                        <strong>Source:</strong> Website Contact Form
                    </div>
                    
                    <table class="data-table">
                        <tr class="category-header">
                            <td colspan="2">COMPANY INFORMATION</td>
                        </tr>
                        <tr>
                            <td class="field-name">Company Name</td>
                            <td class="field-value">%s</td>
                        </tr>
                        <tr>
                            <td class="field-name">DBA (Doing Business As)</td>
                            <td class="field-value">%s</td>
                        </tr>
                        <tr>
                            <td class="field-name">Website</td>
                            <td class="field-value">%s</td>
                        </tr>
                        
                        <tr class="category-header">
                            <td colspan="2">BUSINESS ADDRESS</td>
                        </tr>
                        <tr>
                            <td class="field-name">Street Address 1</td>
                            <td class="field-value">%s</td>
                        </tr>
                        <tr>
                            <td class="field-name">Street Address 2</td>
                            <td class="field-value">%s</td>
                        </tr>
                        <tr>
                            <td class="field-name">City</td>
                            <td class="field-value">%s</td>
                        </tr>
                        <tr>
                            <td class="field-name">State/Province</td>
                            <td class="field-value">%s</td>
                        </tr>
                        <tr>
                            <td class="field-name">ZIP/Postal Code</td>
                            <td class="field-value">%s</td>
                        </tr>
                        <tr>
                            <td class="field-name">Country</td>
                            <td class="field-value">%s</td>
                        </tr>
                        
                        <tr class="category-header">
                            <td colspan="2">PRIMARY CONTACT</td>
                        </tr>
                        <tr>
                            <td class="field-name">Contact Name</td>
                            <td class="field-value">%s</td>
                        </tr>
                        <tr>
                            <td class="field-name">Job Title</td>
                            <td class="field-value">%s</td>
                        </tr>
                        <tr>
                            <td class="field-name">Email Address</td>
                            <td class="field-value"><strong>%s</strong></td>
                        </tr>
                        <tr>
                            <td class="field-name">Phone Number</td>
                            <td class="field-value">%s</td>
                        </tr>
                    </table>
                    
                    <div class="action-section">
                        <h3>Recommended Next Steps</h3>
                        <ul class="action-list">
                            <li>Assign to appropriate business development representative</li>
                            <li>Research company background and potential requirements</li>
                            <li>Schedule initial discovery call within 48 hours</li>
                            <li>Add contact information to CRM system</li>
                            <li>Prepare relevant product materials and case studies</li>
                        </ul>
                    </div>
                </div>
                
                <div class="footer">
                    <div class="system-info">
                        <strong>Mimosa Networks - Business Development System</strong><br>
                        Automated notification | Do not reply to this email<br>
                        For system issues contact: it-support@mimosanetworks.com
                    </div>
                </div>
            </div>
        </body>
        </html>
        """,
                companyEvent.getCompanyId(),
                companyEvent.getSubmissionTime(),
                companyEvent.getCompanyName(),
                companyEvent.getDba() != null && !companyEvent.getDba().isEmpty() ? companyEvent.getDba() : "<span class='empty-value'>Not provided</span>",
                companyEvent.getCompanyUrl() != null && !companyEvent.getCompanyUrl().isEmpty() ? companyEvent.getCompanyUrl() : "<span class='empty-value'>Not provided</span>",
                companyEvent.getStreet1(),
                companyEvent.getStreet2() != null && !companyEvent.getStreet2().isEmpty() ? companyEvent.getStreet2() : "<span class='empty-value'>Not provided</span>",
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

    private String buildEnhancedCustomerEmailBody(CompanyEvent companyEvent) {
        return String.format("""
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Submission Confirmation - Mimosa Networks</title>
            <style>
                body { margin: 0; padding: 0; background-color: #f5f5f5; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; }
                .email-container { max-width: 600px; margin: 20px auto; background: #ffffff; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
                .header { background: linear-gradient(135deg, #1e3a8a 0%%, #3b82f6 100%%); padding: 30px; text-align: center; border-radius: 8px 8px 0 0; }
                .logo-placeholder { background: #ffffff; padding: 15px; border-radius: 6px; margin-bottom: 20px; display: inline-block; }
                .logo-text { color: #1e3a8a; font-size: 24px; font-weight: bold; margin: 0; }
                .header-title { color: #ffffff; font-size: 24px; font-weight: 600; margin: 0 0 8px 0; }
                .header-subtitle { color: rgba(255,255,255,0.9); font-size: 16px; margin: 0; }
                .content { padding: 40px 30px; }
                .greeting { font-size: 18px; color: #1f2937; margin-bottom: 20px; }
                .message { font-size: 16px; line-height: 1.6; color: #4b5563; margin-bottom: 30px; }
                .info-box { background: #f8fafc; border-left: 4px solid #3b82f6; padding: 20px; margin: 25px 0; border-radius: 0 6px 6px 0; }
                .info-box h3 { margin: 0 0 10px 0; color: #1e3a8a; font-size: 18px; }
                .info-box p { margin: 0; color: #4b5563; line-height: 1.5; }
                .next-steps { background: #fefefe; border: 1px solid #e5e7eb; padding: 25px; border-radius: 6px; margin: 30px 0; }
                .next-steps h3 { color: #1e3a8a; margin: 0 0 15px 0; font-size: 18px; }
                .next-steps ul { margin: 10px 0; padding-left: 20px; color: #4b5563; }
                .next-steps li { margin-bottom: 8px; }
                .contact-section { margin: 30px 0; padding: 20px; background: #f9fafb; border-radius: 6px; }
                .contact-section h4 { color: #1e3a8a; margin: 0 0 10px 0; }
                .contact-info { color: #6b7280; font-size: 14px; line-height: 1.5; }
                .footer { background: #f9fafb; padding: 25px 30px; border-radius: 0 0 8px 8px; border-top: 1px solid #e5e7eb; }
                .company-info { color: #6b7280; font-size: 13px; line-height: 1.4; margin-bottom: 15px; }
                .legal-links { text-align: center; margin-top: 20px; }
                .legal-links a { color: #6b7280; text-decoration: none; font-size: 12px; margin: 0 10px; }
                .legal-links a:hover { color: #3b82f6; text-decoration: underline; }
                .unsubscribe { text-align: center; margin-top: 15px; font-size: 11px; color: #9ca3af; }
            </style>
        </head>
        <body>
            <div class="email-container">
                <div class="header">
                    <div class="logo-placeholder">
                        <div class="logo-text">MIMOSA NETWORKS</div>
                    </div>
                    <h1 class="header-title">Submission Confirmation</h1>
                    <p class="header-subtitle">Your business information has been received</p>
                </div>
                
                <div class="content">
                    <div class="greeting">Dear %s,</div>
                    
                    <div class="message">
                        Thank you for submitting your business information to Mimosa Networks. We have successfully received your submission and our team will review the details provided.
                    </div>
                    
                    <div class="info-box">
                        <h3>What happens next?</h3>
                        <p>Our business development team will review your submission and contact you within <strong>2-3 business days</strong> to discuss your requirements and potential partnership opportunities.</p>
                    </div>
                    
                    <div class="next-steps">
                        <h3>In the meantime:</h3>
                        <ul>
                            <li>Review our product documentation and case studies on our website</li>
                            <li>Prepare any specific technical requirements or questions</li>
                            <li>Consider your implementation timeline and budget parameters</li>
                        </ul>
                    </div>
                    
                    <div class="contact-section">
                        <h4>Questions or Urgent Matters?</h4>
                        <div class="contact-info">
                            Business Development: <strong>bd@mimosanetworks.com</strong><br>
                            Technical Support: <strong>support@mimosanetworks.com</strong><br>
                            Phone: <strong>+1 (555) 123-4567</strong><br>
                            Business Hours: Monday - Friday, 8:00 AM - 6:00 PM PST
                        </div>
                    </div>
                    
                    <div class="message">
                        We appreciate your interest in Mimosa Networks and look forward to exploring how our solutions can support your business objectives.
                    </div>
                </div>
                
                <div class="footer">
                    <div class="company-info">
                        <strong>Mimosa Networks</strong><br>
                        1234 Technology Drive, Suite 100<br>
                        San Jose, CA 95110, United States<br>
                        Phone: +1 (555) 123-4567 | Web: www.mimosanetworks.com
                    </div>
                    
                    <div class="legal-links">
                        <a href="#privacy">Privacy Policy</a>
                        <a href="#terms">Terms of Service</a>
                        <a href="#security">Security</a>
                        <a href="#contact">Contact Us</a>
                    </div>
                    
                    <div class="unsubscribe">
                        You received this email because you submitted a business inquiry through our website.<br>
                        To unsubscribe from future communications, <a href="#unsubscribe" style="color: #6b7280;">click here</a>.
                    </div>
                </div>
            </div>
        </body>
        </html>
        """, companyEvent.getContactName());
    }

    private String buildSimpleCustomerEmailBody(CompanyEvent companyEvent) {
        return String.format(
                "🎉 CONGRATULATIONS!\n\n" +
                        "Dear %s,\n\n" +
                        "Thank you for choosing our platform! We're excited to welcome you to our community.\n\n" +
                        "WHAT HAPPENS NEXT?\n" +
                        "==================\n" +
                        "Our dedicated team will review your information and reach out to you within 2-3 business days with personalized recommendations and next steps.\n\n" +
                        "Thank you for trusting us with your business!\n\n" +
                        "Best regards,\n" +
                        "The Team",
                companyEvent.getContactName()
        );
    }
}