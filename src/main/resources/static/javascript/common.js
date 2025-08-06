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