package com.inn.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Controller
public class BookingController {

    @GetMapping("/booking")
    public String bookingPage(Model model, @RequestParam LocalDate checkin, @RequestParam LocalDate checkout, @RequestParam int r_price) {



        // 몇 박인지 계산하여 총 가격 산출
        long nights = checkin.toEpochDay() - checkout.toEpochDay();
        int total_price = r_price * (int)nights;

        //객실정보
        model.addAttribute("r_idx", 101);
        model.addAttribute("h_name", "서울 프리미엄 호텔");
        model.addAttribute("r_name", "디럭스 더블룸 101호");
        model.addAttribute("b_checkin", checkin);
        model.addAttribute("b_checkout", checkout);

        //결제정보
        model.addAttribute("r_price", r_price);
        model.addAttribute("nights", nights);
        model.addAttribute("total_price", total_price);

        return "booking/bookingPage";
    }
}
