package com.inn.controller;

import ch.qos.logback.core.model.Model;
import com.inn.data.member.MemberDaoInter;
import com.inn.data.member.MemberDto;
import com.inn.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class MemberController {

    @Autowired
    MemberService service;

    @Autowired
    MemberDaoInter dao;

    @PostMapping("/isMember")
    public ResponseEntity<MemberDto> isMember(MemberDto dto){
        MemberDto m_dto = service.getMemberByEmail(dto.getM_email());
        if (m_dto == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(m_dto, HttpStatus.OK);
    }

    @PostMapping("/signin")
    public String signin(MemberDto dto){
        service.signup(dto);
        return "redirect:/";
    }

    @GetMapping("/member/list")
    public String memberList(){
        return "member/admin/memberList";
    }

}