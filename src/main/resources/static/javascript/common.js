function errCheck(){
    return true;
}

$(".header-login").find(".btn-info").on("click", function () {
    $(".modal-body").children().addClass("d-none");
    $(".modal-body").children(".login-selector").removeClass("d-none");
    $("#login-btn").attr("type", "button").text("로그인");
    $("#sign-form").attr("action", "/login");
})

$(".header-login").find(".btn-danger").on("click", function () {
    location.href = "/logout";
})

$(".email-login").on("click", function() {
    $(this).parents(".login-selector").addClass("d-none");
    $(".modal-body").find(".login-form").removeClass("d-none");
    $(".modal-body").find(".after-click").addClass("d-none");
})

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
            //성공시 코드
            $(".form-group .after-click").removeClass("d-none");
            $("#login-btn").attr("type", "submit");
        },
        error: function (xhr, status, error) {
            if(xhr.status === 404) {
                //없는 이메일 코드
                $(".form-group .after-click").removeClass("d-none");
                $(".after-click .signin-form").removeClass("d-none");
                $("#sign-form").attr("action", "/signin").on("submit", errCheck);
                $("#login-btn").attr("type", "submit").text("회원가입");


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

// 페이지 로드 시 URL 파라미터를 확인하여 로그인 모달을 자동으로 엽니다.
$(document).ready(function() {
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