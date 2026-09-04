package com.PrzeBarCore.Laboratorymanagementsystem.service;

import com.PrzeBarCore.Laboratorymanagementsystem.dto.request.CreatePatientRequest;
import com.PrzeBarCore.Laboratorymanagementsystem.dto.request.UpdatePatientRequest;
import com.PrzeBarCore.Laboratorymanagementsystem.dto.response.PatientResponse;
import com.PrzeBarCore.Laboratorymanagementsystem.entity.Patient;
import com.PrzeBarCore.Laboratorymanagementsystem.exception.PatientAlreadyExistsException;
import com.PrzeBarCore.Laboratorymanagementsystem.exception.PatientNotFoundException;
import com.PrzeBarCore.Laboratorymanagementsystem.repository.PatientRepository;
import com.PrzeBarCore.Laboratorymanagementsystem.service.impl.PatientServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientServiceImplTest {

    @Mock
    private PatientRepository repository;

    @InjectMocks
    private PatientServiceImpl service;

    @Test
    void shouldCreatePatientWhenPeselDoesNotExist(){
        //Arrange
        CreatePatientRequest request = new CreatePatientRequest(
                "Jaś",
                "Kapela",
                "11111111111",
                LocalDate.of(1990, 1,1)
        );

        Patient savedPatient = new Patient();
        savedPatient.setId(1L);
        savedPatient.setFirstName("Jaś");
        savedPatient.setLastName("Kapela");
        savedPatient.setPesel("11111111111");
        savedPatient.setBirthDate(LocalDate.of(1990, 1,1));

        when(repository.existsByPesel("11111111111")).thenReturn(false);
        when(repository.save(ArgumentMatchers.any(Patient.class))).thenReturn(savedPatient);

        //Act
        PatientResponse response = service.create(request);

        //Assert
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.firstName()).isEqualTo("Jaś");
        assertThat(response.lastName()).isEqualTo("Kapela");
        assertThat(response.birthDate()).isEqualTo(LocalDate.of(1990, 1,1));

        verify(repository).existsByPesel("11111111111");
        verify(repository).save(ArgumentMatchers.any(Patient.class));
    }

    @Test
    void shouldThrowExceptionWhenPatientWithPeselAlreadyExists(){
        //Arrange
        String pesel = "11111111111";
        CreatePatientRequest request = new CreatePatientRequest(
                "Jaś",
                "Kapela",
                pesel,
                LocalDate.of(1990, 1,1)
        );
        when(repository.existsByPesel(pesel)).thenReturn(true);

        //Act
        PatientAlreadyExistsException exception = assertThrows(PatientAlreadyExistsException.class, () -> service.create(request));

        //Assert
        assertThat(exception.getMessage()).isEqualTo("Patient with PESEL " + pesel + " already exists");

        verify(repository).existsByPesel(pesel);
        verify(repository, never()).save(ArgumentMatchers.any(Patient.class));
    }

    @Test
    void shouldFindPatientWhenIdExists(){
        //Arrange
        Patient repositoryPatient = new Patient();
        repositoryPatient.setId(1L);
        repositoryPatient.setFirstName("Zbyszko");
        repositoryPatient.setLastName("ZBogdańca");
        repositoryPatient.setBirthDate(LocalDate.of(1390, 1, 1));
        PatientResponse expectedPatient = new PatientResponse(1L, "Zbyszko", "ZBogdańca", LocalDate.of(1390, 1, 1));

        when(repository.findById(1L)).thenReturn(Optional.of(repositoryPatient));

        //Act
        PatientResponse patientResponse = service.findById(1L);

        //Assert
        assertThat(patientResponse).isEqualTo(expectedPatient);

        verify(repository).findById(1L);
    }

    @Test
    void shouldThrowExceptionWhenPatientDoesNotExist(){
        //Arrange
        Long id = 1L;
        when(repository.findById(id)).thenReturn(Optional.empty());

        //Act
        PatientNotFoundException exception = assertThrows(PatientNotFoundException.class, () -> service.findById(id));

        //Assert
        assertThat(exception.getMessage()).isEqualTo("Patient with ID " + id + " not found");

        verify(repository).findById(id);
        verify(repository).findById(id);
    }

    @Test
    void shouldReturnAllPatients(){
        //Arrange
        Long id1 = 1L;
        String firstName1 = "Zbyszko";
        String lastName1 = "ZBogdańca";
        LocalDate birthDate1 = LocalDate.of(1390, 1, 1);

        Patient repositoryPatient1 = new Patient();
        repositoryPatient1.setId(id1);
        repositoryPatient1.setFirstName(firstName1);
        repositoryPatient1.setLastName(lastName1);
        repositoryPatient1.setBirthDate(birthDate1);
        PatientResponse expectedPatient1 = new PatientResponse(id1, firstName1, lastName1, birthDate1);

        Long id2 = 2L;
        String firstName2 = "Jurand";
        String lastName2 = "ZeSpychowa";
        LocalDate birthDate2 = LocalDate.of(1390, 1, 1);
        Patient repositoryPatient2 = new Patient();
        repositoryPatient2.setId(id2);
        repositoryPatient2.setFirstName(firstName2);
        repositoryPatient2.setLastName(lastName2);
        repositoryPatient2.setBirthDate(birthDate2);
        PatientResponse expectedPatient2 = new PatientResponse(id2, firstName2, lastName2, birthDate2);

        when(repository.findAll()).thenReturn(List.of(repositoryPatient1, repositoryPatient2));

        //Act
        List<PatientResponse> patientList = service.findAll();

        //Assert

        assertThat(patientList).containsExactly(expectedPatient1, expectedPatient2);

        verify(repository).findAll();
    }

    @Test
    void shouldReturnEmptyListWhenNoPatientsExist(){
        //Arrange
        when(repository.findAll()).thenReturn(List.of());

        //Act
        List<PatientResponse> patientList = service.findAll();

        //Assert
        assertThat(patientList).isEmpty();
        verify(repository).findAll();
    }

    @Test
    void shouldUpdatePatientWhenPatientExists(){
        //Arrange
        Long id = 1L;
        Patient repositoryPatient = new Patient();
        repositoryPatient.setId(id);
        repositoryPatient.setFirstName("Zbyszko");
        repositoryPatient.setLastName("ZBogdańca");
        repositoryPatient.setBirthDate(LocalDate.of(1390, 1, 1));
        repositoryPatient.setPesel("12345678910");


        String newFirstName = "Zbychu";
        String newLastName = "ZBagoszna";
        LocalDate newBirthDate = LocalDate.of(1391, 2, 1);
        UpdatePatientRequest updateRequest = new UpdatePatientRequest(
                newFirstName,
                newLastName,
                newBirthDate
        );
        PatientResponse expectedPatient = new PatientResponse(id, newFirstName, newLastName, newBirthDate);


        when(repository.findById(id)).thenReturn(Optional.of(repositoryPatient));

        //Act
        PatientResponse patientResponse = service.update(id, updateRequest);

        //Assert
        assertThat(patientResponse).isEqualTo(expectedPatient);
        assertThat(repositoryPatient.getFirstName()).isEqualTo(newFirstName);
        assertThat(repositoryPatient.getLastName()).isEqualTo(newLastName);
        assertThat(repositoryPatient.getBirthDate()).isEqualTo(newBirthDate);
        assertThat(repositoryPatient.getPesel()).isEqualTo("12345678910");
        verify(repository).findById(id);
        verify(repository, never()).save(any(Patient.class));
    }

    @Test
    void shouldThrowExceptionWhenPatientToUpdateDoesNotExist(){
        //Arrange
        Long id = 1L;
        UpdatePatientRequest updateRequest = new UpdatePatientRequest(
                "Zbychu",
                "ZBagoszna",
                LocalDate.of(1391, 2, 1)
        );

        when(repository.findById(id)).thenReturn(Optional.empty());

        //Act
        PatientNotFoundException exception = assertThrows(PatientNotFoundException.class,
                () -> service.update(id, updateRequest));

        //Assert
        assertThat(exception.getMessage()).isEqualTo("Patient with ID " + id + " not found");

        verify(repository).findById(id);
    }

    @Test
    void shouldDeletePatientWhenPatientExists(){
        //Arrange
        Long id = 1L;
        Patient repositoryPatient = new Patient();
        repositoryPatient.setId(id);

        when(repository.findById(id)).thenReturn(Optional.of(repositoryPatient));

        //Act
        service.delete(id);

        //Assert
        verify(repository).findById(id);
        verify(repository).delete(repositoryPatient);
    }

    @Test
    void shouldThrowExceptionWhenPatientToDeleteDoesNotExist(){
        //Arrange
        Long id = 1L;
        when(repository.findById(id)).thenReturn(Optional.empty());

        //Act
        PatientNotFoundException exception = assertThrows(PatientNotFoundException.class,
                () -> service.delete(id));

        //Assert
        assertThat(exception.getMessage()).isEqualTo("Patient with ID " + id + " not found");

        verify(repository).findById(id);
        verify(repository, never()).delete(any(Patient.class));
    }
}
