package org.ouanu.manager.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BusinessException extends RuntimeException {

    private final HttpStatus httpStatus;
    private final String errorCode;

    public BusinessException(HttpStatus httpStatus, String errorCode, String message) {
        super(message);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
    }

    public static BusinessException of(HttpStatus status, String errorCode, String message) {
        return new BusinessException(status, errorCode, message);
    }

}
