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

    // 통합 검색: 키워드 + 카테고리 + 태그 포함 (가격 제외)
    public List<HotelDto> searchHotels(String keyword, String category, List<String> tags) {
        List<Long> tagFilteredHotelIds = null;

        if (tags != null && !tags.isEmpty()) {
            tagFilteredHotelIds = hotelRepository.findHotelIdxByAllTags(tags, tags.size());

            // 태그에 해당되는 호텔이 하나도 없으면 바로 빈 리스트 반환
            if (tagFilteredHotelIds.isEmpty()) {
                return List.of(); // 빈 리스트
            }
        }

        final List<Long> finalTagFilteredHotelIds = tagFilteredHotelIds;  // final 변수로 복사

        
        // Specification 조립
        Specification<HotelEntity> spec = Specification
                .where(HotelSpecifications.keywordContains(keyword))
                .and(HotelSpecifications.categoryEquals(category));

        // 태그 조건 추가
        if (tagFilteredHotelIds != null) {
            spec = spec.and((root, query, cb) -> root.get("idx").in(finalTagFilteredHotelIds));
        }

        // 검색 실행
        List<HotelEntity> result = hotelRepository.findAll(spec);

        return result.stream()
                .map(hotel -> new HotelDto(
                        hotel.getIdx(),
                        hotel.getHotelName(),
                        hotel.getHotelImages(),
                        hotel.getMemberIdx()))
                .collect(Collectors.toList());
    }

    // 태그로 hotel_idx 리스트 뽑기 (native query)
    public List<Long> findByHotelTagIn(List<String> tags, int tagCount) {
        return hotelRepository.findHotelIdxByAllTags(tags, tagCount);
    }
}