package com.PrzeBarCore.Laboratorymanagementsystem.controller;

import com.PrzeBarCore.Laboratorymanagementsystem.dto.request.CreateMedicalOrderRequest;
import com.PrzeBarCore.Laboratorymanagementsystem.dto.response.MedicalOrderResponse;
import com.PrzeBarCore.Laboratorymanagementsystem.service.MedicalOrderService;
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
