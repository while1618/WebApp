package com.app.webapp.error.exception;

import com.app.webapp.error.ErrorDomains;
import lombok.Getter;

@Getter
public class LoginException extends RuntimeException {
    private final String domain = ErrorDomains.AUTH;

    public LoginException(String message) {
        super(message);
    }
}