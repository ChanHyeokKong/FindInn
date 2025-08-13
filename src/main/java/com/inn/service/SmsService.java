package com.inn.service;

import com.inn.data.booking.BookingSmsInfo;

public interface SmsService {

    String sendAuthCode(String guestPhone);

    void sendBookingConfirmation(BookingSmsInfo info);

}