package com.przebarcore.laboratoryapi.service.impl;

import com.przebarcore.laboratoryapi.dto.events.MedicalOrderCreatedEvent;
import com.przebarcore.laboratoryapi.dto.events.MedicalOrderStatusChangedEvent;
import com.przebarcore.laboratoryapi.dto.request.CreateMedicalOrderRequest;
import com.przebarcore.laboratoryapi.dto.response.MedicalOrderResponse;
import com.przebarcore.laboratoryapi.entity.MedicalOrder;
import com.przebarcore.laboratoryapi.entity.OutboxEvent;
import com.przebarcore.laboratoryapi.entity.Patient;
import com.przebarcore.laboratoryapi.exception.EventSerializationException;
import com.przebarcore.laboratoryapi.exception.InvalidOrderStatusTransitionException;
import com.przebarcore.laboratoryapi.exception.MedicalOrderNotFoundException;
import com.przebarcore.laboratoryapi.exception.PatientNotFoundException;
import com.przebarcore.laboratoryapi.global.AggregateType;
import com.przebarcore.laboratoryapi.global.EventType;
import com.przebarcore.laboratoryapi.global.OrderStatus;
import com.przebarcore.laboratoryapi.mapper.MedicalOrderMapper;
import com.przebarcore.laboratoryapi.repository.MedicalOrderRepository;
import com.przebarcore.laboratoryapi.repository.OutboxEventRepository;
import com.przebarcore.laboratoryapi.repository.PatientRepository;
import com.przebarcore.laboratoryapi.service.MedicalOrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MedicalOrderServiceImpl implements MedicalOrderService {
    private final MedicalOrderRepository orderRepository;
    private final PatientRepository patientRepository;
    private final OutboxEventRepository eventRepository;
    private final ObjectMapper objectMapper;

    public MedicalOrderServiceImpl(MedicalOrderRepository orderRepository, PatientRepository patientRepository, OutboxEventRepository eventRepository, ObjectMapper objectMapper){
        this.orderRepository = orderRepository;
        this.patientRepository = patientRepository;
        this.eventRepository = eventRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public MedicalOrderResponse create(CreateMedicalOrderRequest medicalOrderRequest) {
        Patient patient = patientRepository.findById(medicalOrderRequest.patientId()).orElseThrow(() -> new PatientNotFoundException(medicalOrderRequest.patientId()));
        var order = new MedicalOrder();
        order.setRegistrationDateTime(LocalDateTime.now());
        order.setStatus(OrderStatus.NEW);
        order.setPatient(patient);

        var savedOrder = orderRepository.save(order);

        LocalDateTime eventTime = LocalDateTime.now();
        var event = new OutboxEvent();
        try{
            event.setPayload(objectMapper.writeValueAsString(new MedicalOrderCreatedEvent(savedOrder.getId(), savedOrder.getStatus(),eventTime)));
        } catch(JacksonException exception){
            throw new EventSerializationException(AggregateType.MEDICAL_ORDER, savedOrder.getId(), exception);
        }
        event.setAggregateId(savedOrder.getId());
        event.setAggregateType(AggregateType.MEDICAL_ORDER);
        event.setEventType(EventType.MEDICAL_ORDER_CREATED);
        event.setCreatedAt(eventTime);
        eventRepository.save(event);

        return MedicalOrderMapper.toResponse(savedOrder);
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

        LocalDateTime eventTime = LocalDateTime.now();
        var event = new OutboxEvent();
        try{
            event.setPayload(objectMapper.writeValueAsString(new MedicalOrderStatusChangedEvent(id, currentStatus, status, eventTime)));
        } catch(JacksonException exception){
            throw new EventSerializationException(AggregateType.MEDICAL_ORDER, id, exception.getCause());
        }
        event.setAggregateId(id);
        event.setAggregateType(AggregateType.MEDICAL_ORDER);
        event.setEventType(EventType.MEDICAL_ORDER_STATUS_CHANGED);
        event.setCreatedAt(eventTime);
        eventRepository.save(event);

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
