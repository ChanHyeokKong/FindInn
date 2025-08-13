package com.inn.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
    // ⚠️ 기존 findByHotelTagIn 메서드는 이제 사용되지 않으므로 삭제하거나 주석 처리하는 것이 좋습니다.
    // public List<Long> findByHotelTagIn(List<String> tags, int tagCount) {
    //     return hotelRepository.findHotelIdxByAllTags(tags, tagCount);
    // }
}