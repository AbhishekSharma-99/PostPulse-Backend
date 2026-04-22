package com.postpulse.payload;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Date;

@Getter
@AllArgsConstructor
@Schema(description = "Error Details Model Information")
public class ErrorDetails {

    @Schema(description = "Error Timestamp")
    private Date timestamp;

    @Schema(description = "Error Message")
    private String message;

    @Schema(description = "Error Details")
    private String details;
}