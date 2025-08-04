package com.inn.controller;

import com.inn.data.member.AnswerRequest;
import com.inn.data.test.CharacterType;
import com.inn.service.TestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/test")
public class TestController {

//    private final TestService testService;
//
//    @GetMapping("/questions")
//    public String showQuestions() {
//        return "templates.test.test-questions";
//    }
//
//    @PostMapping("/result")
//    public String showResult(@ModelAttribute AnswerRequest answers, Model model) {
//        CharacterType result = testService.calculateCharacterType(answers);
//        model.addAttribute("character", result);
//        return "templates.test.test-result";
//
//        CharacterType result = testService.calculateCharacterType(answers);
//        model.addAttribute("character", result);
//        return "templates.test.test-result";
//    }
}