package com.inn.data.registerHotel;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class HotelEditDto {

    private Long idx;

    // --- Basic Hotel Information ---
    private String hotelName;
    private String hotelAddress;
    private String hotelTel;
    private String hotelCategory;
    private String description;

    private String hotelImage;
    private MultipartFile newHotelImageFile;

    private List<String> hotelImages;
    private List<MultipartFile> newHotelImageFiles;

    private TagEditDto tags;
    private List<RoomTypeEditDto> roomTypes;
}