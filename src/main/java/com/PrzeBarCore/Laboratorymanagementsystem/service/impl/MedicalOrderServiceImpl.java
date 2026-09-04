package com.PrzeBarCore.Laboratorymanagementsystem.service.impl;

import com.PrzeBarCore.Laboratorymanagementsystem.dto.request.CreateMedicalOrderRequest;
import com.PrzeBarCore.Laboratorymanagementsystem.dto.response.MedicalOrderResponse;
import com.PrzeBarCore.Laboratorymanagementsystem.entity.MedicalOrder;
import com.PrzeBarCore.Laboratorymanagementsystem.entity.Patient;
import com.PrzeBarCore.Laboratorymanagementsystem.exception.InvalidOrderStatusTransitionException;
import com.PrzeBarCore.Laboratorymanagementsystem.exception.MedicalOrderNotFoundException;
import com.PrzeBarCore.Laboratorymanagementsystem.exception.PatientNotFoundException;
import com.PrzeBarCore.Laboratorymanagementsystem.global.OrderStatus;
import com.PrzeBarCore.Laboratorymanagementsystem.mapper.MedicalOrderMapper;
import com.PrzeBarCore.Laboratorymanagementsystem.repository.MedicalOrderRepository;
import com.PrzeBarCore.Laboratorymanagementsystem.repository.PatientRepository;
import com.PrzeBarCore.Laboratorymanagementsystem.service.MedicalOrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MedicalOrderServiceImpl implements MedicalOrderService {
    private final MedicalOrderRepository orderRepository;
    private final PatientRepository patientRepository;

    public MedicalOrderServiceImpl(MedicalOrderRepository orderRepository, PatientRepository patientRepository){
        this.orderRepository = orderRepository;
        this.patientRepository = patientRepository;
    }

    @Override
    @Transactional
    public MedicalOrderResponse create(CreateMedicalOrderRequest medicalOrderRequest) {
        Patient patient = patientRepository.findById(medicalOrderRequest.patientId()).orElseThrow(() -> new PatientNotFoundException(medicalOrderRequest.patientId()));
        var order = new MedicalOrder();
        order.setRegistrationDateTime(LocalDateTime.now());
        order.setStatus(OrderStatus.NEW);
        order.setPatient(patient);
        return MedicalOrderMapper.toResponse(orderRepository.save(order));
    }

    @Override
    @Transactional
    public MedicalOrderResponse updateStatus(Long id, OrderStatus status) {
        MedicalOrder order = orderRepository.findById(id).orElseThrow(() -> new MedicalOrderNotFoundException(id));
        boolean allowedTransition = false;
        OrderStatus currentStatus = order.getStatus();

        if(!currentStatus.equals(status)){
            switch(currentStatus){
                case NEW ->{
                    if(!status.equals(OrderStatus.COMPLETED))
                        allowedTransition = true;
                }
                case IN_PROCESS ->{
                    if(!status.equals(OrderStatus.NEW))
                        allowedTransition = true;
                }
            }
        }

        if(!allowedTransition)
            throw new InvalidOrderStatusTransitionException(order.getStatus(), status);
        order.setStatus(status);

        return MedicalOrderMapper.toResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicalOrderResponse> findAll() {
        return orderRepository.findAll().stream().map(MedicalOrderMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MedicalOrderResponse findById(Long id) {
        MedicalOrder order = orderRepository.findById(id).orElseThrow(() -> new MedicalOrderNotFoundException(id));
        return MedicalOrderMapper.toResponse(order);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        MedicalOrder order = orderRepository.findById(id).orElseThrow(() -> new MedicalOrderNotFoundException(id));
        orderRepository.delete(order);
    }
}
