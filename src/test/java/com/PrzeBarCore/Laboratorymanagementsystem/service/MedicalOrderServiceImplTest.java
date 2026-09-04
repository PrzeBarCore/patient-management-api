package com.PrzeBarCore.Laboratorymanagementsystem.service;

import com.PrzeBarCore.Laboratorymanagementsystem.dto.request.CreateMedicalOrderRequest;
import com.PrzeBarCore.Laboratorymanagementsystem.dto.response.MedicalOrderResponse;
import com.PrzeBarCore.Laboratorymanagementsystem.entity.MedicalOrder;
import com.PrzeBarCore.Laboratorymanagementsystem.entity.Patient;
import com.PrzeBarCore.Laboratorymanagementsystem.exception.InvalidOrderStatusTransitionException;
import com.PrzeBarCore.Laboratorymanagementsystem.exception.MedicalOrderNotFoundException;
import com.PrzeBarCore.Laboratorymanagementsystem.exception.PatientNotFoundException;
import com.PrzeBarCore.Laboratorymanagementsystem.global.OrderStatus;
import com.PrzeBarCore.Laboratorymanagementsystem.repository.MedicalOrderRepository;
import com.PrzeBarCore.Laboratorymanagementsystem.repository.PatientRepository;
import com.PrzeBarCore.Laboratorymanagementsystem.service.impl.MedicalOrderServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MedicalOrderServiceImplTest {
    @Mock
    private MedicalOrderRepository orderRepository;
    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private MedicalOrderServiceImpl service;
    
    @Test
    void shouldCreateOrderWhenPatientExists() {
        // Arrange
        Long patientId = 1L;

        Patient patient = new Patient();
        patient.setId(patientId);

        CreateMedicalOrderRequest request =
                new CreateMedicalOrderRequest(patientId);

        MedicalOrder savedOrder = new MedicalOrder();
        savedOrder.setId(10L);
        savedOrder.setPatient(patient);
        savedOrder.setStatus(OrderStatus.NEW);
        savedOrder.setRegistrationDateTime(LocalDateTime.now());

        when(patientRepository.findById(patientId))
                .thenReturn(Optional.of(patient));
        when(orderRepository.save(any(MedicalOrder.class)))
                .thenReturn(savedOrder);

        // Act
        MedicalOrderResponse response = service.create(request);
        // Assert
        ArgumentCaptor<MedicalOrder> orderCaptor = ArgumentCaptor.forClass(MedicalOrder.class);
        verify(orderRepository).save(orderCaptor.capture());
        MedicalOrder capturedOrder = orderCaptor.getValue();

        assertThat(capturedOrder.getPatient()).isEqualTo(patient);
        assertThat(capturedOrder.getStatus()).isEqualTo(OrderStatus.NEW);
        assertThat(capturedOrder.getRegistrationDateTime()).isNotNull();

        assertThat(response.patientId()).isEqualTo(patientId);
        verify(patientRepository).findById(patientId);
    }

    @Test
    void shouldThrowExceptionWhenPatientForOrderDoesNotExist() {
        // Arrange
        Long patientId = 1L;

        CreateMedicalOrderRequest request =
                new CreateMedicalOrderRequest(patientId);

        when(patientRepository.findById(patientId)).thenReturn(Optional.empty());

        // Act
        PatientNotFoundException exception = assertThrows(PatientNotFoundException.class, () -> service.create(request));

        // Assert
        assertThat(exception.getMessage()).isEqualTo("Patient with ID " + patientId + " not found");

        verify(patientRepository).findById(patientId);
        verify(orderRepository, never()).save(ArgumentMatchers.any(MedicalOrder.class));
    }

    @Test
    void shouldReturnExistingMedicalOrder() {
        // Arrange
        Long orderId = 10L;
        Long patientId = 1L;
        OrderStatus status = OrderStatus.NEW;
        LocalDateTime registrationDate = LocalDateTime.now();

        Patient patient = new Patient();
        patient.setId(patientId);

        MedicalOrder savedOrder = new MedicalOrder();
        savedOrder.setId(orderId);
        savedOrder.setPatient(patient);
        savedOrder.setStatus(status);
        savedOrder.setRegistrationDateTime(registrationDate);

        MedicalOrderResponse expectedResponse = new MedicalOrderResponse(orderId,registrationDate,status,patientId);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(savedOrder));
        // Act
        MedicalOrderResponse response = service.findById(orderId);
        // Assert
        assertThat(response).isEqualTo(expectedResponse);
        verify(orderRepository).findById(orderId);
    }

    @Test
    void shouldThrowExceptionWhenMedicalOrderDoesNotExist() {
        // Arrange
        Long orderId = 10L;

        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());
        // Act
        MedicalOrderNotFoundException exception = assertThrows(MedicalOrderNotFoundException.class, () -> service.findById(orderId));
        // Assert
        assertThat(exception.getMessage()).isEqualTo("Order with ID " +orderId + " not found");
        verify(orderRepository).findById(orderId);
    }

    @Test
    void shouldReturnListOfOrders() {
        // Arrange
        Long patientId = 1L;
        Patient patient = new Patient();
        patient.setId(patientId);

        Long orderId1 = 10L;
        OrderStatus status1 = OrderStatus.NEW;
        LocalDateTime registrationDate1 = LocalDateTime.now();

        Long orderId2 = 11L;
        OrderStatus status2 = OrderStatus.IN_PROCESS;
        LocalDateTime registrationDate2 = LocalDateTime.now();


        MedicalOrder repositoryOrder1 = new MedicalOrder();
        repositoryOrder1.setId(orderId1);
        repositoryOrder1.setPatient(patient);
        repositoryOrder1.setStatus(status1);
        repositoryOrder1.setRegistrationDateTime(registrationDate1);
        MedicalOrderResponse expectedResponse1 = new MedicalOrderResponse(orderId1,registrationDate1, status1, patientId);

        MedicalOrder repositoryOrder2 = new MedicalOrder();
        repositoryOrder2.setId(orderId2);
        repositoryOrder2.setPatient(patient);
        repositoryOrder2.setStatus(status2);
        repositoryOrder2.setRegistrationDateTime(registrationDate2);
        MedicalOrderResponse expectedResponse2 = new MedicalOrderResponse(orderId2,registrationDate2, status2, patientId);

        when(orderRepository.findAll()).thenReturn(List.of(repositoryOrder1,repositoryOrder2));
        // Act
        List<MedicalOrderResponse> result = service.findAll();
        // Assert
        assertThat(result).containsExactly(expectedResponse1, expectedResponse2);

        verify(orderRepository).findAll();
    }

    @Test
    void shouldReturnEmptyList() {
        // Arrange
        when(orderRepository.findAll()).thenReturn(List.of());
        // Act
        List<MedicalOrderResponse> result = service.findAll();
        // Assert
        assertThat(result).isEmpty();

        verify(orderRepository).findAll();
    }

    @Test
    void shouldDeleteExistingOrder() {
        // Arrange
        Patient patient = new Patient();
        patient.setId(1L);

        Long orderId = 10L;
        MedicalOrder order = new MedicalOrder();
        order.setId(orderId);
        order.setPatient(patient);
        order.setStatus(OrderStatus.NEW);
        order.setRegistrationDateTime(LocalDateTime.now());

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        // Act
        service.delete(orderId);
        // Assert

        verify(orderRepository).findById(orderId);
        verify(orderRepository).delete(order);
    }

    @Test
    void shouldThrowExceptionWhenOrderToDeleteDoesNotExist() {
        // Arrange
        Long orderId = 10L;

        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());
        // Act
        MedicalOrderNotFoundException exception = assertThrows(MedicalOrderNotFoundException.class, () -> service.delete(orderId));
        // Assert
        assertThat(exception.getMessage()).isEqualTo("Order with ID " +orderId + " not found");

        verify(orderRepository).findById(orderId);
        verify(orderRepository, never()).delete(ArgumentMatchers.any());
    }

    @Test
    void shouldUpdateStatusFromNewToInProcess(){
        //Arrange
        Patient patient = new Patient();
        patient.setId(2L);

        MedicalOrder order = new MedicalOrder();
        Long orderId = 1L;
        OrderStatus statusToUpdate = OrderStatus.IN_PROCESS;

        order.setId(orderId);
        order.setStatus(OrderStatus.NEW);
        order.setPatient(patient);
        order.setRegistrationDateTime(LocalDateTime.now());

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        //Act
        MedicalOrderResponse response = service.updateStatus(orderId, statusToUpdate);
        //Assert
        assertThat(response.status()).isEqualTo(statusToUpdate);
        assertThat(order.getStatus()).isEqualTo(statusToUpdate);

        verify(orderRepository).findById(orderId);
        verify(orderRepository, never()).save(ArgumentMatchers.any(MedicalOrder.class));
    }

    @Test
    void shouldThrowExceptionWhenUpdatingStatusFromNewToCompleted(){
        //Arrange
        Patient patient = new Patient();
        patient.setId(2L);

        MedicalOrder order = new MedicalOrder();
        Long orderId = 1L;
        OrderStatus statusToUpdate = OrderStatus.COMPLETED;

        order.setId(orderId);
        order.setStatus(OrderStatus.NEW);
        order.setPatient(patient);
        order.setRegistrationDateTime(LocalDateTime.now());

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        //Act
        var exception = assertThrows(InvalidOrderStatusTransitionException.class, () -> service.updateStatus(orderId, statusToUpdate));
        //Assert
        assertThat(order.getStatus()).isEqualTo(OrderStatus.NEW);
        assertThat(exception.getMessage()).isEqualTo("Cannot change order status from " + OrderStatus.NEW + " to " + statusToUpdate);

        verify(orderRepository).findById(orderId);
        verify(orderRepository, never()).save(ArgumentMatchers.any(MedicalOrder.class));
    }
}
