let isLogined = true; // ✅ 테스트용 로그인 상태
let isPhoneVerified = false;

window.addEventListener("DOMContentLoaded", function () {
    if (isLogined) {
        // 인증 관련 UI 전부 제거
        document.getElementById('sendAuthBtn').style.display = 'none';
    }
});
