package com.springboot.blog.payload;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "JWTAuthResponse Model Information")
public class JWTAuthResponse {
    @Schema(description = "JWT Access Token")
    private String accessToken;
    @Schema(description = "Type of JWT Token", defaultValue = "Bearer")  // default value for Swagger UI documentation. It's optional.'
    private String tokenType = "Bearer";


}
