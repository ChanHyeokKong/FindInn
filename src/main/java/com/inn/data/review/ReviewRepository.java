package com.inn.data.review;

import com.inn.data.booking.BookingEntity;
import com.inn.data.hotel.HotelEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    Optional<Review> findByBooking(BookingEntity booking);
    Page<Review> findByHotel(HotelEntity hotel, Pageable pageable);

}
