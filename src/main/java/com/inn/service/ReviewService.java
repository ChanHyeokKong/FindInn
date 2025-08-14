package com.inn.service;

import com.inn.data.booking.BookingEntity;
import com.inn.data.booking.BookingRepository;
import com.inn.data.review.ReviewDto;
import com.inn.data.rooms.Rooms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class ReviewService {

    @Autowired
    public BookingService bookingService;

    @Autowired
    public RoomsService roomsService;

    @Autowired
    public BookingRepository bookingRepository;

    public ReviewDto getOrderData(Long orderId){
        Optional<BookingEntity> data = bookingRepository.findById(orderId);
        ReviewDto dto = new ReviewDto();
        if(data.isPresent()){
            dto.setReviewDate(LocalDate.now());
            RoomsService.RoomHotelDetailsDto rhd = roomsService.getHotelNamebyRoomTypeId(data.get().getRoomIdx());
            dto.setHotelName(rhd.hotelName());
            dto.setRoomType(rhd.roomTypeName());

        }
        else{
            return null;
        }
        return null;
    }

}
