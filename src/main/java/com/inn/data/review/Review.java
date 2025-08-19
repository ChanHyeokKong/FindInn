package com.inn.data.review;

import com.inn.data.booking.BookingEntity;
import com.inn.data.hotel.HotelEntity;
import com.inn.data.member.MemberDto;
import com.inn.data.rooms.RoomTypes;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long idx;

    @ManyToOne
    @JoinColumn(name = "member_id", referencedColumnName = "idx")
    MemberDto member;

    @ManyToOne
    @JoinColumn(name="hotel_id", referencedColumnName = "idx")
    HotelEntity hotel;

    @ManyToOne
    @JoinColumn(name="roomtype_id", referencedColumnName = "idx")
    RoomTypes roomType;

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReviewFile> reviewFiles = new ArrayList<>();

    // 파일 추가하는 메서드
    public void addReviewFile(ReviewFile reviewFile) {
        this.reviewFiles.add(reviewFile);
        reviewFile.setReview(this);
    }

    @OneToOne
    @JoinColumn(name="bookingId",referencedColumnName = "idx")
    BookingEntity booking;

    LocalDateTime reviewDate;
    String content;
    @Column(nullable = false)
    Long rating;
}
