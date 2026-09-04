package com.PrzeBarCore.Laboratorymanagementsystem.dto.validation;

public final class ValidationMessages {
    private ValidationMessages(){};
    public static final String FIRST_NAME_NOT_BLANK = "First name must not be blank";
    public static final String FIRST_NAME_MAX_LENGTH= "First name must be shorter than 50 characters";
    public static final String LAST_NAME_NOT_BLANK = "Last name must not be blank";
    public static final String LAST_NAME_MAX_LENGTH = "Last name must be shorter than 50 characters";
    public static final String PESEL_MUST_NOT_BE_BLANK = "Pesel must not be blank";
    public static final String PESEL_MUST_BE_11_DIGIT_NUMBER = "Pesel must be 11-digit number";
    public static final String BIRTH_DATE_MUST_BE_A_PAST_DATE = "Birth Date must be a past date";
    public static final String BIRTH_DATE_MUST_NOT_BE_NULL = "Birth Date must not be null";
}
