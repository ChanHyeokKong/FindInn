package com.inn.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.inn.data.hotel.*;
import com.inn.data.registerHotel.HotelRegistrationDto;
import com.inn.data.registerHotel.RoomRegistrationDto;
import com.inn.data.rooms.RoomTypes;
import com.inn.data.review.RatingDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

@Service
public class HotelService {
    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private FileStorageService fileStorageService;
  
    @Autowired
    private ReviewService reviewService;

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

    public List<HotelDto> searchHotelsWithConditions(String keyword, String category, List<String> tags, LocalDate checkIn, LocalDate checkOut, Long minPrice, Long personCount, String sort) {


        StringBuilder sql = new StringBuilder(
        		"SELECT h.idx, h.hotel_name, h.member_idx, h.hotel_tel, h.hotel_category, h.hotel_address, h.hotel_image ,rt.min_price FROM hotel h LEFT JOIN hotel_tags t ON h.idx = t.hotel_idx LEFT JOIN( SELECT hotel_id, MIN(price) AS min_price FROM room_types GROUP BY hotel_id) rt ON h.idx = rt.hotel_id WHERE 1=1"
        		);

        if (minPrice != null && minPrice < 500000) {
            sql.append(" AND rt.min_price IS NOT NULL AND rt.min_price <= :priceRange");
        }
        
        //인원수
        if (personCount != null && personCount > 0) {
            sql.append(" AND h.idx IN (");
            sql.append(" SELECT r.hotel_id");
            sql.append(" FROM rooms r");
            sql.append(" JOIN room_types rt2 ON r.room_type_id = rt2.idx");
            sql.append(" GROUP BY r.hotel_id");
            sql.append(" HAVING SUM(rt2.capacity) >= :personCount");
            sql.append(")");
        }
       
        

        if (keyword != null && !keyword.trim().isEmpty()) {
        	sql.append(" AND (h.hotel_name LIKE :keyword OR h.hotel_address LIKE :keyword)");
           
        }
        if (category != null && !category.trim().isEmpty() && !category.equalsIgnoreCase("all")) {
            sql.append(" AND h.hotel_category LIKE :category");
        }

        // SQL 인젝션 방지
        if (tags != null && !tags.isEmpty()) {
            // Example of a simple whitelist
            List<String> allowedTags = List.of(
                    "sauna", "swimming_pool", "restaurant", "fitness", "golf", "pc",
                    "kitchen", "washing_machine", "parking", "spa", "ski", "in_room_eating",
                    "breakfast", "smoking", "luggage", "disabled", "pickup","family","waterpool",
                    "view","beach","nicemeal","coupon","discount"
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

        // 날짜가 제공되면 공실 체크
        if (checkIn != null && checkOut != null) {
            sql.append(" AND EXISTS (SELECT 1 FROM rooms r WHERE r.hotel_id = h.idx AND NOT EXISTS (");
            sql.append(" SELECT 1 FROM booking b WHERE b.room_idx = r.idx");
            sql.append(" AND b.checkin < :checkOutDate AND b.checkout > :checkInDate))");
        }
        
        //sort 
        switch(sort) {
        case "Score": 
        	sql.append("");
        	break;
        	
        case "Review": 
        	sql.append("");
        	break;
        	
        case "lowPrice": 
        	sql.append(" order by rt.min_price asc");
        	break;
        	
        case "highPrice": 
        	sql.append(" order by rt.min_price desc");
        	break;
        	
        
        
        }
        
        

        // 5. Create the query and set parameters
        Query query = entityManager.createNativeQuery(sql.toString()); // Assuming result maps to Hotel entity
        if (minPrice != null && minPrice < 500000) {
            query.setParameter("priceRange", minPrice);
        }
        
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
        
        if (personCount != null && personCount > 0) {
            query.setParameter("personCount", personCount);
        }
        
        

        // The result needs to be mapped to HotelDto, which can be done after fetching
        List<Object[]> resultList = query.getResultList();
        System.out.println("🔍 Generated SQL: " + sql.toString());
        // ✅ DTO로 수동 매핑
        List<HotelDto> dtos = resultList.stream().map(row -> {
            HotelDto dto = new HotelDto();

            Long hotelId = ((Number) row[0]).longValue();
            dto.setIdx(hotelId);
            dto.setHotelName((String) row[1]);
            dto.setMemberIdx(((Number) row[2]).longValue());
            dto.setHotelTel((String) row[3]);
            dto.setHotelCategory((String) row[4]);
            dto.setHotelAddress((String) row[5]);
            dto.setHotelImage((String) row[6]); 
            dto.setPriceRange(row[7] != null ? ((Number) row[7]).intValue() : null);

            // ✅ 호텔 평점 세팅
            RatingDto rating = reviewService.getRatings(hotelId);
            dto.setRatingDto(rating);

            dto.setHotelTag(null);
            return dto;
        }).collect(Collectors.toList());

        return dtos;
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


    @Transactional
    public HotelEntity registerHotel(HotelRegistrationDto dto, Long mem_idx) {
        HotelEntity hotel = new HotelEntity();
        hotel.setHotelName(dto.getHotel_name());
        hotel.setHotelAddress(dto.getAddress());
        hotel.setDescription(dto.getDesc());
        hotel.setHotelCategory(dto.getCategory());
        hotel.setStatus(0L);
        hotel.setHotelTel(dto.getPhone_number());
        hotel.setMemberIdx(mem_idx);

        if (dto.getTag() != null && !dto.getTag().isEmpty()) {
            // 2. Create a new TagEntity instance
            TagEntity tagEntity = new TagEntity();

            // 3. Loop through the list of tag strings from the DTO
            for (String tagValue : dto.getTag()) {
                // 4. Use a switch to set the correct boolean field to true
                switch (tagValue) {
                    case "sauna": tagEntity.setSauna(true); break;
                    case "swimming_pool": tagEntity.setSwimming_pool(true); break;
                    case "restaurant": tagEntity.setRestaurant(true); break;
                    case "fitness": tagEntity.setFitness(true); break;
                    case "golf": tagEntity.setGolf(true); break;
                    case "pc": tagEntity.setPc(true); break;
                    case "kitchen": tagEntity.setKitchen(true); break;
                    case "washing_machine": tagEntity.setWashing_Machine(true); break;
                    case "parking": tagEntity.setParking(true); break;
                    case "spa": tagEntity.setSpa(true); break;
                    case "ski": tagEntity.setSki(true); break;
                    case "in_room_eating": tagEntity.setIn_Room_Eating(true); break;
                    case "breakfast": tagEntity.setBreakfast(true); break;
                    case "smoking": tagEntity.setSmoking(true); break;
                    case "luggage": tagEntity.setLuggage(true); break;
                    case "disabled": tagEntity.setDisabled(true); break;
                    case "pickup": tagEntity.setPickup(true); break;
                    // Tags from "#취향"
                    case "family": tagEntity.setFamily(true); break;
                    case "waterpool": tagEntity.setWaterpool(true); break;
                    case "view": tagEntity.setView(true); break;
                    case "beach": tagEntity.setBeach(true); break;
                    case "nicemeal": tagEntity.setNicemeal(true); break;
                }
            }

            // 5. Establish the bidirectional relationship
            hotel.setTag(tagEntity);
            tagEntity.setHotel(hotel);
        }

        if (dto.getImageFiles()!=null && !dto.getImageFiles().isEmpty()) {
            List<String> imageNames = dto.getImageFiles().stream()
                    .filter(file -> !file.isEmpty())
                    .map(file -> fileStorageService.store(file, "hotels"))
                    .collect(Collectors.toList());
            hotel.setHotelImage(imageNames.get(0));
        }

        if (dto.getRooms() != null) {
            for (RoomRegistrationDto roomDto : dto.getRooms()) {
                RoomTypes roomTypes = new RoomTypes();
                roomTypes.setTypeName(roomDto.getTypeName());
                roomTypes.setDescription(roomDto.getDescription());
                roomTypes.setPrice(roomDto.getPrice());
                roomTypes.setCapacity(roomDto.getMaxCapacity());
                hotel.addRoomType(roomTypes);
                if (roomDto.getImageFile()!=null && !roomDto.getImageFile().isEmpty()) {
                    String uniqueImageName = fileStorageService.store(roomDto.getImageFile(), "hotels");
                    roomTypes.setImageUrl(uniqueImageName);
                }
            }
        }
        return hotelRepository.save(hotel);
    }

    // 엔티티 to Dto 변환 메서드 (/h_list 용)
    public List<HotelDto> getAllHotelDtos() {
        return hotelRepository.findAll()
                .stream()
                .map(hotel -> {
                    HotelDto dto = new HotelDto(
                            hotel.getIdx(),
                            hotel.getHotelName(),
                            hotel.getHotelImages(),
                            hotel.getMemberIdx()
                    );
                    dto.setHotelTel(hotel.getHotelTel());
                    dto.setHotelCategory(hotel.getHotelCategory());
                    dto.setHotelAddress(hotel.getHotelAddress());
                    dto.setHotelImage(hotel.getHotelImage());
                    dto.setRatingDto(reviewService.getRatings(hotel.getIdx())); // ✅ 평점 세팅
                    return dto;
                })
                .collect(Collectors.toList());
    }
}