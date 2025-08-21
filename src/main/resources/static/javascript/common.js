// 로그인 모달 열기 시 초기화
$(document).on('show.bs.modal', '#loginModal', function () {
    console.log('Login modal opening');
    // Reset form to login state
    $(".modal-body").children().addClass("d-none");
    $(".modal-body").children(".login-selector").removeClass("d-none");
    $("#login-btn").attr("type", "button").text("로그인");
    const signForm = $("#sign-form");
    signForm.attr("action", "/login").off("submit");

    // Remove required attributes from signup fields
    signForm.find('input[name="memberName"]').prop('required', false);
    signForm.find('#password').prop('required', false);
    signForm.find('#password-repeat').prop('required', false);
    signForm.find('#memberPhone').prop('required', false);
});

// 로그인 버튼 클릭 이벤트 추가 (Bootstrap data-bs-toggle 외에 추가 보장)
$(document).on('click', '[data-bs-target="#loginModal"]', function(e) {
    console.log('Login button clicked');
});

// 로그아웃은 이제 form submit으로 처리됨 (header.html에서)

$(".email-login").on("click", function() {
    $(this).parents(".login-selector").addClass("d-none");
    $(".modal-body").find(".login-form").removeClass("d-none");
    $(".modal-body").find(".after-click").addClass("d-none");
});

// 뒤로가기 버튼 이벤트
$(document).on("click", ".back-to-selector", function() {
    $(".login-form").addClass("d-none");
    $(".login-selector").removeClass("d-none");
    $(".after-click").addClass("d-none");
    $("#sign-form").attr("action", "/login").off("submit");
    $("#login-btn").attr("type", "button").text("로그인");
});

$("#login-btn").on("click", function () {
    let email = $("input[name='memberEmail']").val();
    $.ajax({
        url: "/isMember",
        method: "POST",
        data: {
            "memberEmail": email
        },
        dataType: "json",
        success: function (result) {
            // --- LOGIN PATH ---
            $(".after-click").removeClass("d-none");
            $("#login-btn").attr("type", "submit");
            // Ensure signup fields are not required
            $('#password').prop('required', true);
            $('input[name="memberName"]').prop('required', false);
            $('#password-repeat').prop('required', false);
            $('#memberPhone').prop('required', false);
        },
        error: function (xhr, status, error) {
            if(xhr.status === 404) {
                // --- SIGNUP PATH ---
                $(".after-click").removeClass("d-none");
                $(".after-click .signin-form").removeClass("d-none");
                $("#sign-form").attr("action", "/signin").on("submit", validateSignupForm);
                $("#login-btn").attr("type", "submit").text("회원가입");
                // Set required attributes for signup fields
                $('#password').prop('required', true);
                $('input[name="memberName"]').prop('required', true);
                $('#password-repeat').prop('required', true);
                $('#memberPhone').prop('required', true);
            } else {
                console.error("AJAX Error: ", status, error);
                alert("로그인 중 오류가 발생했습니다.");
            }
        }
    })
})

// Global AJAX error handler to show login modal on 401 Unauthorized
$(document).ajaxError(function(event, xhr, settings, thrownError) {
    if (xhr.status === 401) {
        // Show the login modal
        $('#loginModal').modal('show');
        // Optionally, you might want to clear any previous error messages in the modal
        // and reset its state if it was used for other purposes.
    }
});

// jQuery와 Bootstrap 로딩 확인
console.log('jQuery loaded:', typeof $ !== 'undefined');
console.log('Bootstrap loaded:', typeof bootstrap !== 'undefined');

// 페이지 로드 시 URL 파라미터를 확인하여 로그인 모달을 자동으로 엽니다.
$(document).ready(function() {
    console.log('Common.js loaded successfully');
    console.log('DOM ready');
    
    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.has('loginRequired') && urlParams.get('loginRequired') === 'true') {
        // 로그인 모달의 ID가 'loginModal'이라고 가정합니다.
        $('#loginModal').modal('show');

        // 로그인 후에는 URL에서 파라미터를 제거하여 페이지를 깔끔하게 유지합니다.
        // 브라우저의 history API를 지원하는 경우에만 작동합니다.
        if (window.history.replaceState) {
            const cleanUrl = window.location.protocol + "//" + window.location.host + window.location.pathname;
            window.history.replaceState({path: cleanUrl}, '', cleanUrl);
        }
    }
});

// Signup phone authentication
let isPhoneVerifiedInSignup = false;
let timerInSignup = null;
let remainingInSignup = 180;

$('#sendAuthBtn-signup').on('click', function () {
    const phone = $('#memberPhone').val().replace(/-/g, '');
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
                $('#serverAuthCode-signup').val(data);

                $('#authSection-signup').show();
                $('#authInput-signup').prop('disabled', false);
                $('#checkAuthBtn-signup').prop('disabled', false);
                $('#authInput-signup').val('');
                $('#authResultMsg-signup').text('');
                $('#timerText-signup').text('');

                startTimerInSignup();
            } else {
                alert("전송 실패. 다시 시도하세요.");
            }
        },
        error: function () {
            alert("오류가 발생했습니다.");
        }
    });
});

$('#checkAuthBtn-signup').on('click', function () {
    const inputCode = $('#authInput-signup').val();
    const serverCode = $('#serverAuthCode-signup').val();

    if (!serverCode) {
        $('#authResultMsg-signup').text("먼저 인증번호를 받아주세요.").css('color', 'red');
        return;
    }

    if (!inputCode) {
        $('#authResultMsg-signup').text("인증번호를 입력하세요.").css('color', 'red');
        return;
    }

    if (inputCode === serverCode) {
        clearInterval(timerInSignup);
        isPhoneVerifiedInSignup = true;

        $('#authResultMsg-signup').text("휴대폰 인증이 완료되었습니다.").css('color', 'green');
        $('#authSection-signup').hide();
        $('#sendAuthBtn-signup').hide();
        $('#memberPhone').prop('readonly', true);
    } else {
        $('#authResultMsg-signup').text("인증번호가 올바르지 않습니다.").css('color', 'red');
    }
});

function startTimerInSignup() {
    clearInterval(timerInSignup);
    remainingInSignup = 180;

    timerInSignup = setInterval(() => {
        remainingInSignup--;

        if (remainingInSignup < 0) {
            clearInterval(timerInSignup);
            $('#authResultMsg-signup').text("⏰ 인증 시간이 만료되었습니다. 다시 시도해주세요.").css('color', 'red');
            $('#authInput-signup').prop('disabled', true);
            $('#checkAuthBtn-signup').prop('disabled', true);
            $('#timerText-signup').text("남은 시간: 0:00");
            return;
        }

        const minutes = Math.floor(remainingInSignup / 60);
        const seconds = remainingInSignup % 60;
        $('#timerText-signup').text(`남은 시간: ${minutes}:${seconds < 10 ? '0' + seconds : seconds}`);
    }, 1000);
}

// Validation function for signup form
function validateSignupForm(){
    if (!isPhoneVerifiedInSignup) {
        alert("휴대폰 인증을 완료해주세요.");
        return false;
    }
    return true;
}
