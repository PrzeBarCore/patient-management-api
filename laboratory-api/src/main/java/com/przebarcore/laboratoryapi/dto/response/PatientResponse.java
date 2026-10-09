package com.przebarcore.laboratoryapi.dto.response;

import java.time.LocalDate;

public record PatientResponse(Long id, String firstName, String lastName, LocalDate birthDate){}
