package com.inn.data.rooms;

import com.inn.data.hotel.HotelEntity;
import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

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
    String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id", referencedColumnName = "idx")
    private HotelEntity hotel;

    @OneToMany(
            mappedBy = "roomType",
            cascade = CascadeType.ALL
    )
    List<Rooms> rooms = new ArrayList<>();

    public void addRoom(Rooms room) {
        rooms.add(room);
        room.setRoomType(this);
    }
}
