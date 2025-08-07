let isLogined = false; // ✅ 로그인 상태
let isPhoneVerified = false;

window.addEventListener("DOMContentLoaded", function () {
    isLogined = document.getElementById('isLogined')?.value === 'true';
});
