package com.gopi.Email_microservice;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EmailEvent {
    
    @JsonProperty("userId")
    private Long userId;
    
    @JsonProperty("email") 
    private String email;
    
    @JsonProperty("firstName")
    private String firstName;
    
    @JsonProperty("eventType")
    private String eventType;
    
    // Constructors
    public EmailEvent() {}
    
    public EmailEvent(Long userId, String email, String firstName, String eventType) {
        this.userId = userId;
        this.email = email;
        this.firstName = firstName;
        this.eventType = eventType;
    }
    
    // Getters and Setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    
    @Override
    public String toString() {
        return "EmailEvent{" +
                "userId=" + userId +
                ", email='" + email + '\'' +
                ", firstName='" + firstName + '\'' +
                ", eventType='" + eventType + '\'' +
                '}';
    }
}
