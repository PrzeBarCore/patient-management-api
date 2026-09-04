package com.PrzeBarCore.Laboratorymanagementsystem.repository;

import com.PrzeBarCore.Laboratorymanagementsystem.dto.request.UpdatePatientRequest;
import com.PrzeBarCore.Laboratorymanagementsystem.entity.Patient;
import com.PrzeBarCore.Laboratorymanagementsystem.service.PatientService;
import com.PrzeBarCore.Laboratorymanagementsystem.service.impl.PatientServiceImpl;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.postgresql.util.PSQLException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@Import(PatientServiceImpl.class)
class PatientRepositoryIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer(DockerImageName.parse("postgres:16"));

    @Autowired
    private PatientRepository repository;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private PatientService service;

    @Test
    void shouldSaveAndFindPatient(){
        // Arrange
        final String firstName = "Kapitan";
        final String lastName = "Bomba";
        final String pesel = "12345678901";
        final LocalDate birthDate = LocalDate.of(1990,1,1);

        Patient patient = new Patient();
        patient.setFirstName(firstName);
        patient.setLastName(lastName);
        patient.setPesel(pesel);
        patient.setBirthDate(birthDate);

        // Act
        Patient savedPatient = repository.saveAndFlush(patient);
        entityManager.clear();
        Optional<Patient> foundPatient =
                repository.findById(savedPatient.getId());

        // Assert
        assertThat(foundPatient).isPresent();
        assertThat(savedPatient.getId())
                .isEqualTo(foundPatient.get().getId());
        assertThat(foundPatient.get().getFirstName())
                .isEqualTo(firstName);
        assertThat(foundPatient.get().getLastName())
                .isEqualTo(lastName);
        assertThat(foundPatient.get().getPesel())
                .isEqualTo(pesel);
        assertThat(foundPatient.get().getBirthDate())
                .isEqualTo(birthDate);
    }

    @Test
    void shouldThrowExceptionWhenPeselIsNotUnique(){
        // Arrange
        final String pesel = "12345678901";
        final String firstName1 = "Kapitan";
        final String lastName1 = "Bomba";
        final LocalDate birthDate1 = LocalDate.of(1990,1,1);

        Patient patient1 = new Patient();
        patient1.setFirstName(firstName1);
        patient1.setLastName(lastName1);
        patient1.setBirthDate(birthDate1);
        patient1.setPesel(pesel);

        final String firstName2 = "Tytus";
        final String lastName2 = "Bombatdiero";
        final LocalDate birthDate2 = LocalDate.of(1991,1,1);

        Patient patient2 = new Patient();
        patient2.setFirstName(firstName2);
        patient2.setLastName(lastName2);
        patient2.setBirthDate(birthDate2);
        patient2.setPesel(pesel);

        // Act
        repository.saveAndFlush(patient1);

        // Assert
        assertThrows(DataIntegrityViolationException.class, () -> repository.saveAndFlush(patient2));
    }

    @Test
    void shouldUpdatePatientWithoutDirectSave(){
        // Arrange
        final String pesel = "12345678901";
        final String firstName = "Kapitan";
        final String lastName = "Bomba";
        final LocalDate birthDate = LocalDate.of(1990,1,1);
        final String firstName2 = "Tytus";
        final String lastName2 = "Bombardiero";
        final LocalDate birthDate2 = LocalDate.of(1991,1,1);

        Patient patient = new Patient();
        patient.setFirstName(firstName);
        patient.setLastName(lastName);
        patient.setBirthDate(birthDate);
        patient.setPesel(pesel);

        UpdatePatientRequest request = new UpdatePatientRequest(firstName2, lastName2, birthDate2);

        // Act
        Patient savedPatient = repository.saveAndFlush(patient);
        service.update(savedPatient.getId(), request);
        entityManager.flush();
        entityManager.clear();
        Optional<Patient> foundPatient = repository.findById(savedPatient.getId());
        // Assert
        assertThat(foundPatient).isPresent();
        assertThat(foundPatient.get().getFirstName()).isEqualTo(firstName2);
        assertThat(foundPatient.get().getLastName()).isEqualTo(lastName2);
        assertThat(foundPatient.get().getBirthDate()).isEqualTo(birthDate2);
        assertThat(foundPatient.get().getPesel()).isEqualTo(pesel);
    }
}
