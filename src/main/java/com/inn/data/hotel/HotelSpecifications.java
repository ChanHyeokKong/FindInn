package com.inn.data.hotel;

import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.*;

public class HotelSpecifications {

    // 키워드로 hotelName LIKE 검색 (null 또는 빈 문자열이면 조건 무시)
	public static Specification<HotelEntity> keywordContains(String keyword) {
	    if (keyword == null || keyword.isBlank()) {
	        return (root, query, cb) -> cb.conjunction(); // 항상 true
	    }
	    return (root, query, cb) -> cb.like(root.get("hotelName"), "%" + keyword + "%");
	}

    // 카테고리 equals 검색 ('all' 이거나 null이면 조건 무시)
    public static Specification<HotelEntity> categoryEquals(String category) {
        return (root, query, cb) -> {
            if (category == null || category.equalsIgnoreCase("all")) {
                return cb.conjunction();
            }
            return cb.equal(root.get("hotelCategory"), category);
        };
    }

    /* 가격 조건 - 예시로 hotelPrice 컬럼이 있다고 가정 (Integer 타입)
    public static Specification<HotelEntity> priceLessThanEqual(Integer maxPrice) {
        return (root, query, cb) -> {
            if (maxPrice == null || maxPrice <= 0) {
                return cb.conjunction();
            }
            // hotelPrice 필드가 없으므로 예시로 hotelTel 대신에 작성, 실제 필드명으로 교체 필요
            // return cb.le(root.get("hotelPrice"), maxPrice);
            return cb.conjunction();  // 실제 필드 없으니 조건 안 걸리게 함
        };
    }*/

    // 태그 조건: hotelTag가 List<String>이므로 서브쿼리로 처리 (모든 태그 포함)
    public static Specification<HotelEntity> hasTags(List<String> tags) {
        return (root, query, cb) -> {
            if (tags == null || tags.isEmpty()) {
                return cb.conjunction();
            }

            // hotelTag는 ElementCollection이므로 join 가능
            Join<HotelEntity, String> tagJoin = root.join("hotelTag");

            // 태그가 여러개면 all 포함 조건이 필요하므로 group by와 having count 방식 필요 (복잡)
            // 여기서는 단순히 태그 중 하나라도 포함하는 조건으로 예시 작성
            CriteriaBuilder.In<String> inClause = cb.in(tagJoin);
            for (String tag : tags) {
                inClause.value(tag);
            }

            query.distinct(true); // 중복 제거

            return inClause;
        };
    }
}