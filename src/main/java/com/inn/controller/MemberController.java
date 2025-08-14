package com.inn.controller;

import com.inn.config.CustomUserDetails;
import com.inn.data.member.MemberDaoInter;
import com.inn.data.member.MemberDto;
import com.inn.data.member.MyPageDto;
import com.inn.data.member.QnaDto;
import com.inn.data.member.QnaRepository;
import com.inn.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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
    QnaRepository qnaRepository;

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

    @GetMapping("/mypage/reserve")
    public String mypage(@AuthenticationPrincipal CustomUserDetails currentUser, Model model) {

        List<MyPageDto> list = service.getMyReserve(currentUser.getIdx());
        model.addAttribute("reserves", list);
        return "member/myreserve";
    }

    @GetMapping("/qna")
    public String qna(Model model) {
        List<QnaDto> qnaList = qnaRepository.findAll();
        model.addAttribute("qnaList", qnaList);
        return "member/qna";
    }

    @GetMapping("/qna/write")
    public String qnawrite() {
        return "member/qnawrite";
    }

    @GetMapping("/mypage/qna")
    public String myQna(@AuthenticationPrincipal CustomUserDetails currentUser, Model model) {
        if (currentUser == null) {
            return "redirect:/?login=false";
        }
        Long memberIdx = currentUser.getIdx();
        List<com.inn.data.chat.ChatRoomDto> chatRooms = service.getChatRoomsForMember(memberIdx);
        model.addAttribute("chatRooms", chatRooms);
        model.addAttribute("currentMemberIdx", memberIdx);
        return "member/myqna";
    }

}