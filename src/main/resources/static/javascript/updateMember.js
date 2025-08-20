$(document).ready(function() {
    let isPhoneChangeInitiated = false;
    let isPhoneVerifiedForUpdate = false;
    let timerInUpdate = null;

    // Show password modal on page load
    $('#passwordConfirmModal').modal('show');

    // Remove is-invalid class when user types
    $('#currentPassword').on('input', function() {
        $(this).removeClass('is-invalid');
        $(this).next('.invalid-feedback').hide();
    });

    $('#confirmPasswordBtn').on('click', function() {
        const password = $('#currentPassword').val();
        if (!password) {
            $('#currentPassword').addClass('is-invalid');
            return;
        }

        $.ajax({
            url: '/mypage/check-password-and-get-data',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify({ password: password }),
            global: false, // Prevent global AJAX error handler from firing
            success: function(member) {
                // Hide modal and show form
                $('#passwordConfirmModal').modal('hide');
                $('#updateFormContainer').show();

                // Populate form
                console.log("Received member data:", member);
                $('#memberIdx').val(member.idx);
                $('#memberEmail').val(member.memberEmail);
                $('#memberName').val(member.memberName);
                $('#updateMemberPhone').val(member.memberPhone);
            },
            error: function(xhr) {
                // Show error message in modal
                $('#currentPassword').addClass('is-invalid');
                $('#currentPassword').next('.invalid-feedback').show();
            }
        });
    });

    // Allow submitting password by pressing Enter
    $('#passwordConfirmForm').on('submit', function(e) {
        e.preventDefault();
        $('#confirmPasswordBtn').click();
    });

    // Handle modal close button (history.back() for cancel)
    $('#passwordConfirmModal').on('hidden.bs.modal', function (e) {
        // If the form container is not shown, it means the user cancelled or failed to authenticate
        if (!$('#updateFormContainer').is(':visible')) {
            history.back();
        }
    });

    // --- Phone Change Logic ---
    $('#changePhoneBtn').on('click', function() {
        isPhoneChangeInitiated = true;
        isPhoneVerifiedForUpdate = false;
        $('#updateMemberPhone').prop('readonly', false);
        $('#phoneChangeSection').slideDown();
        $(this).hide();
    });

    $('#sendAuthBtn-update').on('click', function () {
        const phone = $('#updateMemberPhone').val().replace(/-/g, '');
        if (!phone) {
            alert("새로운 휴대폰 번호를 입력하세요.");
            return;
        }
        // Disable button to prevent multiple clicks
        $(this).prop('disabled', true).text('전송중...');

        $.ajax({
            url: '/sms/auth',
            method: 'GET',
            data: { guestPhone: phone },
            success: function (data) {
                if (data !== 'bad') {
                    alert("인증번호가 전송되었습니다!");
                    $('#serverAuthCode-update').val(data);
                    startTimerInUpdate();
                } else {
                    alert("전송 실패. 다시 시도하세요.");
                }
            },
            error: function () {
                alert("오류가 발생했습니다.");
            },
            complete: function() {
                $('#sendAuthBtn-update').prop('disabled', false).text('인증번호 받기');
            }
        });
    });

    $('#checkAuthBtn-update').on('click', function () {
        const inputCode = $('#authInput-update').val();
        const serverCode = $('#serverAuthCode-update').val();
        const msgBox = $('#authResultMsg-update');

        if (!serverCode) { return msgBox.text("먼저 인증번호를 받아주세요.").css('color', 'red'); }
        if (!inputCode) { return msgBox.text("인증번호를 입력하세요.").css('color', 'red'); }

        if (inputCode === serverCode) {
            clearInterval(timerInUpdate);
            isPhoneVerifiedForUpdate = true;
            msgBox.text("휴대폰 인증이 완료되었습니다.").css('color', 'green');
            $('#phoneChangeSection').slideUp();
            $('#updateMemberPhone').prop('readonly', true);
        } else {
            msgBox.text("인증번호가 올바르지 않습니다.").css('color', 'red');
        }
    });

    function startTimerInUpdate() {
        clearInterval(timerInUpdate);
        let remaining = 180;
        const timerText = $('#timerText-update');

        timerInUpdate = setInterval(() => {
            remaining--;
            if (remaining < 0) {
                clearInterval(timerInUpdate);
                timerText.text("인증 시간이 만료되었습니다.");
                return;
            }
            const minutes = Math.floor(remaining / 60);
            const seconds = remaining % 60;
            timerText.text(`남은 시간: ${minutes}:${seconds < 10 ? '0' + seconds : seconds}`);
        }, 1000);
    }

    // --- Password Change Logic ---
    $('#changePasswordBtn').on('click', function() {
        $('#passwordChangeSection').slideToggle();
    });

    // --- Form Submission Validation ---
    $('#updateForm').on('submit', function(e) {
        // Phone validation
        if (isPhoneChangeInitiated && !isPhoneVerifiedForUpdate) {
            alert('새로운 전화번호의 인증을 완료해주세요.');
            e.preventDefault();
            return;
        }

        // Password validation
        const newPassword = $('#newPassword').val();
        const newPasswordConfirm = $('#newPasswordConfirm').val();

        if (newPassword || newPasswordConfirm) {
            if (newPassword.length < 4) {
                alert('비밀번호는 4자 이상이어야 합니다.');
                e.preventDefault();
                return;
            }
            if (newPassword !== newPasswordConfirm) {
                alert('새 비밀번호가 일치하지 않습니다.');
                e.preventDefault();
            }
        }
    });
});