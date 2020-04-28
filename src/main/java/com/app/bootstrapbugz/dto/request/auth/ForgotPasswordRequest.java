package com.app.bootstrapbugz.dto.request.auth;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

@Getter
@Setter
public class ForgotPasswordRequest {
    @NotEmpty(message = "{email.notEmpty}")
    @Email(message = "{email.regex}")
    private String email;
}