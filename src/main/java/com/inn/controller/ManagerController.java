package com.inn.controller;

import com.inn.config.CustomUserDetails;
import com.inn.data.hotel.HotelEntity;
import com.inn.data.hotel.HotelRepository;
import com.inn.data.member.manager.HotelRoomTypeSummaryDto;
import com.inn.data.member.manager.HotelWithManagerDto;
import com.inn.service.ManagerService;
import com.inn.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
public class ManagerController {

    @Autowired
    ManagerService service;

    @Autowired
    HotelRepository hotelRepository;

    @Autowired
    MemberService memberService;

    @GetMapping("manage/room/detail")
    public String roomDetail(Long idx){



        return "member/manager/roomdetail";
    }

    @GetMapping("manage/hotel")
    public String room(@AuthenticationPrincipal CustomUserDetails currentUser, Model model){
        if(currentUser==null){
            return "redirect:/?login=false";
        }
        Long currentMemberIdx = currentUser.getMemeberIdx();
        List<HotelRoomTypeSummaryDto> list = service.GetAllRoomGroupByDescription(currentMemberIdx);
        model.addAttribute("hotelRoomInfos", list);

        return "member/manager/hotelmanage";
    }

    @GetMapping("manage/room")
    public String hotel(@AuthenticationPrincipal CustomUserDetails currentUser, Model model){
        if(currentUser==null){
            return "redirect:/?login=false";
        }
        Long currentMemberIdx = currentUser.getMemeberIdx();
        List<HotelWithManagerDto> list = service.getAllMyHotel(currentMemberIdx);
        model.addAttribute("hotelRoomInfos", list);

        return "member/manager/roommanage";
    }

    @GetMapping("manage/application")
    public ModelAndView application(@AuthenticationPrincipal CustomUserDetails currentUser){
        ModelAndView mv = new ModelAndView("member/manager/application");
        mv.addObject("currentUser", currentUser);
        return mv;
    }

    @PostMapping("manage/apply")
    public String apply(@AuthenticationPrincipal CustomUserDetails currentUser, HotelEntity entity){
        Integer memberIdx = currentUser.getMemeberIdx().intValue();
        entity.setMemberIdx(memberIdx);
        System.out.println(entity.toString());

        hotelRepository.save(entity);
        memberService.giveManagerRole(currentUser.getMemeberIdx());

        return "redirect:/?login=true";
    }

}
