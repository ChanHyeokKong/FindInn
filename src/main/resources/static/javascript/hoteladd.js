$(document).ready(function() {
    // 페이지 로드 시 방 종류 드롭다운 초기화
    $('#roomTypeName').prop('disabled', true);

    // 호텔 선택 드롭다운 변경 시 이벤트 처리
    $('#hotelName').on('change', function() {
        const selectedHotelId = $(this).val();
        const roomTypeSelect = $('#roomTypeName');

        // 기존 옵션 초기화
        roomTypeSelect.find('option').not(':first').hide();
        roomTypeSelect.val('');

        if (selectedHotelId) {
            // 선택된 호텔에 맞는 방 종류만 보여주기
            roomTypeSelect.find('option[data-hotel-id="' + selectedHotelId + '"]').show();
            roomTypeSelect.prop('disabled', false);
            roomTypeSelect.find('option:first').text('방 종류를 선택하세요');
        } else {
            // 호텔 선택이 해제되면 초기 상태로
            roomTypeSelect.prop('disabled', true);
            roomTypeSelect.find('option:first').text('호텔을 먼저 선택하세요');
        }
    });
});