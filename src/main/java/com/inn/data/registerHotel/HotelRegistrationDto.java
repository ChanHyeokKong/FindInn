package com.inn.data.registerHotel;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@Data
public class HotelRegistrationDto {
    private String hotel_name;
    private String postcode;
    private String address;
    private String detailAddress;
    private String category;
    private String desc;

    private List<MultipartFile> imageFiles;
    private List<String> tag;

    private List<RoomRegistrationDto> rooms;

}