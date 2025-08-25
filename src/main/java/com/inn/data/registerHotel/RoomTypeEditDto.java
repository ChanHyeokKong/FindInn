package com.inn.data.registerHotel;


import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class RoomTypeEditDto {
    private Long idx;

    // --- Room Details ---
    private String typeName;
    private String description;
    private Long price;
    private Long capacity; // Matches the 'capacity' field in your RoomTypes entity

    private String imageUrl;
    private MultipartFile imageFile;
}