package com.inn.data.review;

import lombok.Data;

@Data
public class RatingDto {
    Double rating_avg;
    Long rating_count;

    public RatingDto(Double averageRating, Long reviewCount) {
        this.rating_avg= (averageRating == null) ? 0.0 : averageRating;
        this.rating_count = reviewCount;
    }
}
