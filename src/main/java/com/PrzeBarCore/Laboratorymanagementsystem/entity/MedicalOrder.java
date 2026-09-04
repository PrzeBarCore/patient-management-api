package com.PrzeBarCore.Laboratorymanagementsystem.entity;

import com.PrzeBarCore.Laboratorymanagementsystem.global.OrderStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "medical_order")
@Getter
@Setter
@NoArgsConstructor
public class MedicalOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private LocalDateTime registrationDateTime;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;
    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false, updatable = false)
    private Patient patient;
}
