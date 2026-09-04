package com.PrzeBarCore.Laboratorymanagementsystem.exception;

public class MedicalOrderNotFoundException extends RuntimeException {
    public MedicalOrderNotFoundException(Long id){
        super("Order with ID " + id + " not found");
    }
}
