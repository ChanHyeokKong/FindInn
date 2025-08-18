package com.inn.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.inn.data.hotel.HotelDto;
import com.inn.data.hotel.HotelEntity;
import com.inn.data.hotel.HotelRepository;
import com.inn.data.hotel.HotelSearchCondition;
import com.inn.data.hotel.HotelSpecifications;

@Service
public class HotelService {
    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private HotelRepository hotelRepository;

    // 전체 조회
    public List<HotelEntity> getAllHotelData() {
        return hotelRepository.findAll();        
    }

    // 호텔 ID로 조회
    public Optional<HotelEntity> getHotelDataById(long idx) {
        return hotelRepository.findById(idx);
    }

    // 기존 키워드 + 카테고리 검색
    public List<HotelDto> getHotelDataByKeywordAndCategory(String keyword, String category) {
        List<HotelEntity> hotels;

        if ("all".equals(category)) {
            hotels = hotelRepository.findByHotelNameContaining(keyword);
        } else {
            hotels = hotelRepository.findByHotelNameContainingAndHotelCategory(keyword, category);
        }

        return hotels.stream()
                .map(hotel -> new HotelDto(
                        hotel.getIdx(),
                        hotel.getHotelName(),
                        hotel.getHotelImages(),
                        hotel.getMemberIdx()))
                .collect(Collectors.toList());
    }


    // ⚠️ 수정된 searchHotels 메서드
    public List<HotelDto> searchHotels(HotelSearchCondition condition) {
        String keyword = condition.getKeyword();
        String category = condition.getCategory();
        List<String> tags = condition.getTags();

        Specification<HotelEntity> spec = Specification
            .where(HotelSpecifications.keywordContains(keyword))
            .and(HotelSpecifications.categoryEquals(category));

        // 태그 조건 따로 처리
        if (tags != null && !tags.isEmpty()) {
            List<Long> idxList = hotelRepository.findHotelIdxByAllTags(tags, tags.size());
            if (idxList.isEmpty()) return List.of();
            spec = spec.and((root, query, cb) -> root.get("idx").in(idxList));
        }

        List<HotelEntity> result = hotelRepository.findAll(spec);

        return result.stream()
                .map(hotel -> new HotelDto(
                        hotel.getIdx(),
                        hotel.getHotelName(),
                        hotel.getHotelImages(),
                        hotel.getMemberIdx()))
                .collect(Collectors.toList());
    }


    public List<HotelDto> searchHotelsWithConditions(String keyword, String category, List<String> tags, LocalDate checkIn, LocalDate checkOut, long safePrice) {

        // 1. Start with the base of the query
        StringBuilder sql = new StringBuilder(
                "SELECT h.* FROM hotel h LEFT JOIN hotel_tags t ON h.idx = t.hotel_idx WHERE 1=1"
        );

        // 2. Add keyword and category conditions if they exist
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND h.hotel_name LIKE :keyword");
        }
        if (category != null && !category.trim().isEmpty() && !category.equalsIgnoreCase("all")) {
            sql.append(" AND h.hotel_category LIKE :category");
        }

        // 3. Add tag conditions dynamically
        // IMPORTANT: This part is vulnerable to SQL injection.
        // You MUST validate the tag names against a whitelist of allowed column names.
        if (tags != null && !tags.isEmpty()) {
            // Example of a simple whitelist
            List<String> allowedTags = List.of(
                    "sauna", "swimming_pool", "restaurant", "fitness", "golf", "pc",
                    "kitchen", "washing_machine", "parking", "spa", "ski", "in_room_eating",
                    "breakfast", "smoking", "luggage", "disabled", "pickup"
            );
            for (String tag : tags) {
                if (allowedTags.contains(tag)) {
                    // Append validated tag name directly into the query string
                    sql.append(" AND t.").append(tag).append(" = 1");
                } else {
                    // Handle invalid tag name - throw exception or log a warning
                    throw new IllegalArgumentException("Invalid tag provided: " + tag);
                }
            }
        }

        // 4. Add room availability condition if dates are provided
        if (checkIn != null && checkOut != null) {
            sql.append(" AND EXISTS (SELECT 1 FROM rooms r WHERE r.hotel_id = h.idx AND NOT EXISTS (");
            sql.append(" SELECT 1 FROM reserve res WHERE res.room_id = r.room_id");
            sql.append(" AND res.check_in < :checkOutDate AND res.check_out > :checkInDate))");
        }

        // 5. Create the query and set parameters
        Query query = entityManager.createNativeQuery(sql.toString(), HotelEntity.class); // Assuming result maps to Hotel entity

        if (keyword != null && !keyword.trim().isEmpty()) {
            query.setParameter("keyword", "%" + keyword + "%");
        }
        if (category != null && !category.trim().isEmpty() && !category.equalsIgnoreCase("all")) {
            query.setParameter("category", "%" + category + "%");
        }
        if (checkIn != null && checkOut != null) {
            query.setParameter("checkInDate", checkIn);
            query.setParameter("checkOutDate", checkOut);
        }

        // The result needs to be mapped to HotelDto, which can be done after fetching
        List<HotelEntity> hotels = query.getResultList();
        return hotels.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    private HotelDto convertToDto(HotelEntity hotelEntity) {
        HotelDto dto = new HotelDto();

        // 2. Map all corresponding fields from the Entity to the DTO.
        dto.setIdx(hotelEntity.getIdx());
        dto.setMemberIdx(hotelEntity.getMemberIdx());
        dto.setHotelName(hotelEntity.getHotelName());
        dto.setHotelImages(hotelEntity.getHotelImages());
        dto.setHotelAddress(hotelEntity.getHotelAddress());
        dto.setHotelTel(hotelEntity.getHotelTel());
        dto.setHotelCategory(hotelEntity.getHotelCategory());
        dto.setHotelTag(hotelEntity.getHotelTag());

        return dto;
    }


    public String getHotelDescription(Long hotelId) {
        Optional<HotelEntity> hotelOptional = hotelRepository.findById(hotelId);
        if (hotelOptional.isPresent()) {
            System.out.println(hotelOptional.get().getDescription());
            return hotelOptional.get().getDescription();
        } else {
            return "";
        }
    }


}