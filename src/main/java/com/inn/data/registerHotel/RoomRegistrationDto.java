package com.inn.data.registerHotel;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class RoomRegistrationDto {
    private String typeName;
    private String description;
    private Long price;
    private Long maxCapacity;
    private MultipartFile imageFile;
}