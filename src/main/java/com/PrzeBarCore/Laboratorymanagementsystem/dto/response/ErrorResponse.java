package com.PrzeBarCore.Laboratorymanagementsystem.dto.response;


import com.PrzeBarCore.Laboratorymanagementsystem.dto.validation.FieldValidationError;
import com.PrzeBarCore.Laboratorymanagementsystem.exception.ErrorCode;
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


