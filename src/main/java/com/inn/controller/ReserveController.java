package com.inn.controller;

import com.inn.reserve.ReserveForm;
import com.inn.rooms.*;
import com.inn.reserve.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Controller
public class ReserveController {
    @Autowired
    private RoomTypesRepository roomTypesRepository;

    @Autowired
    private RoomsRepository roomsRepository;

    @Autowired
    private ReserveRepository reserveRepository;

    @GetMapping("/reserve") // 1. Path is now just /reserve
    public String reserve(@RequestParam("hotelId") Long hotelId, // 2. Annotation changed to @RequestParam
                          @RequestParam(value = "checkIn", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInDate,
                          @RequestParam(value = "checkOut", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOutDate,
                          @RequestParam(value = "personal", required = false) int personal,
                          Model model) {




        ReserveForm reservation = new ReserveForm();
        reservation.setHotelId(hotelId);

        if (checkInDate != null) {
            reservation.setCheckIn(checkInDate);
        }
        if (checkOutDate != null) {
            reservation.setCheckOut(checkOutDate);
        }
        reservation.setPerson(personal);

        model.addAttribute("reservation", reservation);

        List<RoomTypes> roomTypes = roomTypesRepository.findAvailableRoomType(checkInDate,checkOutDate,personal,hotelId);
        System.out.println("Found " + roomTypes.size() + " room types.");

        model.addAttribute("roomTypes", roomTypes);
        model.addAttribute("hotelId", hotelId);

        return "reserve/reserve";
    }

    @PostMapping("/reserve")
    public String processReservation(@ModelAttribute("reservation") ReserveForm reservation, RedirectAttributes redirectAttributes) {
        Random random = new Random();
        Reserve reserve = new Reserve();

        List<Rooms> rooms = roomsRepository.findAvailableRoomNo(reservation.getRoomTypeId(),reservation.getCheckIn(),reservation.getCheckOut());
        System.out.println("Available room numbers: " + rooms);
        if (rooms.isEmpty()) {
            System.out.println("The list of available rooms is empty.");
        } else {
            int randomIndex = random.nextInt(rooms.size());
            Rooms randomRoom = rooms.get(randomIndex);
            reserve.setRoom(randomRoom);
            System.out.println("A randomly selected room is: " + randomRoom);
        }

        RoomTypes selectedRoomType = roomTypesRepository.findById(reservation.getRoomTypeId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid Room Type ID: " + reservation.getRoomTypeId()));

        reserve.setReserveHotelId(reservation.getHotelId());
        reserve.setReserveUserId(reservation.getUserId());
        reserve.setCheckIn(reservation.getCheckIn());
        reserve.setCheckOut(reservation.getCheckOut());
        reserve.setPaymentDate(LocalDateTime.now());
        reserve.setMethod(reservation.getMethod());
        reserve.setPrice(reservation.getPrice());
        reserve.setRoomType(selectedRoomType);
        reserveRepository.save(reserve);

        redirectAttributes.addAttribute("hotelId", reservation.getHotelId());
        return "redirect:/reserve/{hotelId}";
    }
}
