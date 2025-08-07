package com.inn.data.rooms;

import jakarta.persistence.*;
import lombok.Data;


@Data
@Entity
public class Rooms {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private long roomNumber;

    private long hotelId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_type_id", referencedColumnName = "id")
    private RoomTypes roomType;
}