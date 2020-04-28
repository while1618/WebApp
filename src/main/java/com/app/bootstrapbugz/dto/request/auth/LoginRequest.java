package com.app.bootstrapbugz.dto.request.auth;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;

@Getter
@Setter
public class LoginRequest {
    @NotEmpty(message = "{username.notEmpty}")
    private String username;
    @NotEmpty(message = "{password.notEmpty}")
    private String password;
}