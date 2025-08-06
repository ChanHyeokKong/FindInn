document.getElementById('payBtn').addEventListener('click', function () {

    $.ajax({
            url: '/booking/merchantUid',
            type: 'GET',
            success: function (response) {
                console.log("merchantUid 발급:", response);
                if (callback) callback(response); // 발급된 UID를 콜백으로 넘김
            },
            error: function (xhr) {
                alert("고유번호 발급에 실패했습니다.");
                console.error("UID Error:", xhr);
            }
        });

    // 고유번호
    const merchantUid =

    // 채널키
    const channelKey = document.getElementById('channelKey').value;

    // 객실 정보
    const roomId = document.getElementById('roomId').value;
    const checkin = document.getElementById('checkin').value;
    const checkout = document.getElementById('checkout').value;
    const totalPrice = parseInt(document.getElementById('totalPrice').value);

    // 예약자 정보
    const name = document.getElementById('guestName').value;
    const phone = document.getElementById('guestPhone').value;

    if (!name || !phone) {
        alert("예약자 정보를 입력해주세요");
        return;
    }

    // 이곳에서 Booking Insert작업 먼저하기

    const IMP = window.IMP;
    IMP.init("imp37255548"); // 식별코드 (가맹점 고유 식별 코드)

    IMP.request_pay({
        channelKey: channelKey,
        pg: "html5_inicis",
        pay_method: "card",
        merchant_uid: "ORDER" + new Date().getTime(),
        name: "호텔 예약 결제", // ▶️ 실제 예약명으로 교체 가능
        amount: totalPrice,
        buyer_name: name,
        buyer_tel: phone,
        buyer_email: "guest@example.com", // ▶️ 이메일 입력받는 경우 교체
    }, function (rsp) {
        console.log("결제 응답", rsp);

        if (rsp.success) {
            // ✅ 서버로 결제 완료 정보 전달
            $.ajax({
                url: "/payment/validate",
                type: "POST",
                contentType: "application/json",
                data: JSON.stringify({
                    imp_uid: rsp.imp_uid,
                    merchant_uid: rsp.merchant_uid,
                    bookingInfo: rsp.name,
                    guestName: rsp.buyer_name,
                    guestPhone: rsp.buyer_tel,
                    guestEmail: rsp.buyer_email,
                    amount: rsp.paid_amount
                }),
                success: function () {
                    alert("결제가 완료되었습니다.");
                    window.location.href = "/booking"; // 예약 완료 페이지 이동
                },
                error: function () {
                    alert("결제 검증 중 오류 발생.");
                }
            });
        } else {
            alert("결제 실패: " + rsp.error_msg);
        }
    });
});
