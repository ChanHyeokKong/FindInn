package com.inn.data.rooms;

import com.inn.data.hotel.HotelEntity;
import jakarta.persistence.*;
import lombok.Data;


@Data
@Entity
public class Rooms {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    private Long roomNumber;

    @ManyToOne
    @JoinColumn(name = "hotel_id", referencedColumnName = "idx")
    private HotelEntity hotel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_type_id", referencedColumnName = "idx")
    private RoomTypes roomType;
}