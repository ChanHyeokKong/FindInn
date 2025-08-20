package com.inn.data.review;

import com.inn.data.booking.BookingEntity;
import com.inn.data.hotel.HotelEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    Optional<Review> findByBooking(BookingEntity booking);
    Page<Review> findByHotel(HotelEntity hotel, Pageable pageable);
    @Query("SELECT new com.inn.data.review.RatingDto(AVG(r.rating), COUNT(r)) FROM Review r WHERE r.hotel.idx = :hotelId")
    RatingDto findReviewStatsByHotelId(@Param("hotelId") Long hotelId);
}
