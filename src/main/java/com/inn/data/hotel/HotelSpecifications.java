package com.inn.data.hotel;

import java.util.List;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

public class HotelSpecifications {

    public static Specification<HotelEntity> keywordContains(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return (root, query, cb) -> cb.conjunction();
        }
        return (root, query, cb) -> cb.like(root.get("hotelName"), "%" + keyword + "%");
    }

    public static Specification<HotelEntity> categoryEquals(String category) {
        return (root, query, cb) -> {
            if (category == null || category.equalsIgnoreCase("all")) {
                return cb.conjunction();
            }
            return cb.equal(root.get("hotelCategory"), category);
        };
    }

    public static Specification<HotelEntity> hasAllTags(List<String> tags) {
        return (root, query, cb) -> {
            if (tags == null || tags.isEmpty()) {
                return cb.conjunction();
            }

            // Subquery를 사용하여 태그 조건을 처리
            Subquery<Long> subquery = query.subquery(Long.class);
            // @ElementCollection 테이블을 직접 참조
            Root<HotelEntity> subRoot = subquery.from(HotelEntity.class);
            Join<HotelEntity, String> tagJoin = subRoot.join("hotelTag");

            subquery.select(subRoot.get("idx"))
            	.where(cb.in(tagJoin).in(tags))
                    .groupBy(subRoot.get("idx"))
                    .having(cb.equal(cb.count(tagJoin), (long) tags.size()));

            return cb.in(root.get("idx")).value(subquery);
        };
    }
}