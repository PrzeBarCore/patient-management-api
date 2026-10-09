package com.przebarcore.laboratoryapi.dto.response;


import com.przebarcore.laboratoryapi.dto.validation.FieldValidationError;
import com.przebarcore.laboratoryapi.exception.ErrorCode;
import java.time.LocalDateTime;
import java.util.List;

public record ErrorResponse(LocalDateTime timestamp, int status, String errorCode, String errorMessage, String path, List<FieldValidationError> fieldErrors){
    public ErrorResponse(int status, ErrorCode errorCode, String errorMessage, String path, List<FieldValidationError> fieldErrors){
        this(LocalDateTime.now(), status, errorCode.name(), errorMessage, path, fieldErrors);
    }

    public ErrorResponse(int status, ErrorCode errorCode, String errorMessage, String path){
        this(LocalDateTime.now(), status, errorCode.name(), errorMessage, path, List.of());
    }
}


