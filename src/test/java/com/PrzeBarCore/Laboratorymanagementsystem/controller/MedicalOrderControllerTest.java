package com.PrzeBarCore.Laboratorymanagementsystem.controller;

import com.PrzeBarCore.Laboratorymanagementsystem.dto.request.CreateMedicalOrderRequest;
import com.PrzeBarCore.Laboratorymanagementsystem.dto.response.MedicalOrderResponse;
import com.PrzeBarCore.Laboratorymanagementsystem.exception.ErrorCode;
import com.PrzeBarCore.Laboratorymanagementsystem.exception.MedicalOrderNotFoundException;
import com.PrzeBarCore.Laboratorymanagementsystem.exception.PatientNotFoundException;
import com.PrzeBarCore.Laboratorymanagementsystem.global.OrderStatus;
import com.PrzeBarCore.Laboratorymanagementsystem.service.MedicalOrderService;
import com.github.dockerjava.zerodep.shaded.org.apache.hc.core5.http.ContentType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MedicalOrderController.class)
class MedicalOrderControllerTest {
    @Autowired
    ObjectMapper mapper;
    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    MedicalOrderService service;

    @Test
    void shouldReturnCreatedStatusWhenOrderRequestIsValid() throws Exception{
        // Arrange
        Long patientId = 1L;
        Long orderId = 10L;
        LocalDateTime registrationDate = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS);
        OrderStatus status = OrderStatus.NEW;

        CreateMedicalOrderRequest request = new CreateMedicalOrderRequest(patientId);
        MedicalOrderResponse expectedResponse = new MedicalOrderResponse(orderId, registrationDate,status,patientId);

        when(service.create(request)).thenReturn(expectedResponse);
        // Act + Assert
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/orders/" + orderId))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(orderId))
                .andExpect(jsonPath("$.status").value(status.name()))
                .andExpect(jsonPath("$.registrationDateTime").value(registrationDate.toString()))
                .andExpect(jsonPath("$.patientId").value(patientId));

        verify(service).create(request);
    }

    @Test
    void shouldReturnNotFoundWhenPatientDoesNotExist() throws Exception{
        // Arrange
        Long patientId = 1L;
        CreateMedicalOrderRequest request = new CreateMedicalOrderRequest(patientId);

        when(service.create(request)).thenThrow(new PatientNotFoundException(patientId));
        // Act + Assert
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
                .andExpect(jsonPath("$.errorCode").value(ErrorCode.PATIENT_NOT_FOUND.name()))
                .andExpect(jsonPath("$.errorMessage").value("Patient with ID " + patientId + " not found"))
                .andExpect(jsonPath("$.path").value("/api/orders"));

        verify(service).create(request);
    }

    @Test
    void shouldReturnOkWhenOrderExists() throws Exception{
        // Arrange
        Long patientId = 1L;
        Long orderId = 10L;
        LocalDateTime registrationDate = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS);
        OrderStatus status = OrderStatus.NEW;

        MedicalOrderResponse expectedResponse = new MedicalOrderResponse(orderId, registrationDate,status,patientId);

        when(service.findById(orderId)).thenReturn(expectedResponse);
        // Act + Assert
        mockMvc.perform(get("/api/orders/{id}",orderId))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(orderId))
                .andExpect(jsonPath("$.registrationDateTime").value(registrationDate.toString()))
                .andExpect(jsonPath("$.status").value(status.name()))
                .andExpect(jsonPath("$.patientId").value(patientId));

        verify(service).findById(orderId);
    }

    @Test
    void shouldReturnNotFoundWhenOrderDoesNotExist() throws Exception{
        // Arrange
        Long orderId = 10L;

        when(service.findById(orderId)).thenThrow(new MedicalOrderNotFoundException(orderId));
        // Act + Assert
        mockMvc.perform(get("/api/orders/{id}",orderId))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
                .andExpect(jsonPath("$.errorCode").value(ErrorCode.ORDER_NOT_FOUND.name()))
                .andExpect(jsonPath("$.errorMessage").value("Order with ID " + orderId + " not found"))
                .andExpect(jsonPath("$.path").value("/api/orders/" + orderId));

        verify(service).findById(orderId);
    }

    @Test
    void shouldReturnOkWhenOrdersExist() throws Exception{
        // Arrange
        Long patientId = 1L;

        Long orderId1 = 10L;
        LocalDateTime registrationDate1 = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS);
        OrderStatus status1 = OrderStatus.NEW;

        Long orderId2 = 11L;
        LocalDateTime registrationDate2 = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS);
        OrderStatus status2 = OrderStatus.IN_PROCESS;

        MedicalOrderResponse expectedResponse1 = new MedicalOrderResponse(orderId1, registrationDate1,status1,patientId);
        MedicalOrderResponse expectedResponse2 = new MedicalOrderResponse(orderId2, registrationDate2,status2,patientId);

        when(service.findAll()).thenReturn(List.of(expectedResponse1, expectedResponse2));
        // Act + Assert
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath(String.format("$.[?(@.id == %d && @.registrationDateTime == \"%s\" && @.status == \"%s\" && @.patientId == %d)]",
                orderId1, registrationDate1, status1.name(), patientId)).exists())
                .andExpect(jsonPath(String.format("$.[?(@.id == %d && @.registrationDateTime == \"%s\" && @.status == \"%s\" && @.patientId == %d)]",
                orderId2, registrationDate2, status2.name(), patientId)).exists());

        verify(service).findAll();
    }

    @Test
    void shouldReturnOkWhenOrdersDoNotExist() throws Exception{
        // Arrange

        when(service.findAll()).thenReturn(List.of());
        // Act + Assert
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(0));

        verify(service).findAll();
    }

    @Test
    void shouldReturnNoContentWhenOrderToDeleteExists() throws Exception{
        // Arrange
        Long orderId = 1L;

        // Act + Assert
        mockMvc.perform(delete("/api/orders/{id}", orderId))
                .andExpect(status().isNoContent());

        verify(service).delete(orderId);
    }

    @Test
    void shouldReturnNotFoundWhenOrderToDeleteDoesNotExist() throws Exception{
        // Arrange
        Long orderId = 1L;

        doThrow(new MedicalOrderNotFoundException(orderId)).when(service).delete(orderId);

        // Act + Assert
        mockMvc.perform(delete("/api/orders/{id}", orderId))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
                .andExpect(jsonPath("$.errorCode").value(ErrorCode.ORDER_NOT_FOUND.name()))
                .andExpect(jsonPath("$.errorMessage").value("Order with ID " + orderId + " not found"))
                .andExpect(jsonPath("$.path").value("/api/orders/" + orderId));

        verify(service).delete(orderId);
    }
}
