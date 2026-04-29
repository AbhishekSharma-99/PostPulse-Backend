package com.postpulse.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

public class BlogAPIException extends RuntimeException {

    @Getter
    private final HttpStatus httpStatus;

    public BlogAPIException(HttpStatus httpStatus, String message) {
        super(message);
        this.httpStatus = httpStatus;
    }
}