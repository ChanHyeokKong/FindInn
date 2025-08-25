package com.inn.controller;

import com.inn.config.CustomUserDetails;
import com.inn.data.chat.ChatRoomDto;
import com.inn.data.chat.ChatRoomRepository;
import com.inn.data.detail.DescriptionDto;
import com.inn.data.hotel.HotelEntity;
import com.inn.data.hotel.HotelRepository;
import com.inn.data.hotel.TagEntity;
import com.inn.data.member.MemberDto;
import com.inn.data.member.MyPageDto;
import com.inn.data.member.manager.HotelRoomTypeSummaryDto;
import com.inn.data.member.manager.HotelWithManagerDto;
import com.inn.data.post.Post;
import com.inn.data.registerHotel.HotelEditDto;
import com.inn.data.registerHotel.HotelRegistrationDto;
import com.inn.data.registerHotel.RoomTypeEditDto;
import com.inn.data.registerHotel.TagEditDto;
import com.inn.data.rooms.Rooms;
import com.inn.data.rooms.RoomsRepository;
import com.inn.data.rooms.RoomTypes;
import com.inn.data.rooms.RoomTypesRepository;
import com.inn.service.FileStorageService;
import com.inn.service.HotelService;
import com.inn.service.ManagerService;
import com.inn.service.MemberService;
import jakarta.persistence.EntityNotFoundException;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class ManagerController {

    @Autowired
    ManagerService service;
    @Autowired
    MemberService memberService;
    @Autowired
    HotelRepository hotelRepository;
    @Autowired
    RoomTypesRepository roomTypesRepository;
    @Autowired
    RoomsRepository roomsRepository;
    @Autowired
    ChatRoomRepository chatRoomRepository;
    @Autowired
    private HotelService hotelService;

    @Autowired
    FileStorageService fileStorageService;

    @Autowired
    private org.springframework.core.io.ResourceLoader resourceLoader;

    @GetMapping("manage/room/detail")
    public String roomDetail(Long idx){



        return "member/manager/roomdetail";
    }

    @GetMapping("manage/hotel")
    public String room(@AuthenticationPrincipal CustomUserDetails currentUser, Model model){
        if(currentUser==null){
            return "redirect:/?login=false";
        }
        Long currentMemberIdx = currentUser.getIdx();
        List<HotelRoomTypeSummaryDto> list = service.GetAllRoomGroupByDescription(currentMemberIdx);
        model.addAttribute("hotelRoomInfos", list);

        return "member/manager/hotelmanage";
    }

    @GetMapping("manage/room")
    public String hotel(@AuthenticationPrincipal CustomUserDetails currentUser, Model model){
        if(currentUser==null){
            return "redirect:/?login=false";
        }
        Long currentMemberIdx = currentUser.getIdx();
        List<HotelWithManagerDto> list = service.getAllMyHotel(currentMemberIdx);
        model.addAttribute("hotelRoomInfos", list);

        return "member/manager/roommanage";
    }

    @GetMapping("manage/application")
    public ModelAndView application(@AuthenticationPrincipal CustomUserDetails currentUser){
        ModelAndView mv = new ModelAndView("member/manager/application");
        mv.addObject("currentUser", currentUser);
        return mv;
    }

    @PostMapping("manage/apply")
    public String apply(MemberDto dto){
        dto.setStatus(Long.valueOf(1));
        memberService.updateMember(dto);
        return "redirect:/?login=true";
    }

    @GetMapping("manage/addhotel")
    public String addHotel(@AuthenticationPrincipal CustomUserDetails currentUser, Model model){
        List<HotelEntity> hotels = service.GetAllMyHotel(currentUser.getIdx());
        model.addAttribute("hotels", hotels);

        List<Long> hotelIds = hotels.stream().map(HotelEntity::getIdx).collect(Collectors.toList());
        String templateContent = "";
        if(!hotelIds.isEmpty()) {
            List<RoomTypes> roomTypes = service.getRoomTypesByHotelIds(hotelIds);
            model.addAttribute("roomTypes", roomTypes);
            templateContent = hotelService.getHotelDescription(hotelIds.get(0));
        }
        Post postWithTemplate = new Post();

        postWithTemplate.setContent(templateContent);
        model.addAttribute("post", postWithTemplate);
        return "member/manager/addhotel";
    }



    @GetMapping("manage/addnewhotel")
    public String addNewHotel(@AuthenticationPrincipal CustomUserDetails currentUser, Model model) {
        HotelRegistrationDto hotelRegistrationDto = new HotelRegistrationDto();

        // 리소스 폴더의 txt 파일에서 기본 설명을 로드
        try {
            org.springframework.core.io.Resource resource = resourceLoader.getResource("classpath:templates/defaults/hotel-description-template.txt");
            String template = new String(resource.getInputStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
            hotelRegistrationDto.setDesc(template);
        } catch (java.io.IOException e) {
            e.printStackTrace();
            // 파일을 읽지 못한 경우, 비어있거나 기본적인 대체 텍스트를 설정
            hotelRegistrationDto.setDesc("<p>설명 템플릿을 불러오는 데 실패했습니다.</p>");
        }

        model.addAttribute("hotelRegistrationDto", hotelRegistrationDto);
        return "member/manager/addnewhotel";
    }

    @PostMapping("manage/registerHotel")
    public String registerHotel(@AuthenticationPrincipal CustomUserDetails currentUser, @ModelAttribute HotelRegistrationDto hotelRegistrationDto){
        System.out.println("Hotel Name: " + hotelRegistrationDto.getHotel_name());
        System.out.println("Address: " + hotelRegistrationDto.getAddress() + " " + hotelRegistrationDto.getDetailAddress());
        System.out.println("Category: " + hotelRegistrationDto.getCategory());
        hotelService.registerHotel(hotelRegistrationDto, currentUser.getIdx());
        return "member/manager/addnewHotel";

    }

    @PostMapping("manage/changeDesc")
    public String changeDescription(@AuthenticationPrincipal CustomUserDetails currentUser, @ModelAttribute DescriptionDto post){
        String unsafeHtml = post.getContent();
        Safelist safelist = Safelist.relaxed();
        safelist.addAttributes(":all", "style");
        String safeHtml = Jsoup.clean(unsafeHtml, safelist);

        Optional<HotelEntity> hotel = hotelRepository.findById(post.getHotelId());
        if (hotel.isPresent()) {
            HotelEntity hotelEntity = hotel.get();
            hotelEntity.setDescription(safeHtml);
            hotelRepository.save(hotelEntity);
        }
        return "redirect:http://localhost:8080/domestic-accommodations?id="+post.getHotelId();
    }



    @PostMapping("manage/addhotel/action")
    public String addHotelAction(@RequestParam Long hotelIdx,
                               @RequestParam String description,
                               @RequestParam String typeName,
                               @RequestParam long price,
                               @RequestParam long capacity) {
        RoomTypes newRoomType = new RoomTypes();
        HotelEntity hotel = hotelRepository.findById(hotelIdx)
                .orElseThrow(() -> new EntityNotFoundException("Hotel not found with ID: " + hotelIdx));
        newRoomType.setHotel(hotel);
        newRoomType.setDescription(description);
        newRoomType.setTypeName(typeName);
        newRoomType.setPrice(price);
        newRoomType.setCapacity(capacity);

        roomTypesRepository.save(newRoomType);

        return "redirect:/manage/hotel";
    }

    @GetMapping("manage/addroom")
    public String addRoom(@AuthenticationPrincipal CustomUserDetails currentUser, Model model){
        List<HotelEntity> hotels = service.GetAllMyHotel(currentUser.getIdx());
        model.addAttribute("hotels", hotels);

        List<Long> hotelIds = hotels.stream().map(HotelEntity::getIdx).collect(Collectors.toList());

        if(!hotelIds.isEmpty()) {
            List<RoomTypes> roomTypes = service.getRoomTypesByHotelIds(hotelIds);
            model.addAttribute("roomTypes", roomTypes);
        }

        return "member/manager/addroom";
    }

    @PostMapping("manage/addroom/action")
    public String addRoomAction(@RequestParam Long hotelIdx,
                                @RequestParam Long roomTypeIdx,
                                @RequestParam long roomNumber) {

        Rooms newRoom = new Rooms();
        HotelEntity hotel = hotelRepository.findById(hotelIdx)
                .orElseThrow(() -> new EntityNotFoundException("Hotel not found with ID: " + hotelIdx));
        newRoom.setHotel(hotel);
        newRoom.setRoomNumber(roomNumber);

        RoomTypes roomTypeProxy = new RoomTypes();
        roomTypeProxy.setIdx(roomTypeIdx);
        newRoom.setRoomType(roomTypeProxy);

        roomsRepository.save(newRoom);

        return "redirect:/manage/room";
    }

    @GetMapping("manage/reserve")
    public String reserveManage(@AuthenticationPrincipal CustomUserDetails currentUser, Model model){
        List<MyPageDto> list = service.GetAllReservesInMyHotel(currentUser.getIdx());
        model.addAttribute("reserves", list);

        return "member/manager/reservemanage";
    }

    @GetMapping("manage/qna")
    public String qnaManage(@AuthenticationPrincipal CustomUserDetails currentUser, Model model){
        if (currentUser == null) {
            return "redirect:/?login=false";
        }
        Long managerIdx = currentUser.getIdx();

        // 1. 매니저가 관리하는 모든 호텔 목록 조회
        List<HotelEntity> hotels = service.GetAllMyHotel(managerIdx);
        model.addAttribute("hotels", hotels);

        // 2. 해당 호텔들에 대한 모든 채팅방 정보와 마지막 메시지 정보 조회
        List<ChatRoomDto> chatRooms = service.getChatRoomsAndLastMessageByManager(managerIdx);
        model.addAttribute("chatRooms", chatRooms);

        return "member/manager/qna";
    }


    @GetMapping("/api/hotel/description/{hotelId}")
    @ResponseBody
    public Map<String, String> getHotelDescription(@PathVariable Long hotelId) {
        String content = hotelService.getHotelDescription(hotelId);
        return Collections.singletonMap("content", content != null ? content : "");
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        // 1. Fetch the HotelEntity from the database
        HotelEntity hotelEntity = hotelRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid hotel Id:" + id));
        HotelEditDto hotelEditDto = mapEntityToDto(hotelEntity);
        model.addAttribute("hotel", hotelEditDto);
        return "member/manager/hotel-edit-form";
    }

    @PostMapping("/edit/{id}")
    public String processEditForm(@PathVariable("id") Long id,
                                  @ModelAttribute("hotel") HotelEditDto hotelEditDto,
                                  RedirectAttributes redirectAttributes) {
        try {
            HotelEntity hotelEntity = hotelRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid hotel Id:" + id));

            updateEntityFromDto(hotelEntity, hotelEditDto);

            hotelRepository.save(hotelEntity);

            redirectAttributes.addFlashAttribute("successMessage", "Hotel updated successfully!");
        } catch (IOException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Error saving image file.");
            return "redirect:/manage/hotel";
        } catch (Exception e) {
            // Handle other exceptions
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "An error occurred while updating the hotel.");
            return "redirect:/manage/hotel";
        }

        return "redirect:/manage/hotel";
    }


    private HotelEditDto mapEntityToDto(HotelEntity entity) {
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

    private void updateEntityFromDto(HotelEntity entity, HotelEditDto dto) throws IOException {
        // Update basic hotel info
        entity.setHotelName(dto.getHotelName());
        entity.setHotelAddress(dto.getHotelAddress());
        entity.setHotelTel(dto.getHotelTel());
        entity.setHotelCategory(dto.getHotelCategory());
        entity.setDescription(dto.getDescription());

        // --- Handle main image replacement ---
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


}