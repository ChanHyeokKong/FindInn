package com.inn.controller;


import com.inn.data.rooms.Rooms;
import com.inn.service.RoomsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDate;

@Controller
public class ReserveController {
    @Autowired
    private RoomsService roomsService;

    @GetMapping("/reserve")
    public String processReservation(@RequestParam long roomId, @RequestParam(value="checkIn", required = false) LocalDate checkIn, @RequestParam(value="checkOut", required = false) LocalDate checkOut, RedirectAttributes redirectAttributes,@RequestHeader("Referer") String refererUrl) {
        if (checkIn==null || checkOut==null) {
            redirectAttributes.addFlashAttribute("message", "예약날짜를 설정해주세요");
            return "redirect:"+refererUrl;
        }


        Rooms room = roomsService.getAvailableRoom(roomId, checkIn, checkOut);
        if (room ==null){
            redirectAttributes.addFlashAttribute("message", "해당 객실은 매진되었습니다.");
            return "redirect:"+refererUrl;
        }

        redirectAttributes.addAttribute("checkIn", checkIn.toString());
        redirectAttributes.addAttribute("checkOut", checkOut.toString());
        redirectAttributes.addAttribute("r_id", room.getIdx());


        return "redirect:/booking";
    }
}
