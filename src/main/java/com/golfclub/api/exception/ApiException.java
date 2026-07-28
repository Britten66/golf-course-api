package com.golfclub.api.exception;

import org.springframework.http.HttpStatus;

//Our own exception.
//It carries the status code we want to
//send back, so the handler just reads it.
public class ApiException extends RuntimeException {

    private final HttpStatus status;

    public ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
