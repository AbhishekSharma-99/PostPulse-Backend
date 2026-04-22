package com.postpulse.payload;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "LoginDto Model Information")
public class LoginDto {

    @Schema(description = "Username or Email")
    @NotBlank(message = "Username or email is required")
    private String usernameOrEmail;

    @Schema(description = "Password")
    @NotBlank(message = "Password is required")
    private String password;
}