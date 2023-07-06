package com.forum.application.validator;

public interface Validator {
    boolean validate();
    
    static boolean isValidBody(String body) {
        return body == null || body.isEmpty() || body.isBlank();
    }
}
