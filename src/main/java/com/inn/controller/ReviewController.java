package com.inn.controller;

import com.inn.config.CustomUserDetails;
import com.inn.data.review.Review;
import com.inn.data.review.ReviewDto;
import com.inn.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Controller
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @RestControllerAdvice
    public class GlobalExceptionHandler {
        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex) {
            // Returns a 400 Bad Request status with the exception message as the body
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }


    @GetMapping("/reviews/write")
    public String write(@AuthenticationPrincipal CustomUserDetails currentUser, @RequestParam (value = "id", required = false) Long order_id, Model model) {
        Long mem_id = currentUser.getIdx();
        ReviewDto reviewDto = reviewService.getOrderData(order_id);
        if (!mem_id.equals(reviewDto.getMemberId())) {
            model.addAttribute("message", "해당 페이지에 접근 권한이 없습니다.");
            return "review/close-with-alert";
        }
        model.addAttribute("review", reviewDto);
        return "review/addReview";
    }

    @PostMapping("/reviews/save")
    public String saveReview(@ModelAttribute ReviewDto reviewDto, @RequestParam("files") List<MultipartFile> files, Model model) throws IOException {
        reviewService.save(reviewDto,files);
        model.addAttribute("message", "리뷰가 작성되었습니다!");
        return "review/close-with-alert";
    }

 }
