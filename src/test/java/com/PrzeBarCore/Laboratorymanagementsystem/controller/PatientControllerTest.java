package com.PrzeBarCore.Laboratorymanagementsystem.controller;

import com.PrzeBarCore.Laboratorymanagementsystem.dto.request.CreatePatientRequest;
import com.PrzeBarCore.Laboratorymanagementsystem.dto.request.UpdatePatientRequest;
import com.PrzeBarCore.Laboratorymanagementsystem.dto.response.PatientResponse;
import com.PrzeBarCore.Laboratorymanagementsystem.exception.PatientAlreadyExistsException;
import com.PrzeBarCore.Laboratorymanagementsystem.exception.PatientNotFoundException;
import com.PrzeBarCore.Laboratorymanagementsystem.service.PatientService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.List;

import static com.PrzeBarCore.Laboratorymanagementsystem.dto.validation.ValidationMessages.*;
import static com.PrzeBarCore.Laboratorymanagementsystem.exception.ErrorCode.*;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PatientController.class)
class PatientControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private PatientService service;

    @Test
    void shouldReturnCreatedStatusWithPatientWhenRequestIsValid() throws Exception{
        //Arrange
        Long id = 1L;
        String firstName = "Zbyszko";
        String lastName = "ZBogdańca";
        String pesel = "12345678910";
        LocalDate birthDate = LocalDate.of(1390,1,1);
        CreatePatientRequest request = new CreatePatientRequest(
                firstName,
                lastName,
                pesel,
                birthDate);
        PatientResponse serviceResponse = new PatientResponse(id, firstName, lastName, birthDate);

        when(service.create(request)).thenReturn(serviceResponse);

        //Act + Assert

        mockMvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.firstName").value(firstName))
                .andExpect(jsonPath("$.lastName").value(lastName))
                .andExpect(jsonPath("$.birthDate").value(birthDate.toString()));

        //Assert
        verify(service).create(request);
    }

    @Test
    void shouldReturnBadRequestWhenPatientRequestIsInvalid() throws Exception{
        //Arrange
        String firstName = "";
        String lastName = "";
        String pesel = "123";
        LocalDate birthDate = LocalDate.now().plusYears(1);
        CreatePatientRequest request = new CreatePatientRequest(
                firstName,
                lastName,
                pesel,
                birthDate);

        //Act + Assert

        mockMvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errorCode").value(VALIDATION_ERROR.name()))
                .andExpect(jsonPath("$.errorMessage").value("Request validation failed"))
                .andExpect(jsonPath("$.path").value("/api/patients"))
                .andExpect(jsonPath("$.fieldErrors").isArray())
                .andExpect(jsonPath("$.fieldErrors", hasSize(4)))
                .andExpect(jsonPath("$.fieldErrors[*].fieldName")
                        .value(containsInAnyOrder("pesel", "birthDate", "firstName", "lastName")))
                .andExpect(jsonPath("$.fieldErrors[?(@.fieldName == 'firstName')].errorMessage")
                        .value(hasItem(FIRST_NAME_NOT_BLANK)))
                .andExpect(jsonPath("$.fieldErrors[?(@.fieldName == 'lastName')].errorMessage")
                        .value(hasItem(LAST_NAME_NOT_BLANK)))
                .andExpect(jsonPath("$.fieldErrors[?(@.fieldName == 'pesel')].errorMessage")
                        .value(hasItem(PESEL_MUST_BE_11_DIGIT_NUMBER)))
                .andExpect(jsonPath("$.fieldErrors[?(@.fieldName == 'birthDate')].errorMessage").
                        value(hasItem(BIRTH_DATE_MUST_BE_A_PAST_DATE)));

        //Assert
        verifyNoInteractions(service);
    }

    @Test
    void shouldReturnConflictWhenPatientWithPeselAlreadyExists() throws Exception{
        //Arrange
        String firstName = "asasdda";
        String lastName = "asdasd";
        String pesel = "12345678910";
        LocalDate birthDate = LocalDate.now().minusYears(1);
        CreatePatientRequest request = new CreatePatientRequest(
                firstName,
                lastName,
                pesel,
                birthDate);

        when(service.create(request)).thenThrow(new PatientAlreadyExistsException(pesel));
        //Act + Assert

        mockMvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.errorCode").value(PATIENT_ALREADY_EXISTS.name()))
                .andExpect(jsonPath("$.errorMessage").value("Patient with PESEL " + pesel + " already exists"))
                .andExpect(jsonPath("$.path").value("/api/patients"))
                .andExpect(jsonPath("$.fieldErrors").isEmpty());

        //Assert
        verify(service).create(request);
    }

    @Test
    void shouldReturnOkWhenPatientExists() throws Exception{
        //Arrange
        Long id = 1L;
        String firstName = "asasdda";
        String lastName = "asdasd";
        LocalDate birthDate = LocalDate.now().minusYears(1);
        PatientResponse response = new PatientResponse(id,
                firstName,
                lastName,
                birthDate);

        when(service.findById(id)).thenReturn(response);
        //Act + Assert

        mockMvc.perform(get("/api/patients/{id}", id))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(response.id()))
                .andExpect(jsonPath("$.firstName").value(response.firstName()))
                .andExpect(jsonPath("$.lastName").value(response.lastName()))
                .andExpect(jsonPath("$.birthDate").value(response.birthDate().toString()));

        //Assert
        verify(service).findById(id);
    }

    @Test
    void shouldReturnNotFoundWhenPatientDoesNotExist() throws Exception{
        //Arrange
        Long id = 1L;

        when(service.findById(id)).thenThrow(new PatientNotFoundException(id));
        //Act + Assert

        mockMvc.perform(get("/api/patients/{id}", id))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
                .andExpect(jsonPath("$.errorCode").value(PATIENT_NOT_FOUND.name()))
                .andExpect(jsonPath("$.errorMessage").value("Patient with ID " + id + " not found"))
                .andExpect(jsonPath("$.path").value(String.format("/api/patients/%d", id)))
                .andExpect(jsonPath("$.fieldErrors").isEmpty());

        //Assert
        verify(service).findById(id);
    }

    @Test
    void shouldReturnOkWhenPatientsExist() throws Exception{
        //Arrange
        Long id1 = 1L;
        String firstName1 = "Baobab1";
        String lastName1 = "Drzewo1";
        LocalDate birthDate1 = LocalDate.now().minusYears(1);
        PatientResponse response1 = new PatientResponse(id1,
                firstName1,
                lastName1,
                birthDate1);

        Long id2 = 2L;
        String firstName2 = "Baobab2";
        String lastName2 = "Drzewo2";
        LocalDate birthDate2 = LocalDate.now().minusYears(2);
        PatientResponse response2 = new PatientResponse(id2,
                firstName2,
                lastName2,
                birthDate2);

        when(service.findAll()).thenReturn(List.of(response1, response2));
        //Act + Assert

        mockMvc.perform(get("/api/patients/"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(String.format("$.[?(@.id == %d && @.firstName == \"%s\" && @.lastName == \"%s\" && @.birthDate == \"%s\")]",
                id1, firstName1, lastName1, birthDate1.toString())).exists())
                .andExpect(jsonPath(String.format("$.[?(@.id == %d && @.firstName == \"%s\" && @.lastName == \"%s\" && @.birthDate == \"%s\")]",
                id2, firstName2, lastName2, birthDate2.toString())).exists());

        //Assert
        verify(service).findAll();
    }

    @Test
    void shouldReturnOkWhenPatientsDoNotExist() throws Exception{
        //Arrange

        when(service.findAll()).thenReturn(List.of());
        //Act + Assert

        mockMvc.perform(get("/api/patients/"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

        //Assert
        verify(service).findAll();
    }

    @Test
    void shouldReturnOkWhenPatientToUpdateExists() throws Exception{
        //Arrange
        Long id = 1L;
        String firstName = "Zbyszko";
        String lastName = "ZBogdańca";
        LocalDate birthDate = LocalDate.of(1390,1,1);
        UpdatePatientRequest request = new UpdatePatientRequest(
                firstName,
                lastName,
                birthDate);
        PatientResponse response = new PatientResponse(id, firstName, lastName, birthDate);

        when(service.update(id, request)).thenReturn(response);
        //Act + Assert

        mockMvc.perform(put("/api/patients/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(response.id()))
                .andExpect(jsonPath("$.firstName").value(response.firstName()))
                .andExpect(jsonPath("$.lastName").value(response.lastName()))
                .andExpect(jsonPath("$.birthDate").value(response.birthDate().toString()));

        //Assert
        verify(service).update(id, request);
    }

    @Test
    void shouldReturnNotFoundWhenPatientToUpdateDoesNotExist() throws Exception{
        //Arrange
        Long id = 1L;
        String firstName = "Zbyszko";
        String lastName = "ZBogdańca";
        LocalDate birthDate = LocalDate.of(1390,1,1);
        UpdatePatientRequest request = new UpdatePatientRequest(
                firstName,
                lastName,
                birthDate);

        when(service.update(id, request)).thenThrow(new PatientNotFoundException(id));
        //Act + Assert

        mockMvc.perform(put("/api/patients/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
                .andExpect(jsonPath("$.errorCode").value(PATIENT_NOT_FOUND.name()))
                .andExpect(jsonPath("$.errorMessage").value("Patient with ID " + id + " not found"))
                .andExpect(jsonPath("$.path").value(String.format("/api/patients/%d", id)))
                .andExpect(jsonPath("$.fieldErrors").isEmpty());

        //Assert
        verify(service).update(id, request);
    }

    @Test
    void shouldReturnBadRequestWhenUpdateRequestIsInvalid() throws Exception{
        Long id = 1L;
        String firstName = "";
        String lastName = "";
        LocalDate birthDate = LocalDate.now().plusYears(1);
        UpdatePatientRequest request = new UpdatePatientRequest(
                firstName,
                lastName,
                birthDate);

        //Act + Assert

        mockMvc.perform(put("/api/patients/{id}",id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errorCode").value(VALIDATION_ERROR.name()))
                .andExpect(jsonPath("$.errorMessage").value("Request validation failed"))
                .andExpect(jsonPath("$.path").value("/api/patients/"+id))
                .andExpect(jsonPath("$.fieldErrors").isArray())
                .andExpect(jsonPath("$.fieldErrors", hasSize(3)))
                .andExpect(jsonPath("$.fieldErrors[*].fieldName")
                        .value(containsInAnyOrder("birthDate", "firstName", "lastName")))
                .andExpect(jsonPath("$.fieldErrors[?(@.fieldName == 'firstName')].errorMessage")
                        .value(hasItem(FIRST_NAME_NOT_BLANK)))
                .andExpect(jsonPath("$.fieldErrors[?(@.fieldName == 'lastName')].errorMessage")
                        .value(hasItem(LAST_NAME_NOT_BLANK)))
                .andExpect(jsonPath("$.fieldErrors[?(@.fieldName == 'birthDate')].errorMessage").
                        value(hasItem(BIRTH_DATE_MUST_BE_A_PAST_DATE)));
        //Assert
        verifyNoInteractions(service);
    }

    @Test
    void shouldReturnNoContentWhenPatientToDeleteExists() throws Exception{
        //Arrange
        Long id = 1L;

        //Act + Assert

        mockMvc.perform(delete("/api/patients/{id}", id))
                .andExpect(status().isNoContent());

        //Assert
        verify(service).delete(id);
    }

    @Test
    void shouldReturnNotFoundWhenPatientToDeleteDoesNotExist() throws Exception{
        //Arrange
        Long id = 1L;

        doThrow(new PatientNotFoundException(id)).when(service).delete(id);
        //Act + Assert

        mockMvc.perform(delete("/api/patients/{id}", id))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
                .andExpect(jsonPath("$.errorCode").value(PATIENT_NOT_FOUND.name()))
                .andExpect(jsonPath("$.errorMessage").value("Patient with ID " + id + " not found"))
                .andExpect(jsonPath("$.path").value(String.format("/api/patients/%d", id)))
                .andExpect(jsonPath("$.fieldErrors").isEmpty());

        //Assert
        verify(service).delete(id);
    }
}
