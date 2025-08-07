package com.inn.data.detail;

import com.inn.data.rooms.RoomTypeAvailDto;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;


@Data
public class AccommodationDto {
    private String name; // 숙소 이름 (예: "강릉 더끌림 펜션")
    private String address; // 주소
    private List<String> imageGalleries; // 이미지 갤러리 URL 리스트
    private LocalDate checkInDate; // 체크인 날짜
    private LocalDate checkOutDate; // 체크아웃 날짜
    private String checkInTime; // 체크인 시간
    private String checkOutTime; // 체크아웃 시간
    private int personal; // 인원
    private List<RoomTypeAvailDto> roomTypes; // 객실 정보 리스트

}