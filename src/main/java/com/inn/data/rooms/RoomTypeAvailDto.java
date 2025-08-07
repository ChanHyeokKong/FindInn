package com.inn.data.rooms;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RoomTypeAvailDto {
    long id;
    String typeName;
    String description;
    long price;
    long capacity;
    long hotelId;
    String imageUrl;
    boolean available;

    public RoomTypeAvailDto(RoomTypes roomType, boolean isAvailable) {
        this.id = roomType.getId();
        this.typeName = roomType.getTypeName();
        this.description = roomType.getDescription();
        this.capacity = roomType.getCapacity();
        this.price = roomType.getPrice();
        this.available = isAvailable;
    }
}