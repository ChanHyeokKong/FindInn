package com.inn.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ReviewController {

    @GetMapping("/reviews/write")
    public String write(@RequestParam Long order_id, Model model) {


        return "review-form";

    }
 }
