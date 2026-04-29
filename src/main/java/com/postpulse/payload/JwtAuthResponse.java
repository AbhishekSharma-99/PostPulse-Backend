package com.postpulse.payload;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@Schema(description = "JWTAuthResponse Model Information")
public class JwtAuthResponse {
    @Schema(description = "JWT Access Token")
    private String accessToken;
    @Schema(description = "Type of JWT Token", defaultValue = "Bearer")
    // default value for Swagger UI documentation. It's optional.'
    private String tokenType = "Bearer";

    public JwtAuthResponse(String accessToken) {
        this.accessToken = accessToken;
    }

}
