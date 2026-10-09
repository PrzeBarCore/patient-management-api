package com.przebarcore.laboratoryapi.integration;

import com.przebarcore.laboratoryapi.entity.MedicalOrder;
import com.przebarcore.laboratoryapi.entity.Patient;
import com.przebarcore.laboratoryapi.global.OrderStatus;
import com.przebarcore.laboratoryapi.outbox.EventSender;
import com.przebarcore.laboratoryapi.repository.MedicalOrderRepository;
import com.przebarcore.laboratoryapi.repository.PatientRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Testcontainers
public class MedicalOrderOptimisticLockIntegrationTest {
    @Container
    @ServiceConnection
    private static  PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer(DockerImageName.parse("postgres:16"));

    @MockitoBean
    private EventSender eventSender;
    @Autowired
    private MedicalOrderRepository medicalOrderRepository;
    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    PlatformTransactionManager transactionManager;

    @Test
    void shouldRejectUpdateWhenMedicalOrderVersionIsStale(){
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

        TransactionTemplate transactionTemplate =
                new TransactionTemplate(transactionManager);

        MedicalOrder orderA = transactionTemplate.execute(status ->
                medicalOrderRepository.findById(savedOrder.getId())
                        .orElseThrow()
        );

        MedicalOrder orderB = transactionTemplate.execute(status ->
                medicalOrderRepository.findById(savedOrder.getId())
                        .orElseThrow()
        );

        assertThat(orderA.getVersion()).isEqualTo(0L);
        assertThat(orderB.getVersion()).isEqualTo(0L);
        assertThat(orderA).isNotSameAs(orderB);

        orderA.setStatus(OrderStatus.IN_PROCESS);
        transactionTemplate.execute(status -> medicalOrderRepository.saveAndFlush(orderA));


        orderB.setStatus(OrderStatus.CANCELED);
        var exception = assertThrows(ObjectOptimisticLockingFailureException.class, () ->transactionTemplate.execute(status -> medicalOrderRepository.saveAndFlush(orderB)));

        MedicalOrder result = medicalOrderRepository.findById(savedOrder.getId()).orElseThrow();
        assertThat(result.getVersion()).isEqualTo(1);
        assertThat(result.getStatus()).isEqualTo(OrderStatus.IN_PROCESS);
    }
}
