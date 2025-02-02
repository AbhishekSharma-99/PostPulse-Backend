package com.springboot.blog.payload;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Date;

@Schema(
        description = "Error Details Model Information"
)
// ErrorDetails class for handling error responses in the API
public class ErrorDetails {
    public ErrorDetails(Date timestamp, String message, String details) {
        this.timestamp = timestamp;
        this.message = message;
        this.details = details;
    }

    @Schema(description = "Error Timestamp")
    private Date timestamp;
    @Schema(description = "Error Message")
    private String message;
    @Schema(description = "Error Details")
    private String details;

    public Date getTimestamp() {
        return timestamp;
    }

    public String getMessage() {
        return message;
    }

    public String getDetails() {
        return details;
    }
}
