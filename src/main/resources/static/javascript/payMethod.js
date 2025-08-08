// ✅ 결제 수단 선택 버튼
document.querySelectorAll('.payMethodBtn').forEach(btn => {
    btn.addEventListener('click', () => {
        // 모든 버튼 초기화
        document.querySelectorAll('.payMethodBtn').forEach(b => b.classList.remove('selected'));

        // 클릭한 버튼에 선택 표시
        btn.classList.add('selected');

        // 변수 저장
        payMethod = btn.dataset.method;

        console.log('결제 수단:', payMethod);
    });
});