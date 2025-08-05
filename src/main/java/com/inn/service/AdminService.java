package com.inn.service;

import com.inn.data.hotel.HotelWithManagerDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminService {

    @Autowired
    MemberService memberService;
    @Autowired
    HotelService hotelService;

    public List<HotelWithManagerDto> getHotelList(){
        return hotelService.GetAllForAdmin();
    }
}
