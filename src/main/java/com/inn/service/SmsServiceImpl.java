package com.inn.service;

import com.inn.data.booking.BookingSmsInfo;
import com.inn.data.payment.PaymentEntity;
import com.inn.data.payment.PaymentRepository;
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
    private final PaymentRepository paymentRepository;

    @Value("${sms.from}")
    private String smsFrom;

    // 인증번호 문자 전송
    @Override
    public String sendAuthCode(String guestPhone) {
        int code = new Random().nextInt(888888) + 111111;

        Message message = new Message();
        message.setFrom(smsFrom);  // 발신번호 여기서 사용
        message.setTo(guestPhone);
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
    public void sendBookingConfirmation(BookingSmsInfo info) {
        String checkinWithDay = info.getCheckin() + " " + info.getCheckinDay();
        String checkoutWithDay = info.getCheckout() + " " + info.getCheckoutDay();

        String messageText = String.format(
                "[Find Inn] 예약완료 안내               \n\n" +
                        "안녕하세요 Find Inn 입니다.\n고객님의 예약이 확정되었습니다.\n아래 예약 정보를 확인해 주세요.\n\n" +
                        "- 예약번호 : %s\n" +
                        "- 숙소이름 : %s\n" +
                        "- 객실타입 : %s\n\n" +
                        "- 입실일시 : %s\n" +
                        "- 퇴실일시 : %s\n\n" +
                        "좋은 곳에서 행복한 시간 되세요.\n\n" +
                        "※ 개인정보보호를 위해 고객님의 전화번호는 안심번호로 숙소에 전달됩니다.",
                info.getMerchantUid(),
                info.getHotelName(),
                info.getRoomName(),
                checkinWithDay,
                checkoutWithDay
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

    // 예약 취소 문자
    @Override
    public void sendBookingCancelByMerchantUid(String merchantUid) {
        // 결제 정보 조회 (merchantUid 기준)
        PaymentEntity payment = paymentRepository.findByMerchantUid(merchantUid)
                .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다."));

        // 문자 내용 구성
        String messageText = String.format(
                "[Find Inn] 예약취소 안내               \n\n" +
                        "안녕하세요 Find Inn입니다.\n고객님께서 예약하신 예약이 취소되었습니다.\n\n" +
                        "- 예약번호 : %s\n" +
                        "- 환불예정금액 : %,d원\n" +
                        "- 결제수단 : %s\n" +
                        "- 환불소요기간 : 영업일 기준 3~5일\n\n" +
                        "더 좋은 숙소로 다시 만나 뵐 수 있기를 바라겠습니다.\n" +
                        "이용해 주셔서 감사합니다.",
                payment.getMerchantUid(),
                payment.getPaidAmount(),
                payment.getPayMethod()
        );

        try {
            Message message = new Message();
            message.setFrom(smsFrom);
            message.setTo(payment.getBuyerTel());  // 결제 정보에서 전화번호 사용
            message.setText(messageText);

            messageService.sendOne(new SingleMessageSendingRequest(message));
        } catch (Exception e) {
            System.err.println("[문자 전송 오류] " + e.getMessage());
        }
    }
}
