package com.PrzeBarCore.Laboratorymanagementsystem.integration;

import com.PrzeBarCore.Laboratorymanagementsystem.dto.events.MedicalOrderStatusChangedEvent;
import com.PrzeBarCore.Laboratorymanagementsystem.entity.MedicalOrder;
import com.PrzeBarCore.Laboratorymanagementsystem.entity.OutboxEvent;
import com.PrzeBarCore.Laboratorymanagementsystem.entity.Patient;
import com.PrzeBarCore.Laboratorymanagementsystem.global.AggregateType;
import com.PrzeBarCore.Laboratorymanagementsystem.global.EventType;
import com.PrzeBarCore.Laboratorymanagementsystem.global.OrderStatus;
import com.PrzeBarCore.Laboratorymanagementsystem.outbox.OutboxEventPublisher;
import com.PrzeBarCore.Laboratorymanagementsystem.repository.MedicalOrderRepository;
import com.PrzeBarCore.Laboratorymanagementsystem.repository.OutboxEventRepository;
import com.PrzeBarCore.Laboratorymanagementsystem.repository.PatientRepository;
import com.PrzeBarCore.Laboratorymanagementsystem.service.MedicalOrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@SpringBootTest
@Testcontainers
public class MedicalOrderOutboxIntegrationTest {
    @Container
    @ServiceConnection
    private static PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer(DockerImageName.parse("postgres:16"));
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MedicalOrderService medicalOrderService;
    @Autowired
    private OutboxEventPublisher outboxEventPublisher;
    @Autowired
    private MedicalOrderRepository medicalOrderRepository;
    @Autowired
    private OutboxEventRepository outboxEventRepository;
    @Autowired
    private PatientRepository patientRepository;

    @Test
    void shouldCreateOutboxEventWhenMedicalOrderStatusChanges(){
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
        MedicalOrder savedOrder= medicalOrderRepository.save(medicalOrder);

        medicalOrderService.updateStatus(savedOrder.getId(), OrderStatus.IN_PROCESS);
        List<OutboxEvent> events = outboxEventRepository.findAll();

        assertThat(events).hasSize(1);
        OutboxEvent event = events.get(0);

        assertThat(event.getAggregateId()).isEqualTo(savedOrder.getId());
        assertThat(event.getAggregateType()).isEqualTo(AggregateType.MEDICAL_ORDER);
        assertThat(event.getEventType()).isEqualTo(EventType.MEDICAL_ORDER_STATUS_CHANGED);
        assertThat(event.getProcessedAt()).isNull();

        MedicalOrder updatedOrder = medicalOrderRepository.findById(savedOrder.getId()).orElseThrow();
        assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.IN_PROCESS);
        assertThat(updatedOrder.getVersion()).isEqualTo(1L);

        var payload = objectMapper.readValue(event.getPayload(), MedicalOrderStatusChangedEvent.class);

        assertThat(payload.newStatus()).isEqualTo(OrderStatus.IN_PROCESS);
        assertThat(payload.oldStatus()).isEqualTo(OrderStatus.NEW);
        assertThat(payload.orderId()).isEqualTo(savedOrder.getId());
        assertThat(payload.changedAt()).isNotNull();
    }

    @Test
    void shouldProcessPendingEvents(){
        OutboxEvent event = new OutboxEvent();
        event.setPayload("payload");
        event.setCreatedAt(LocalDateTime.now());
        event.setAggregateType(AggregateType.MEDICAL_ORDER);
        event.setAggregateId(1L);
        event.setEventType(EventType.MEDICAL_ORDER_STATUS_CHANGED);
        OutboxEvent savedEvent = outboxEventRepository.save(event);

        assertThat(savedEvent.getProcessedAt()).isNull();
        outboxEventPublisher.processEvents();
        OutboxEvent processedEvent = outboxEventRepository.findById(savedEvent.getId()).orElseThrow();
        assertThat(processedEvent.getProcessedAt()).isNotNull();
    }

    @BeforeEach
    void clearData(){
        outboxEventRepository.deleteAll();
        medicalOrderRepository.deleteAll();
        patientRepository.deleteAll();
    }
}
