package com.inn.data.rooms;

import jakarta.persistence.*;
import lombok.Data;


@Data
@Entity
public class Rooms {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    private Long roomNumber;

    private Long hotelId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_type_id", referencedColumnName = "idx")
    private RoomTypes roomType;
}