package com.przebarcore.laboratoryapi.exception;

public class PatientAlreadyExistsException extends RuntimeException {
    public PatientAlreadyExistsException(String pesel){
        super("Patient with PESEL " + pesel + " already exists");
    }
}
