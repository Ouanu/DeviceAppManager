package org.ouanu.manager.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class DeviceNotFoundException extends RuntimeException {

    private final HttpStatus httpStatus;

    public DeviceNotFoundException(HttpStatus httpStatus, String message) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public static DeviceNotFoundException of(HttpStatus status, String message) {
        return new DeviceNotFoundException(status, message);
    }

}
