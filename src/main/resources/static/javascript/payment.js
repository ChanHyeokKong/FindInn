document.getElementById('payBtn').addEventListener('click', function () {
    // 예약자 정보
    const name = document.getElementById('guestName').value;
    const phone = document.getElementById('guestPhone').value;
    if (!name || !phone) {
        alert("예약자 정보를 입력해주세요");
        return;
    }

    // 비회원 휴대폰 인증 여부 확인
    if (!isLogined && !isPhoneVerified) {
        alert("휴대폰 인증이 필요합니다.");
        return;
    }

    // 로그인 정보
    const memberId = isLogined ? document.getElementById('memberId')?.value : null;
    const memberEmail = isLogined ? document.getElementById('memberEmail')?.value : "test@example.com";

    // 예약 정보
    const roomId = document.getElementById('roomId').value;
    const checkin = document.getElementById('checkin').value;
    const checkout = document.getElementById('checkout').value;
    const totalPrice = parseInt(document.getElementById('totalPrice').value);
    const channelKey = document.getElementById('channelKey').value;

    // 1. 고유 merchant_uid 먼저 요청
    $.ajax({
        url: "/booking/merchantUid",
        type: "GET",
        success: function (merchantUid) {
            console.log("✅ UID 발급 성공:", merchantUid);
            // 2. 예약 겹침 여부 확인
            $.ajax({
                url: "/booking/validate",
                type: "GET",
                data: {
                    roomId: roomId,
                    checkin: checkin,
                    checkout: checkout
                },
                success: function (isAvailable) {
                    if (!isAvailable) {
                        alert("❌ 해당 날짜에 이미 예약이 있습니다.");
                        return;
                    }

                    // 3. 예약 저장
                    $.ajax({
                        url: "/booking/insert",
                        type: "POST",
                        contentType: "application/json",
                        data: JSON.stringify({
                            merchantUid: merchantUid,
                            roomId: roomId,
                            memberId: memberId,
                            checkin: checkin,
                            checkout: checkout,
                            price: totalPrice
                        }),
                        success: function (response) {
                            console.log("✅ 예약 저장 완료");
                            const bookingId = response.id;

                            // 4. 포트원 결제 요청
                            const IMP = window.IMP;
                            IMP.init("imp37255548");

                            IMP.request_pay({
                                channelKey: channelKey,
                                pay_method: "card",
                                merchant_uid: merchantUid,
                                name: "호텔 예약 결제",
                                amount: totalPrice,
                                buyer_name: name,
                                buyer_email: memberEmail,
                                buyer_tel: phone
                            }, function (rsp) {
                                if (rsp.success) {
                                    console.log("결제 응답 rsp:", rsp);
                                    // 5. 결제 검증
                                    $.ajax({
                                        url: "/payment/validate",
                                        type: "POST",
                                        data: {
                                            imp_uid: rsp.imp_uid,
                                            merchant_uid: rsp.merchant_uid
                                        },
                                        success: function () {
                                            // 6. 결제 검증 성공하면 결제 정보 저장
                                            $.ajax({
                                                url: "/payment/insert",
                                                type: "POST",
                                                contentType: "application/json",
                                                data: JSON.stringify({
                                                    bookingId: bookingId,
                                                    impUid: rsp.imp_uid,
                                                    merchantUid: rsp.merchant_uid,
                                                    payMethod: rsp.pay_method,
                                                    paidAmount: rsp.paid_amount,
                                                    buyerName: rsp.buyer_name,
                                                    buyerTel: rsp.buyer_tel,
                                                    buyerEmail: rsp.buyer_email
                                                }),
                                                success: function () {
                                                    // 7. 결제 완료 후 예약 확인 페이지
                                                    alert("🎉 결제가 완료되었습니다!");
                                                    window.location.href = "/booking/complete";
                                                },
                                                error: function (xhr) {
                                                    const res = xhr.responseJSON;
                                                    const message = res?.message || "결제 정보 저장 중 오류가 발생했습니다.";
                                                    alert("❌ 결제 저장 실패: " + message);

                                                    // 예약 상태 취소로 업데이트
                                                    $.ajax({
                                                        url: "/booking/update/cancel/" + bookingId,  // 실제 API 경로 맞게 조정
                                                        type: 'PUT',
                                                        success: function() {
                                                            alert("❌ 예약이 취소되었습니다");
                                                            return;
                                                        },
                                                        error: function(xhr) {
                                                            const res = xhr.responseJSON;
                                                            const message = res?.message || '예약 취소 중 오류가 발생했습니다.';
                                                            alert('❌ 예약 취소 실패: ' + message);
                                                        }
                                                    });
                                                    return;
                                                }
                                            });
                                        },
                                        error: function (xhr) {
                                            const res = xhr.responseJSON;
                                            const message = res?.message || "결제 검증 중 오류가 발생했습니다.";
                                            alert("❌ 결제 검증 실패: " + message);

                                            // 예약 상태 취소로 업데이트
                                            $.ajax({
                                                url: "/booking/update/cancel/" + bookingId,  // 실제 API 경로 맞게 조정
                                                type: 'PUT',
                                                success: function() {
                                                    alert("❌ 예약이 취소되었습니다");
                                                    return;
                                                },
                                                error: function(xhr) {
                                                    const res = xhr.responseJSON;
                                                    const message = res?.message || '예약 취소 중 오류가 발생했습니다.';
                                                    alert('❌ 예약 취소 실패: ' + message);
                                                }
                                            });
                                            return;
                                        }
                                    });
                                } else {
                                    alert("❌ 결제 실패: " + rsp.error_msg);

                                    // 예약 상태 취소로 업데이트
                                    $.ajax({
                                        url: "/booking/update/cancel/" + bookingId,  // 실제 API 경로 맞게 조정
                                        type: 'PUT',
                                        success: function() {
                                            alert("❌ 예약이 취소되었습니다");
                                            return;
                                        },
                                        error: function(xhr) {
                                            const res = xhr.responseJSON;
                                            const message = res?.message || '예약 취소 중 오류가 발생했습니다.';
                                            alert('❌ 예약 취소 실패: ' + message);
                                        }
                                    });
                                    return;
                                }
                            });
                        },
                        error: function (xhr) {
                            const res = xhr.responseJSON;
                            const message = res?.message || "예약 저장 중 오류가 발생했습니다.";
                            alert("❌ 예약 저장 실패: " + message);
                            return;
                        }
                    });
                },
                error: function () {
                    alert("❌ 예약 중복 확인 실패");
                    return;
                }
            });
        },
        error: function () {
            alert("❌ 고유 주문번호 발급 실패");
            return;
        }
    });
});
