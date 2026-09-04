package com.PrzeBarCore.Laboratorymanagementsystem.integration;

import com.PrzeBarCore.Laboratorymanagementsystem.dto.request.CreateMedicalOrderRequest;
import com.PrzeBarCore.Laboratorymanagementsystem.dto.request.CreatePatientRequest;
import com.PrzeBarCore.Laboratorymanagementsystem.exception.ErrorCode;
import com.PrzeBarCore.Laboratorymanagementsystem.global.OrderStatus;
import com.PrzeBarCore.Laboratorymanagementsystem.repository.MedicalOrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import tools.jackson.databind.ObjectMapper;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class MedicalOrderApiIntegrationTest {
    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer(DockerImageName.parse("postgres:16"));

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MedicalOrderRepository orderRepository;

    @Test
    void shouldCreateAndRetrieveMedicalOrderForExistingPatient() throws Exception {
        //Arrange
        String firstName = "Zbyszko";
        String lastName = "ZBogdańca";
        String pesel = "12345678910";
        LocalDate birthDate = LocalDate.of(1390,1,1);
        CreatePatientRequest request = new CreatePatientRequest(
                firstName,
                lastName,
                pesel,
                birthDate);


        MvcResult result = mockMvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.firstName").value(firstName))
                .andExpect(jsonPath("$.lastName").value(lastName))
                .andExpect(jsonPath("$.birthDate").value(birthDate.toString()))
                .andReturn();

        Long patientId = objectMapper
                .readTree(result.getResponse().getContentAsString())
                .get("id")
                .asLong();

        MvcResult orderResult = mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString((new CreateMedicalOrderRequest(patientId)))))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").exists())
                .andReturn();

        Long orderId = objectMapper
                .readTree(orderResult.getResponse().getContentAsString())
                .get("id")
                .asLong();

        mockMvc.perform(get("/api/orders/{id}",orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId))
                .andExpect(jsonPath("$.status").value(OrderStatus.NEW.name()))
                .andExpect(jsonPath("$.registrationDateTime").isNotEmpty())
                .andExpect(jsonPath("$.patientId").value(patientId));
    }

    @Test
    void shouldReturnNotFoundWhenPatientDoesNotExist() throws Exception {
        //Arrange
        Long patientId = 999999L;
        long countBefore = orderRepository.count();

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString((new CreateMedicalOrderRequest(patientId)))))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
                .andExpect(jsonPath("$.errorCode").value(ErrorCode.PATIENT_NOT_FOUND.name()))
                .andExpect(jsonPath("$.errorMessage").value("Patient with ID " + patientId + " not found"))
                .andExpect(jsonPath("$.path").value("/api/orders"));

        assertThat(countBefore).isEqualTo(orderRepository.count());
    }

}
