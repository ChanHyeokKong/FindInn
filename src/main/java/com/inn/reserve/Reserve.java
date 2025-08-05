package com.inn.reserve;

import com.inn.rooms.RoomTypes;
import com.inn.rooms.Rooms;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
@Data
@Entity
public class Reserve {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long reserveId;

    private LocalDate checkIn;
    private LocalDate checkOut;
    private String method;
    private LocalDateTime paymentDate;
    private long price;
    private long reserveHotelId;
    private long reserveUserId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_type_id", referencedColumnName = "id")
    private RoomTypes roomType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", referencedColumnName = "id")
    private Rooms room;
}