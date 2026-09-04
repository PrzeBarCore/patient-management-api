package com.PrzeBarCore.Laboratorymanagementsystem.dto.request;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

import static com.PrzeBarCore.Laboratorymanagementsystem.dto.validation.ValidationMessages.*;
import static com.PrzeBarCore.Laboratorymanagementsystem.dto.validation.ValidationMessages.BIRTH_DATE_MUST_BE_A_PAST_DATE;

public record UpdatePatientRequest(@NotBlank(message = FIRST_NAME_NOT_BLANK) @Size(max = 50, message = FIRST_NAME_MAX_LENGTH) String firstName,
                                   @NotBlank(message = LAST_NAME_NOT_BLANK) @Size(max = 50, message = LAST_NAME_MAX_LENGTH) String lastName,
                                   @NotNull(message = BIRTH_DATE_MUST_NOT_BE_NULL) @Past(message = BIRTH_DATE_MUST_BE_A_PAST_DATE) LocalDate birthDate) {
}
