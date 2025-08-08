// ✅ DOM 요소 캐싱
const authSection = document.getElementById('authSection');
const authInput = document.getElementById('authInput');
const checkAuthBtn = document.getElementById('checkAuthBtn');
const timerText = document.getElementById('timerText');
const msgBox = document.getElementById('authResultMsg');
const sendAuthBtn = document.getElementById('sendAuthBtn');
const guestPhoneInput = document.getElementById('guestPhone');
const serverAuthCodeInput = document.getElementById('serverAuthCode');

// ✅ 타이머 관련 변수
let timer = null;
let remaining = 180;

// ✅ 인증번호 발송 버튼 이벤트
if (sendAuthBtn) {
    sendAuthBtn.addEventListener('click', function () {
        const phone = guestPhoneInput.value;
        if (!phone) {
            alert("휴대폰 번호를 입력하세요.");
            return;
        }

        $.ajax({
            url: '/sms/auth',
            method: 'GET',
            data: { guestPhone: phone },
            success: function (data) {
                if (data !== 'bad') {
                    alert("인증번호가 전송되었습니다!");
                    serverAuthCodeInput.value = data;

                    // 인증 입력창 보이기 및 초기화
                    authSection.style.display = 'block';
                    authInput.disabled = false;
                    checkAuthBtn.disabled = false;
                    authInput.value = '';
                    msgBox.textContent = '';
                    timerText.textContent = '';

                    startTimer();
                } else {
                    alert("전송 실패. 다시 시도하세요.");
                }
            },
            error: function () {
                alert("오류가 발생했습니다.");
            }
        });
    });
}

// ✅ 인증번호 확인 버튼 이벤트
checkAuthBtn.addEventListener('click', function () {
    const inputCode = authInput.value;
    const serverCode = serverAuthCodeInput.value;

    if (!serverCode) {
        msgBox.textContent = "먼저 인증번호를 받아주세요.";
        msgBox.style.color = "red";
        return;
    }

    if (!inputCode) {
        msgBox.textContent = "인증번호를 입력하세요.";
        msgBox.style.color = "red";
        return;
    }

    if (inputCode === serverCode) {
        clearInterval(timer);

        // 인증 관련 요소 비활성화 및 숨김
        authInput.value = '';
        msgBox.textContent = '';
        timerText.textContent = '';
        authInput.disabled = true;
        checkAuthBtn.disabled = true;
        authSection.style.display = "none";
        sendAuthBtn.style.display = "none";

        // ✅ 인증 완료!
        isPhoneVerified = true;

        msgBox.textContent = "휴대폰 인증이 완료되었습니다.";
        msgBox.style.color = "green";
    } else {
        msgBox.textContent = "인증번호가 올바르지 않습니다.";
        msgBox.style.color = "red";
    }
});

// ✅ 타이머 함수
function startTimer() {
    clearInterval(timer);
    remaining = 180;

    timer = setInterval(() => {
        remaining--;

        if (remaining < 0) {
            clearInterval(timer);
            msgBox.textContent = "⏰ 인증 시간이 만료되었습니다. 다시 시도해주세요.";
            msgBox.style.color = "red";

            authInput.disabled = true;
            checkAuthBtn.disabled = true;
            timerText.textContent = "남은 시간: 0:00";
            return;
        }

        const minutes = Math.floor(remaining / 60);
        const seconds = remaining % 60;
        timerText.textContent = `남은 시간: ${minutes}:${seconds < 10 ? '0' + seconds : seconds}`;
    }, 1000);
}
