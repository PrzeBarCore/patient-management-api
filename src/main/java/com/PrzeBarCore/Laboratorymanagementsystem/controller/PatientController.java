package com.PrzeBarCore.Laboratorymanagementsystem.controller;

import com.PrzeBarCore.Laboratorymanagementsystem.dto.request.CreatePatientRequest;
import com.PrzeBarCore.Laboratorymanagementsystem.dto.request.UpdatePatientRequest;
import com.PrzeBarCore.Laboratorymanagementsystem.dto.response.PatientResponse;
import com.PrzeBarCore.Laboratorymanagementsystem.service.PatientService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
public class PatientController {
    private final PatientService service;

    public PatientController(PatientService service){
        this.service = service;
    }

    @PostMapping(path = "/api/patients")
    public ResponseEntity<PatientResponse> create(@Valid @RequestBody CreatePatientRequest request){
        PatientResponse response = service.create(request);
        URI location = URI.create("/api/patients/" + response.id());
        return ResponseEntity.created(location)
                .body(response);
    }

    @PutMapping(path = "/api/patients/{id}")
    public ResponseEntity<PatientResponse> update(@PathVariable Long id, @Valid @RequestBody UpdatePatientRequest request){
        PatientResponse response = service.update(id, request);
        URI location = URI.create("/api/patients/" + response.id());
        return ResponseEntity.ok()
                .body(response);
    }

    @DeleteMapping(path = "/api/patients/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(path = "/api/patients/{id}")
    public ResponseEntity<PatientResponse> findById(@PathVariable Long id){
        PatientResponse response = service.findById(id);
        URI location = URI.create("/api/patients/" + response.id());
        return ResponseEntity.ok()
                .body(response);
    }

    @GetMapping(path = "/api/patients/")
    public ResponseEntity<List<PatientResponse>> findAll(){
        List<PatientResponse> response = service.findAll();
        URI location = URI.create("/api/patients/");
        return ResponseEntity.ok()
                .body(response);
    }
}
