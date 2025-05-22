package org.ouanu.manager.exception;

import org.springframework.http.HttpStatus;

public class ConflictException extends BusinessException {
    public ConflictException(String message) {
        super(HttpStatus.CONFLICT, "RESOURCE_CONFLICT", message);
    }
}