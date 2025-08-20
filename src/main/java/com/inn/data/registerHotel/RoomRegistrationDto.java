package com.inn.data.registerHotel;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class RoomRegistrationDto {
    private String typeName;
    private String description;
    private Integer price;
    private Integer maxCapacity;
    private MultipartFile imageFile;
}