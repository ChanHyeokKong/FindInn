// 예약번호 복사
function copyMerchantUid() {
    const number = document.getElementById('merchantUid').innerText;
    navigator.clipboard.writeText(number).then(() => {
        alert("예약번호가 복사되었습니다.");
    });
}