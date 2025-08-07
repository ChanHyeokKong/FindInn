package com.inn.data.reserve;

import com.inn.data.rooms.RoomTypes;
import com.inn.data.rooms.Rooms;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
@Data
@Entity
public class Reserve {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    private LocalDate checkIn;
    private LocalDate checkOut;
    private String method;
    private LocalDateTime paymentDate;
    private Long price;
    private Long reserveHotelId;
    private Long reserveUserId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_type_id", referencedColumnName = "idx")
    private RoomTypes roomType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", referencedColumnName = "idx")
    private Rooms room;
}