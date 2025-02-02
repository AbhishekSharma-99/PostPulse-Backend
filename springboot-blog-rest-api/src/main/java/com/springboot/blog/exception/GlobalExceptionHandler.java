package com.springboot.blog.exception;

import com.springboot.blog.payload.ErrorDetails;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

// This annotation makes this class a global exception handler for the application
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    // Handle ResourceNotFoundException specifically
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorDetails> handleResourceNotFoundException(ResourceNotFoundException exception, WebRequest webRequest){
        // Create an ErrorDetails object with the error message and request details
        ErrorDetails errorDetails = new ErrorDetails(new Date(), exception.getMessage(), webRequest.getDescription(false));
        // Return a ResponseEntity with the error details and a NOT_FOUND status
        return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND);
    }

    // Handle BlogAPIExecution exceptions specifically
    @ExceptionHandler(BlogAPIExecution.class)
    public ResponseEntity<ErrorDetails> handleBlogAPIExecution(BlogAPIExecution exception, WebRequest webRequest){
        // Create an ErrorDetails object for the BlogAPIExecution error
        ErrorDetails errorDetails = new ErrorDetails(new Date(), exception.getMessage(), webRequest.getDescription(false));
        // Return a ResponseEntity with the error details and a BAD_REQUEST status
        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }

    // Handle method argument type mismatch exceptions
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorDetails> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException exception, WebRequest webRequest) {
        // Create a custom error message for the type mismatch
        String errorMessage = String.format("Invalid value '%s' for parameter '%s'. Expected type: %s",
                exception.getValue(),
                exception.getName(),
                exception.getRequiredType().getSimpleName());
        // Create an ErrorDetails object with the custom error message
        ErrorDetails errorDetails = new ErrorDetails(new Date(), errorMessage, webRequest.getDescription(false));
        // Return a ResponseEntity with the error details and a BAD_REQUEST status
        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }

    // Override the default behavior for handling validation errors
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {

        // Create a map to hold field-specific validation errors
        Map<String, String> errors = new HashMap<>();
        // Iterate through all validation errors and populate the map
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError)error).getField(); // Get the field name
            String message = error.getDefaultMessage(); // Get the error message
            errors.put(fieldName, message); // Add to the map
        });

        // Return a ResponseEntity with the errors map and a BAD_REQUEST status
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    // Global exception handler for any other exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDetails> handleGlobalException(Exception exception, WebRequest webRequest){
        // Create an ErrorDetails object for the global error
        ErrorDetails errorDetails = new ErrorDetails(new Date(), exception.getMessage(), webRequest.getDescription(false));

        // Print a message to the console for debugging purposes
        System.out.println("Error!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

        // Return a ResponseEntity with the error details and an INTERNAL_SERVER_ERROR status
        return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Handle AuthorizationDeniedException specifically
    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ErrorDetails> handleResourceNotFoundException(AuthorizationDeniedException exception, WebRequest webRequest){
        // Create an ErrorDetails object with the error message and request details
        ErrorDetails errorDetails = new ErrorDetails(new Date(), exception.getMessage(), webRequest.getDescription(false));
        // Return a ResponseEntity with the error details and a NOT_FOUND status
        return new ResponseEntity<>(errorDetails, HttpStatus.UNAUTHORIZED);
    }
}