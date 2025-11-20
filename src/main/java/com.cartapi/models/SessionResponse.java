// src/main/java/com/cartapi/models/SessionResponse.java
package com.cartapi.models;

public class SessionResponse {
    private final String sessionId;
    private final String message;

    public SessionResponse(String sessionId, String message) {
        this.sessionId = sessionId;
        this.message = message;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getMessage() {
        return message;
    }
}