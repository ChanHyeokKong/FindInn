package com.inn.data.rooms;

import com.inn.data.hotel.HotelEntity;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class RoomTypes {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;
    String typeName;
    String description;
    private Long price;
    private Long capacity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id", referencedColumnName = "idx")
    private HotelEntity hotel;

    String imageUrl;
}
