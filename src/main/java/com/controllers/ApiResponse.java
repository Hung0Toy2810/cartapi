package com.controllers;

public class ApiResponse {
    public Object data;
    public String message;

    public ApiResponse(Object data, String message) {
        this.data = data;
        this.message = message;
    }
}