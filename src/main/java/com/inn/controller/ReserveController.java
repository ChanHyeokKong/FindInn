package com.inn.controller;

import com.inn.reserve.ReserveForm;
import com.inn.rooms.*;
import com.inn.reserve.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
public class ReserveController {
    @Autowired
    private RoomTypesRepository roomTypesRepository;

    @Autowired
    private ReserveRepository reserveRepository;

    @GetMapping("/reserve/{hotelId}")
    public String reserve(@PathVariable Long hotelId, Model model) {

        ReserveForm reservation = new ReserveForm();
        reservation.setHotelId(hotelId);
        model.addAttribute("reservation", reservation);
        List<RoomTypes> roomTypes = roomTypesRepository.findByHotelId(hotelId);
        model.addAttribute("roomTypes", roomTypes);
        model.addAttribute("hotelId", hotelId);
        return "reserve/reserve";
    }
    @PostMapping("/reserve")
    public String processReservation(@ModelAttribute("reservation") ReserveForm reservation, RedirectAttributes redirectAttributes) {
        Reserve reserve = new Reserve();
        reserve.setReserveHotelId(reservation.getHotelId());
        reserve.setReserveUserId(reservation.getUserId());
        reserve.setCheckIn(reservation.getCheckIn());
        reserve.setCheckOut(reservation.getCheckOut());
        reserve.setPaymentDate(LocalDateTime.now());
        reserve.setMethod(reservation.getMethod());
        reserve.setPrice(reservation.getPrice());
        reserveRepository.save(reserve);

        redirectAttributes.addAttribute("hotelId", reservation.getHotelId());
        return "redirect:/reserve/{hotelId}";
    }
}
