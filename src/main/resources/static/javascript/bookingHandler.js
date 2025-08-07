let isLogined = false;              // 로그인 여부
let isPhoneVerified = false;        // 문자 인증 여부 (smsAuth.js 에서 처리)

window.addEventListener("DOMContentLoaded", function () {
    // 로그인 여부 확인
    isLogined = document.getElementById('isLogined')?.value === 'true';

    // 결제 수단 선택 버튼
    document.querySelectorAll('.payMethodBtn').forEach(btn => {
        btn.addEventListener('click', () => {
            // 모든 버튼 초기화
            document.querySelectorAll('.payMethodBtn').forEach(b => b.classList.remove('selected'));

            // 클릭한 버튼에 선택 표시
            btn.classList.add('selected');

            // hidden input에 선택값 저장
            document.getElementById('payMethod').value = btn.dataset.method;
        });
    });
});
