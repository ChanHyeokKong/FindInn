package com.inn.controller;

import com.inn.config.CustomUserDetails;
import com.inn.data.member.manager.HotelWithManagerDto;
import com.inn.data.member.MemberDto;
import com.inn.service.AdminService;
import com.inn.service.ManagerService;
import com.inn.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
public class AdminController {

    @Autowired
    private AdminService adminService;

    @GetMapping("/admin/memberlist")
    public ModelAndView memberList(){
        ModelAndView mv = new ModelAndView("member/admin/memberList");
        List<MemberDto> list = adminService.getAllMember();
        mv.addObject("members",list);

        return mv;
    }

    @GetMapping("/admin/managerlist")
    public ModelAndView managerList(){
        ModelAndView mv = new ModelAndView("member/admin/managerList");
        List<MemberDto> list = adminService.getAllManager();
        mv.addObject("members",list);

        return mv;
    }

    @GetMapping("/admin/hotellist")
    public ModelAndView hotelList(){
        ModelAndView mv = new ModelAndView("member/admin/hotelList");
        List<HotelWithManagerDto> list = adminService.getHotelList();
        mv.addObject("hotels",list);

        return mv;
    }

    @GetMapping("/admin/qna")
    public String qnaManage(@AuthenticationPrincipal CustomUserDetails currentUser, Model model){
        return "member/admin/qna";
    }

}
