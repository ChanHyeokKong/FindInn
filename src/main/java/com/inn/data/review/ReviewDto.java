package com.inn.data.review;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ReviewDto {
    public String hotelName;
    public String hotelImages;
    public String roomType;
    public Long rating;
    public String content;
    public LocalDate reviewDate;
}
