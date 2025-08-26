package com.inn.service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.inn.data.hotel.*;
import com.inn.data.registerHotel.*;
import com.inn.data.rooms.RoomTypes;
import com.inn.data.review.RatingDto;
import com.inn.data.rooms.Rooms;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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
    /*public List<HotelDto> searchHotels(HotelSearchCondition condition) {
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
    }*/

    public List<HotelDto> searchHotelsWithConditions(String keyword, String category, List<String> tags, LocalDate checkIn, LocalDate checkOut, Long minPrice, Long personCount, String sort) {


        StringBuilder sql = new StringBuilder(
               // "SELECT h.idx, h.hotel_name, h.member_idx, h.hotel_tel, h.hotel_category, h.hotel_address, h.hotel_image ,rt.min_price, rv.avg_rating,  rv.review_count FROM hotel h LEFT LEFT JOIN hotel_tags t ON h.idx = t.hotel_idx LEFT JOIN( SELECT hotel_id, MIN(price) AS min_price FROM room_types GROUP BY hotel_id) rt ON h.idx = rt.hotel_id WHERE 1=1"
        		"SELECT "
        	    + "    h.idx, "
        	    + "    h.hotel_name, "
        	    + "    h.member_idx, "
        	    + "    h.hotel_tel, "
        	    + "    h.hotel_category, "
        	    + "    h.hotel_address, "
        	    + "    h.hotel_image, "
        	    + "    rt.min_price, "
        	    + "    rv.avg_rating, "
        	    + "    rv.review_count "
        	    + "FROM "
        	    + "    hotel h "
        	    + "LEFT JOIN hotel_tags t ON h.idx = t.hotel_idx "
        	    + "LEFT JOIN "
        	    + "    ( "
        	    + "        SELECT "
        	    + "            hotel_id, "
        	    + "            MIN(price) AS min_price "
        	    + "        FROM "
        	    + "            room_types "
        	    + "        GROUP BY "
        	    + "            hotel_id "
        	    + "    ) AS rt ON h.idx = rt.hotel_id "
        	    + "LEFT JOIN "
        	    + "    ( "
        	    + "        SELECT "
        	    + "            hotel_id, "
        	    + "            AVG(rating) AS avg_rating, "
        	    + "            COUNT(rating) AS review_count "
        	    + "        FROM "
        	    + "            review "
        	    + "        GROUP BY "
        	    + "            hotel_id "
        	    + "    ) AS rv ON h.idx = rv.hotel_id "
        	    + "WHERE "
        	    + "    1=1 "
        	  
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
                    "breakfast", "smoking", "luggage", "disabled", "pickup", "family", "waterpool",
                    "view", "beach", "nicemeal", "coupon", "discount"
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
        switch (sort) {
            case "Score":
                sql.append(" order by rv.avg_rating desc");
                break;

            case "Review":
                sql.append(" order by rv.review_count desc");
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
            TagEntity tagEntity = new TagEntity();

            for (String tagValue : dto.getTag()) {
                switch (tagValue) {
                    case "sauna":
                        tagEntity.setSauna(true);
                        break;
                    case "swimming_pool":
                        tagEntity.setSwimming_pool(true);
                        break;
                    case "restaurant":
                        tagEntity.setRestaurant(true);
                        break;
                    case "fitness":
                        tagEntity.setFitness(true);
                        break;
                    case "golf":
                        tagEntity.setGolf(true);
                        break;
                    case "pc":
                        tagEntity.setPc(true);
                        break;
                    case "kitchen":
                        tagEntity.setKitchen(true);
                        break;
                    case "washing_machine":
                        tagEntity.setWashing_Machine(true);
                        break;
                    case "parking":
                        tagEntity.setParking(true);
                        break;
                    case "spa":
                        tagEntity.setSpa(true);
                        break;
                    case "ski":
                        tagEntity.setSki(true);
                        break;
                    case "in_room_eating":
                        tagEntity.setIn_Room_Eating(true);
                        break;
                    case "breakfast":
                        tagEntity.setBreakfast(true);
                        break;
                    case "smoking":
                        tagEntity.setSmoking(true);
                        break;
                    case "luggage":
                        tagEntity.setLuggage(true);
                        break;
                    case "disabled":
                        tagEntity.setDisabled(true);
                        break;
                    case "pickup":
                        tagEntity.setPickup(true);
                        break;
                    // Tags from "#취향"
                    case "family":
                        tagEntity.setFamily(true);
                        break;
                    case "waterpool":
                        tagEntity.setWaterpool(true);
                        break;
                    case "view":
                        tagEntity.setView(true);
                        break;
                    case "beach":
                        tagEntity.setBeach(true);
                        break;
                    case "nicemeal":
                        tagEntity.setNicemeal(true);
                        break;
                }
            }

            hotel.setTag(tagEntity);
            tagEntity.setHotel(hotel);
        }

        if (dto.getImageFiles() != null && !dto.getImageFiles().isEmpty()) {
            List<String> imageNames = dto.getImageFiles().stream()
                    .filter(file -> !file.isEmpty())
                    .map(file -> fileStorageService.store(file, "hotels"))
                    .collect(Collectors.toList());
            hotel.setHotelImage(imageNames.get(0));
        }
        int roomnum = 1;
        int floornum = 2;
        if (dto.getRooms() != null) {
            for (RoomRegistrationDto roomDto : dto.getRooms()) {
                RoomTypes roomTypes = new RoomTypes();
                roomTypes.setTypeName(roomDto.getTypeName());
                roomTypes.setDescription(roomDto.getDescription());
                roomTypes.setPrice(roomDto.getPrice());
                roomTypes.setCapacity(roomDto.getMaxCapacity());

                for (int i = 0; i < 5; i++) {
                    Rooms rooms = new Rooms();
                    rooms.setRoomNumber(roomnum + floornum * 100L);
                    rooms.setRoomType(roomTypes);
                    rooms.setHotel(hotel);
                    roomTypes.addRoom(rooms);
                    roomnum++;
                    if (roomnum > 10) {
                        roomnum = 1;
                        floornum++;
                    }
                }
                hotel.addRoomType(roomTypes);
                if (roomDto.getImageFile() != null && !roomDto.getImageFile().isEmpty()) {
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

                    // 최저 가격 조회 추가
                    String priceQuery = "SELECT MIN(price) FROM room_types WHERE hotel_id = ?1";
                    Query query = entityManager.createNativeQuery(priceQuery);
                    query.setParameter(1, hotel.getIdx());
                    Object priceResult = query.getSingleResult();
                    dto.setPriceRange(priceResult != null ? ((Number) priceResult).intValue() : null);

                    dto.setRatingDto(reviewService.getRatings(hotel.getIdx())); // ✅ 평점 세팅
                    return dto;
                })
                .collect(Collectors.toList());
    }




    public HotelEditDto mapEntityToDto(HotelEntity entity) {
        HotelEditDto dto = new HotelEditDto();
        dto.setIdx(entity.getIdx());
        dto.setHotelName(entity.getHotelName());
        dto.setHotelAddress(entity.getHotelAddress());
        dto.setHotelTel(entity.getHotelTel());
        dto.setHotelCategory(entity.getHotelCategory());
        dto.setDescription(entity.getDescription());
        dto.setHotelImage(entity.getHotelImage()); // URL of the main image
        dto.setHotelImages(entity.getHotelImages()); // URLs of gallery images

        // Map associated tags
        if (entity.getTag() != null) {
            dto.setTags(mapTagEntityToDto(entity.getTag()));
        }

        // Map associated room types
        dto.setRoomTypes(entity.getRoomTypes().stream()
                .map(this::mapRoomTypeEntityToDto)
                .collect(Collectors.toList()));

        return dto;
    }

    private TagEditDto mapTagEntityToDto(TagEntity tagEntity) {
        TagEditDto tagDto = new TagEditDto();
        tagDto.setSauna(tagEntity.isSauna());
        tagDto.setSwimming_pool(tagEntity.isSwimming_pool());
        tagDto.setRestaurant(tagEntity.isRestaurant());
        tagDto.setFitness(tagEntity.isFitness());
        tagDto.setGolf(tagEntity.isGolf());
        tagDto.setPc(tagEntity.isPc());
        tagDto.setKitchen(tagEntity.isKitchen());
        tagDto.setWashing_Machine(tagEntity.isWashing_Machine());
        tagDto.setParking(tagEntity.isParking());
        tagDto.setSpa(tagEntity.isSpa());
        tagDto.setSki(tagEntity.isSki());
        tagDto.setIn_Room_Eating(tagEntity.isIn_Room_Eating());
        tagDto.setBreakfast(tagEntity.isBreakfast());
        tagDto.setSmoking(tagEntity.isSmoking());
        tagDto.setLuggage(tagEntity.isLuggage());
        tagDto.setDisabled(tagEntity.isDisabled());
        tagDto.setPickup(tagEntity.isPickup());
        tagDto.setFamily(tagEntity.isFamily());
        tagDto.setWaterpool(tagEntity.isWaterpool());
        tagDto.setView(tagEntity.isView());
        tagDto.setBeach(tagEntity.isBeach());
        tagDto.setNicemeal(tagEntity.isNicemeal());
        tagDto.setCoupon(tagEntity.isCoupon());
        tagDto.setDiscount(tagEntity.isDiscount());
        return tagDto;
    }

    private RoomTypeEditDto mapRoomTypeEntityToDto(RoomTypes roomType) {
        RoomTypeEditDto roomDto = new RoomTypeEditDto();
        roomDto.setIdx(roomType.getIdx());
        roomDto.setTypeName(roomType.getTypeName());
        roomDto.setDescription(roomType.getDescription());
        roomDto.setPrice(roomType.getPrice());
        roomDto.setCapacity(roomType.getCapacity());
        roomDto.setImageUrl(roomType.getImageUrl());
        return roomDto;
    }

    public void updateEntityFromDto(HotelEntity entity, HotelEditDto dto) throws IOException {
        entity.setHotelName(dto.getHotelName());
        entity.setHotelAddress(dto.getHotelAddress());
        entity.setHotelTel(dto.getHotelTel());
        entity.setHotelCategory(dto.getHotelCategory());
        entity.setDescription(dto.getDescription());

        MultipartFile mainImageFile = dto.getNewHotelImageFile();
        if (mainImageFile != null && !mainImageFile.isEmpty()) {
            String oldImageUrl = entity.getHotelImage();
            String newImageUrl = fileStorageService.store(mainImageFile, "hotels");
            entity.setHotelImage(newImageUrl);
            if (oldImageUrl != null && !oldImageUrl.isEmpty()) {
                fileStorageService.delete(oldImageUrl, "hotels");
            }
        }

        updateTagEntityFromDto(entity.getTag(), dto.getTags());

        Map<Long, RoomTypes> existingRoomsMap = entity.getRoomTypes()
                .stream()
                .collect(Collectors.toMap(RoomTypes::getIdx, room -> room));

        List<RoomTypes> updatedRooms = new ArrayList<>();

        if (dto.getRoomTypes() != null) {
            for (RoomTypeEditDto roomDto : dto.getRoomTypes()) {
                RoomTypes roomType;
                if (roomDto.getIdx() != null) {
                    roomType = existingRoomsMap.get(roomDto.getIdx());
                    if (roomType == null) continue;
                    existingRoomsMap.remove(roomDto.getIdx());
                } else {
                    roomType = new RoomTypes();
                    roomType.setHotel(entity);
                }

                roomType.setTypeName(roomDto.getTypeName());
                roomType.setDescription(roomDto.getDescription());
                roomType.setPrice(roomDto.getPrice());
                roomType.setCapacity(roomDto.getCapacity());

                MultipartFile roomImageFile = roomDto.getImageFile();
                if (roomImageFile != null && !roomImageFile.isEmpty()) {
                    String oldRoomImageUrl = roomType.getImageUrl();

                    String newRoomImageUrl = fileStorageService.store(roomImageFile, "hotels");
                    roomType.setImageUrl(newRoomImageUrl);

                    if (oldRoomImageUrl != null && !oldRoomImageUrl.isEmpty()) {
                        fileStorageService.delete(oldRoomImageUrl, "hotels");
                    }
                }

                updatedRooms.add(roomType);
            }
        }

        for (RoomTypes roomToDelete : existingRoomsMap.values()) {
            String imageToDelete = roomToDelete.getImageUrl();
            if (imageToDelete != null && !imageToDelete.isEmpty()) {
                fileStorageService.delete(imageToDelete, "hotels");
            }
        }

        entity.getRoomTypes().clear();
        entity.getRoomTypes().addAll(updatedRooms);
        hotelRepository.save(entity);
    }

    private void updateTagEntityFromDto(TagEntity tagEntity, TagEditDto tagDto) {
        if (tagEntity == null || tagDto == null) return;

        tagEntity.setSauna(tagDto.isSauna());
        tagEntity.setSwimming_pool(tagDto.isSwimming_pool());
        tagEntity.setRestaurant(tagDto.isRestaurant());
        tagEntity.setFitness(tagDto.isFitness());
        tagEntity.setGolf(tagDto.isGolf());
        tagEntity.setPc(tagDto.isPc());
        tagEntity.setKitchen(tagDto.isKitchen());
        tagEntity.setWashing_Machine(tagDto.isWashing_Machine());
        tagEntity.setParking(tagDto.isParking());
        tagEntity.setSpa(tagDto.isSpa());
        tagEntity.setSki(tagDto.isSki());
        tagEntity.setIn_Room_Eating(tagDto.isIn_Room_Eating());
        tagEntity.setBreakfast(tagDto.isBreakfast());
        tagEntity.setSmoking(tagDto.isSmoking());
        tagEntity.setLuggage(tagDto.isLuggage());
        tagEntity.setDisabled(tagDto.isDisabled());
        tagEntity.setPickup(tagDto.isPickup());
        tagEntity.setFamily(tagDto.isFamily());
        tagEntity.setWaterpool(tagDto.isWaterpool());
        tagEntity.setView(tagDto.isView());
        tagEntity.setBeach(tagDto.isBeach());
        tagEntity.setNicemeal(tagDto.isNicemeal());
        tagEntity.setCoupon(tagDto.isCoupon());
        tagEntity.setDiscount(tagDto.isDiscount());
    }

    public void deleteHotel(Long id){
        HotelEntity hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid hotel Id:" + id));
        for (RoomTypes room : hotel.getRoomTypes()) {
            fileStorageService.delete(room.getImageUrl(), "hotels");
        }
        fileStorageService.delete(hotel.getHotelImage(), "hotels");
        hotelRepository.delete(hotel);
    }


}


