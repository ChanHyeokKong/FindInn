package com.inn.controller;

import com.inn.data.hotel.HotelWithManagerDto;
import com.inn.data.member.MemberDto;
import com.inn.service.AdminService;
import com.inn.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
public class AdminController {

    @Autowired
    private MemberService memberService;
    @Autowired
    private AdminService adminService;

    @GetMapping("/member/list")
    public ModelAndView memberList(){
        ModelAndView mv = new ModelAndView("member/admin/memberList");
        List<MemberDto> list = memberService.getAllMember();
        mv.addObject("members",list);

        return mv;
    }

    @GetMapping("/hotel/list")
    public ModelAndView hotelList(){
        ModelAndView mv = new ModelAndView("member/admin/hotelList");
        List<HotelWithManagerDto> list = adminService.getHotelList();
        mv.addObject("hotels",list);

        return mv;
    }

}
