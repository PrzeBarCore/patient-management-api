package com.przebarcore.laboratoryapi.integration;

import com.przebarcore.laboratoryapi.entity.MedicalOrder;
import com.przebarcore.laboratoryapi.entity.OutboxEvent;
import com.przebarcore.laboratoryapi.entity.Patient;
import com.przebarcore.laboratoryapi.global.OrderStatus;
import com.przebarcore.laboratoryapi.repository.MedicalOrderRepository;
import com.przebarcore.laboratoryapi.repository.OutboxEventRepository;
import com.przebarcore.laboratoryapi.repository.PatientRepository;
import com.przebarcore.laboratoryapi.service.MedicalOrderService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Testcontainers
@SpringBootTest
public class MedicalOrderOutboxRollbackIntegrationTest {
    @Container
    @ServiceConnection
    private static PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer(DockerImageName.parse("postgres:16"));

    @Autowired
    private MedicalOrderService medicalOrderService;
    @Autowired
    private MedicalOrderRepository medicalOrderRepository;
    @Autowired
    private PatientRepository patientRepository;

    @MockitoBean
    private OutboxEventRepository outboxEventRepository;

    @Test
    void shouldRollbackChangesWhenEventIsNotCreated(){
        Patient patient = new Patient();
        patient.setFirstName("Zbyszko");
        patient.setLastName("TrzyCytryny");
        patient.setPesel("12345678910");
        patient.setBirthDate(LocalDate.of(1390,1,1));
        Patient savedPatient = patientRepository.save(patient);

        MedicalOrder medicalOrder = new MedicalOrder();
        medicalOrder.setPatient(savedPatient);
        medicalOrder.setStatus(OrderStatus.NEW);
        medicalOrder.setRegistrationDateTime(LocalDateTime.now());
        MedicalOrder savedOrder = medicalOrderRepository.save(medicalOrder);

        when(outboxEventRepository.save(ArgumentMatchers.any(OutboxEvent.class))).thenThrow(new RuntimeException("Simulated outbox failure"));

        assertThrows(
                RuntimeException.class,
                () -> medicalOrderService.updateStatus(savedOrder.getId(), OrderStatus.IN_PROCESS)
        );

        var resultOrder = medicalOrderRepository.findById(savedOrder.getId()).orElseThrow();

        assertThat(resultOrder.getStatus()).isEqualTo(OrderStatus.NEW);
        assertThat(resultOrder.getVersion()).isEqualTo(0L);
        verify(outboxEventRepository).save(ArgumentMatchers.any(OutboxEvent.class));
    }
}
