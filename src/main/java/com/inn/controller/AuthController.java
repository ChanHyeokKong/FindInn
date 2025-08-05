package com.inn.controller;

import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.request.SingleMessageSendingRequest;
import net.nurigo.sdk.message.response.SingleMessageSentResponse;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;

@RestController
public class AuthController {

    private final DefaultMessageService messageService;
    private final String smsFrom;

    // 생성자 주입 방식
    public AuthController(
            @Value("${coolsms.api-key}") String apiKey,
            @Value("${coolsms.api-secret}") String apiSecret,
            @Value("${sms.from}") String smsFrom) {
        this.smsFrom = smsFrom;
        this.messageService = NurigoApp.INSTANCE.initialize(apiKey, apiSecret, "https://api.coolsms.co.kr");
    }

    @GetMapping("/send-one")
    @ResponseBody
    public String sendOne(@RequestParam("pNum") String guestPhone) {
        Message message = new Message();
        int checkNum = new Random().nextInt(888888) + 111111;

        message.setFrom(this.smsFrom);
        message.setTo(guestPhone);
        message.setText("[인증번호] " + checkNum + "를 입력해주세요.");

        try {
            SingleMessageSentResponse response =
                    this.messageService.sendOne(new SingleMessageSendingRequest(message));

            System.out.println("[문자 전송 응답] " + response);

            if (response != null && "2000".equals(response.getStatusCode())) {
                return String.valueOf(checkNum);
            } else {
                return "bad";
            }

        } catch (Exception e) {
            System.err.println("[문자 전송 오류] " + e.getMessage());
            return "bad";
        }
    }
}