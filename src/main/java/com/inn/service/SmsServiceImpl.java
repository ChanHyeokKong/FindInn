package com.inn.service;

import com.inn.data.booking.BookingInfo;
import lombok.RequiredArgsConstructor;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.request.SingleMessageSendingRequest;
import net.nurigo.sdk.message.response.SingleMessageSentResponse;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@RequiredArgsConstructor
public class SmsServiceImpl implements SmsService {

    private final DefaultMessageService messageService;

    @Value("${sms.from}")
    private String smsFrom;

    // 인증번호 문자 전송
    @Override
    public String sendAuthCode(String phoneNumber) {
        int code = new Random().nextInt(888888) + 111111;

        Message message = new Message();
        message.setFrom(smsFrom);  // 발신번호 여기서 사용
        message.setTo(phoneNumber);
        message.setText("[인증번호] " + code + "를 입력해주세요.");

        try {
            SingleMessageSentResponse response =
                    messageService.sendOne(new SingleMessageSendingRequest(message));

            if (response != null && "2000".equals(response.getStatusCode())) {
                return String.valueOf(code);
            } else {
                throw new RuntimeException("문자 전송 실패: 응답코드 " + (response != null ? response.getStatusCode() : "null"));
            }
        } catch (Exception e) {
            throw new RuntimeException("문자 전송 오류", e);
        }
    }

    // 예약 확인 문자
    @Override
    public void sendBookingConfirmation(BookingInfo info) {
        String messageText = String.format(
                "\n[Find Inn]\n예약완료 안내\n\n" +
                        "안녕하세요\n고객님의 예약이 확정되었습니다.\n아래 예약 정보를 확인해 주세요.\n\n" +
                        "- 예약번호 : %s\n" +
                        "- 객실ID : %s\n\n" +
                        "- 입실일시 : %s\n" +
                        "- 퇴실일시 : %s\n\n" +
                        "좋은 곳에서 행복한 시간 되세요.\n\n" +
                        "※ 개인정보보호를 위해 고객님의 전화번호는 안심번호로 숙소에 전달됩니다.",
                info.getMerchantUid(),
                info.getRoomId(),
                info.getCheckin(),
                info.getCheckout()
        );

        Message message = new Message();
        message.setFrom(smsFrom);  // 발신번호
        message.setTo(info.getGuestPhone());
        message.setText(messageText);

        try {
            messageService.sendOne(new SingleMessageSendingRequest(message));
        } catch (Exception e) {
            System.err.println("[문자 전송 오류] " + e.getMessage());
        }
    }
}
