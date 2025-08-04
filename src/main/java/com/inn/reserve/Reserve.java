package com.inn.reserve; // Or your actual package

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
@Data
@Entity
public class Reserve {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reserveId;
    private Long reserveUserId;
    private Long reserveHotelId;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private LocalDateTime paymentDate;
    private String method;
    private Long price;
}