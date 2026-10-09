package com.przebarcore.laboratoryapi.controller;

import com.przebarcore.laboratoryapi.dto.request.CreateMedicalOrderRequest;
import com.przebarcore.laboratoryapi.dto.request.UpdateMedicalOrderStatusRequest;
import com.przebarcore.laboratoryapi.dto.response.MedicalOrderResponse;
import com.przebarcore.laboratoryapi.service.MedicalOrderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
public class MedicalOrderController {
    private final MedicalOrderService orderService;

    public MedicalOrderController(MedicalOrderService orderService) { this.orderService = orderService; }

    @PostMapping(path = "/api/orders")
    public ResponseEntity<MedicalOrderResponse> createOrder(@Valid @RequestBody CreateMedicalOrderRequest request){
        MedicalOrderResponse response = orderService.create(request);
        URI location = URI.create("/api/orders/" + response.id());
        return ResponseEntity.created(location)
                .body(response);
    }

    @PatchMapping(path = "/api/orders/{id}/status")
    public ResponseEntity<MedicalOrderResponse> updateOrderStatus(@PathVariable Long id, @Valid @RequestBody UpdateMedicalOrderStatusRequest request){
        MedicalOrderResponse response = orderService.updateStatus(id, request.newStatus());
        URI location = URI.create("/api/orders/" + response.id() + "/status");
        return ResponseEntity.ok()
                .location(location)
                .body(response);
    }

    @GetMapping(path = "/api/orders")
    public ResponseEntity<List<MedicalOrderResponse>> findAll(){
        return ResponseEntity.ok().body(orderService.findAll());
    }

    @GetMapping(path = "/api/orders/{id}")
    public ResponseEntity<MedicalOrderResponse> findById(@PathVariable Long id){
        return ResponseEntity.ok().body(orderService.findById(id));
    }

    @DeleteMapping(path = "/api/orders/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        orderService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
