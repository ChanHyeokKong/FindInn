package com.inn.controller;

import com.inn.config.CustomUserDetails;
import com.inn.data.chat.ChatRoomDto;
import com.inn.data.member.MemberDaoInter;
import com.inn.data.member.MemberDto;
import com.inn.data.member.MyPageDto;
import com.inn.data.member.QnaDto;
import com.inn.data.member.QnaRepository;
import com.inn.data.review.ReviewDto;
import com.inn.data.review.ReviewRepository;
import com.inn.service.BookingService;
import com.inn.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.userdetails.UserDetails; // UserDetails import 추가
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken; // UsernamePasswordAuthenticationToken import 추가
import org.springframework.security.core.context.SecurityContextHolder; // SecurityContextHolder import 추가
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken; // OAuth2AuthenticationToken import 추가
import org.springframework.security.oauth2.core.user.OAuth2User; // OAuth2User import 추가

import java.util.List;
import java.util.Map; // Map import 추가

@Controller
public class MemberController {

    @Autowired
    MemberService service;

    @Autowired
    MemberDaoInter dao;

    @Autowired
    BookingService bookingService;

    @Autowired
    QnaRepository qnaRepository;

    @Autowired
    ReviewRepository reviewRepository;

    @PostMapping("/isMember")
    public ResponseEntity<MemberDto> isMember(MemberDto dto) {
        System.out.printf("isMember: %s\n", dto.getMemberEmail());
        MemberDto m_dto = service.getMemberByEmail(dto.getMemberEmail());
        if (m_dto == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(m_dto, HttpStatus.OK);
    }

    @PostMapping("/signin")
    public String signin(MemberDto dto) {
        service.signup(dto);
        return "redirect:/";
    }

    @GetMapping("/login")
    public String login() {
        return "login/login";
    }

//    @GetMapping("/mypage/reserve")
//    public String mypage(@AuthenticationPrincipal CustomUserDetails currentUser, Model model) {
//
//        List<MyPageDto> list = service.getMyReserve(currentUser.getIdx());
//        model.addAttribute("reserves", list);
//        return "member/myreserve";
//    }

    @GetMapping("/mypage/reserve")
    public String bookingList(@AuthenticationPrincipal CustomUserDetails currentUser,
                              Model model) {

        if (currentUser == null) {
            return "redirect:/login";
        }

        Long memberIdx = currentUser.getIdx();

        // 통합된 상태별 조회 메서드 사용
        model.addAttribute("confirmedList", bookingService.getBookingsByStatus(memberIdx, "CONFIRMED"));
        model.addAttribute("completedList", bookingService.getBookingsByStatus(memberIdx, "COMPLETED"));
        model.addAttribute("canceledList", bookingService.getBookingsByStatus(memberIdx, "CANCELED"));

        return "booking/bookingList";
    }

    @GetMapping("/mypage/update")
    public String updatePage() {
        return "member/updateMember";
    }

    @PostMapping("/mypage/check-password-and-get-data")
    public ResponseEntity<MemberDto> checkPasswordAndGetData(@AuthenticationPrincipal CustomUserDetails currentUser, @RequestBody Map<String, String> payload) {
        String password = payload.get("password");
        if (service.checkPassword(currentUser.getIdx(), password)) {
            MemberDto member = service.getMemberByEmail(currentUser.getUsername());
            return ResponseEntity.ok(member);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/mypage/update")
    public String updateMember(MemberDto dto, @RequestParam(required = false) String newPassword, @AuthenticationPrincipal CustomUserDetails currentUser, RedirectAttributes redirectAttributes) {
        if (currentUser == null || !currentUser.getIdx().equals(dto.getIdx())) {
            return "redirect:/"; // Or show an error
        }

        service.updateMember(dto, newPassword);
        redirectAttributes.addFlashAttribute("successMessage", "회원 정보가 성공적으로 수정되었습니다.");
        return "redirect:/mypage/update";
    }

    @GetMapping("/qna")
    public String qna(Model model) {
        List<QnaDto> qnaList = qnaRepository.findAll();
        model.addAttribute("qnaList", qnaList);
        return "member/qna";
    }

    @GetMapping("/qna/write")
    public String qnaWrite() {
        return "member/qnawrite";
    }

    @GetMapping("/mypage/qna")
    public String myQna(@AuthenticationPrincipal CustomUserDetails currentUser, Model model) {
        if (currentUser == null) {
            return "redirect:/?login=false";
        }
        Long memberIdx = currentUser.getIdx();
        List<ChatRoomDto> chatRooms = service.getChatRoomsForMember(memberIdx);
        model.addAttribute("chatRooms", chatRooms);
        model.addAttribute("currentMemberIdx", memberIdx);
        return "member/myqna";
    }

    @GetMapping("/mypage/review")
    public String myReviewPage(@AuthenticationPrincipal CustomUserDetails currentUser, Model model) {
        if (currentUser == null) {
            return "redirect:/?login=false";
        }
        Long memberIdx = currentUser.getIdx();
        List<com.inn.data.review.Review> reviews = reviewRepository.findByMember_Idx(memberIdx);
        model.addAttribute("reviews", reviews);
        return "member/myreview";
    }


}