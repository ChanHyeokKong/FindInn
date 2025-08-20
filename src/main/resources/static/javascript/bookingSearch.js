// bookingSearch.js

$(document).ready(function () {
    const searchBtn = $("#searchBtn");
    const merchantUidInput = $("#merchantUid");

    // 주기적으로 isPhoneVerified 값 확인해서 버튼 활성화
    const checkVerifiedInterval = setInterval(() => {
        if (isPhoneVerified) {
            searchBtn.prop("disabled", false);
            clearInterval(checkVerifiedInterval); // 한 번만 실행
        }
    }, 500);

    // ✅ 버튼 클릭 시 이동
    searchBtn.on("click", function () {
        const merchantUid = merchantUidInput.val().trim();

        if (!merchantUid) {
            alert("예약번호를 입력하세요.");
            return;
        }
        if (!isPhoneVerified) {
            alert("휴대폰 인증을 완료해야 합니다.");
            return;
        }

        window.location.href = "/booking/detail?merchantUid=" + merchantUid;
    });
});
