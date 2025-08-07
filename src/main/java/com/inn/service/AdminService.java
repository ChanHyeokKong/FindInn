package com.inn.service;

import com.inn.data.member.manager.HotelWithManagerDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminService {

    @Autowired
    ManagerService managerService;

    public List<HotelWithManagerDto> getHotelList(){
        return managerService.GetAllForAdmin();
    }
}
