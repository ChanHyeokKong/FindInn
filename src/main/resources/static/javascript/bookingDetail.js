// 예약번호 복사
function copyMerchantUid() {
    const number = document.getElementById('merchantUid').innerText;
    navigator.clipboard.writeText(number).then(() => {
        alert("예약번호가 복사되었습니다.");
    });
}

// 예약 취소
$(document).ready(function() {
    const cancelBtn = $(".btn-cancel");

    cancelBtn.on("click", function() {
        // 취소 전 확인 경고
        if (!confirm("정말로 예약을 취소하시겠습니까?")) {
            return; // 취소 클릭 시 종료
        }

        $.ajax({
            url: "/payment/cancel",
            type: "POST",
            data: {
                merchantUid: merchantUid,
                checkin: checkin
            },
            success: function(data) {
                if (data.result === "success") {
                    alert(data.message);

                    // ✅ 예약 취소 문자 전송
                    $.ajax({
                        url: "/sms/booking-cancel",
                        type: "POST",
                        data: { merchantUid: merchantUid },
                        success: function() {
                            console.log("📨 예약 취소 문자 전송 완료");
                        },
                        error: function(xhr, status, error) {
                            console.error("❌ 예약 취소 문자 전송 실패:", error);
                        }
                    });

                    location.reload(); // 페이지 새로고침
                } else {
                    alert(data.message);
                }
            },
            error: function(xhr, status, error) {
                console.error(error);
                alert("서버 오류로 취소할 수 없습니다.");
            }
        });
    });
});