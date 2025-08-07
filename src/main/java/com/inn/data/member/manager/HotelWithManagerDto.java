package com.inn.data.member.manager;

import com.inn.data.hotel.HotelEntity;
import com.inn.data.rooms.RoomTypes;
import com.inn.data.rooms.Rooms;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HotelWithManagerDto {
    private HotelEntity hotelEntity;
    private Rooms rooms;
    private String memberName;
    private RoomTypes roomTypes;

    public HotelWithManagerDto(HotelEntity hotelEntity, String memberName) {
        this.hotelEntity = hotelEntity;
        this.memberName = memberName;
    }

}
