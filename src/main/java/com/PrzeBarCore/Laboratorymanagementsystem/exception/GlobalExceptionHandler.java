package com.PrzeBarCore.Laboratorymanagementsystem.exception;

import com.PrzeBarCore.Laboratorymanagementsystem.dto.response.ErrorResponse;
import com.PrzeBarCore.Laboratorymanagementsystem.dto.validation.FieldValidationError;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(PatientAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handlePatientExists(PatientAlreadyExistsException exception, HttpServletRequest request) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(HttpStatus.CONFLICT.value(), ErrorCode.PATIENT_ALREADY_EXISTS, exception.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(PatientNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePatientNotFound(PatientNotFoundException exception, HttpServletRequest request) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(HttpStatus.NOT_FOUND.value(), ErrorCode.PATIENT_NOT_FOUND, exception.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException exception, HttpServletRequest request) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), ErrorCode.VALIDATION_ERROR, "Request validation failed", request.getRequestURI(),
                        exception.getFieldErrors().stream().map(fieldError -> new FieldValidationError(fieldError.getField(), fieldError.getDefaultMessage())).toList()));
    }

    @ExceptionHandler(MedicalOrderNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleOrderNotFound(MedicalOrderNotFoundException exception, HttpServletRequest request) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(HttpStatus.NOT_FOUND.value(), ErrorCode.ORDER_NOT_FOUND, exception.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(InvalidOrderStatusTransitionException.class)
    public ResponseEntity<ErrorResponse> handleNotAllowedTransition(InvalidOrderStatusTransitionException exception, HttpServletRequest request) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(HttpStatus.CONFLICT.value(), ErrorCode.INVALID_ORDER_STATUS_TRANSITION, exception.getMessage(), request.getRequestURI()));
    }
}
