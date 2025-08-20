package com.inn.controller;

import com.inn.config.CustomUserDetails;
import com.inn.data.chat.ChatRoomDto;
import com.inn.data.chat.ChatRoomRepository;
import com.inn.data.detail.DescriptionDto;
import com.inn.data.hotel.HotelDto;
import com.inn.data.hotel.HotelEntity;
import com.inn.data.hotel.HotelRepository;
import com.inn.data.member.MyPageDto;
import com.inn.data.member.manager.HotelRoomTypeSummaryDto;
import com.inn.data.member.manager.HotelWithManagerDto;
import com.inn.data.post.Post;
import com.inn.data.rooms.Rooms;
import com.inn.data.rooms.RoomsRepository;
import com.inn.data.rooms.RoomTypes;
import com.inn.data.rooms.RoomTypesRepository;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Optional;
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
    public String apply(@AuthenticationPrincipal CustomUserDetails currentUser, HotelEntity entity){
        Long memberIdx = currentUser.getIdx();
        entity.setMemberIdx(memberIdx);
        System.out.println(entity.toString());

        hotelRepository.save(entity);
        memberService.giveManagerRole(currentUser.getIdx());

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
    public String addHotelAction(@RequestParam Long hotelId,
                               @RequestParam String description,
                               @RequestParam String typeName,
                               @RequestParam long price,
                               @RequestParam long capacity) {
        RoomTypes newRoomType = new RoomTypes();
        HotelEntity hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new EntityNotFoundException("Hotel not found with ID: " + hotelId));
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
    public String addRoomAction(@RequestParam Long hotelId,
                                @RequestParam Long roomTypeIdx,
                                @RequestParam long roomNumber) {

        Rooms newRoom = new Rooms();
        HotelEntity hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new EntityNotFoundException("Hotel not found with ID: " + hotelId));
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
}