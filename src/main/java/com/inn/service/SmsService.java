package com.inn.service;

import com.inn.data.booking.BookingInfo;

public interface SmsService {

    String sendAuthCode(String guestPhone);

    void sendBookingConfirmation(BookingInfo info);

}