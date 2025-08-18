package com.inn.data.review;

import com.inn.data.hotel.HotelEntity;
import com.inn.data.member.MemberDto;
import com.inn.data.rooms.RoomTypes;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class ReviewDto {
    public long idx;
    public long bookingId;
    public long memberId;
    public MemberDto member;
    public Long hotelId;
    public String hotelName;
    public String roomName;
    public String hotelImages;
    public Long roomTypeId;
    public Long rating;
    public String content;
    public LocalDate reviewDate;
    private List<String> imagePaths = new ArrayList<>();
    private List<MultipartFile> files = new ArrayList<>();
    public Long reviewId;

}
