package org.ouanu.manager.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApplicationNotFoundException extends RuntimeException {

    private final HttpStatus httpStatus;

    public ApplicationNotFoundException(HttpStatus httpStatus, String message) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public static ApplicationNotFoundException of(HttpStatus status, String message) {
        return new ApplicationNotFoundException(status, message);
    }

}
