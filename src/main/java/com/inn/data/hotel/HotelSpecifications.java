package com.inn.data.hotel;

import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.*;

public class HotelSpecifications {

    // 키워드 포함 조건
    public static Specification<HotelEntity> keywordContains(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return (root, query, cb) -> cb.conjunction(); // 아무 조건 없음
        }
        return (root, query, cb) ->
            cb.like(cb.lower(root.get("hotelName")), "%" + keyword.toLowerCase() + "%");
    }

    // 카테고리 일치 조건
    public static Specification<HotelEntity> categoryEquals(String category) {
        return (root, query, cb) -> {
            if (category == null || category.equalsIgnoreCase("all")) {
                return cb.conjunction(); // 조건 없음
            }
            return cb.equal(root.get("hotelCategory"), category);
        };
    }

    // ⚠️ 태그 조건은 이제 여기서 다루지 않음!
}