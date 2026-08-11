package com.example.demo.exception;

// запрошенный ресурс отсутствует
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}