document.addEventListener('DOMContentLoaded', () => {
    // ===== DOM 요소 =====
    const couponBtn = document.getElementById('couponBtn');
    const discountAmountInput = document.getElementById('discountAmount');
    const totalPriceElem = document.getElementById('totalPrice');

    const couponModal = document.getElementById('couponModal');
    const couponList = document.getElementById('couponList');
    const closeModal = document.getElementById('closeCouponModal');
    const applyCouponBtn = document.getElementById('applyCouponBtn');

    // 할인 표시 영역
    const discountRow = document.getElementById('discountRow');
    const discountPriceElem = document.getElementById('discountPrice');

    // ===== 사용 가능한 쿠폰 불러오기 =====
    async function loadCoupons() {
        try {
            const response = await fetch(`/api/user-coupons/usable?hotelId=${hotelIdx}&price=${price * nights}`);
            if (!response.ok) throw new Error('쿠폰 조회 실패');
            return await response.json();
        } catch (err) {
            console.error(err);
            alert('쿠폰 조회 중 오류가 발생했습니다.');
            return [];
        }
    }

    // ===== 선택 쿠폰 할인액 계산 =====
    async function calculateDiscount(userCouponId) {
        if (!userCouponId) return 0;
        try {
            const response = await fetch(`/api/user-coupons/discount?userCouponId=${userCouponId}&hotelId=${hotelIdx}&price=${price * nights}`);
            if (!response.ok) throw new Error('할인 계산 실패');
            const data = await response.json();
            return data.discount;
        } catch (err) {
            console.error(err);
            alert('쿠폰 적용 중 오류가 발생했습니다.');
            return 0;
        }
    }

    // ===== 모달 열기 =====
    couponBtn.addEventListener('click', async () => {
        const coupons = await loadCoupons();
        couponList.innerHTML = '';

        // "적용 안함" 옵션
        const liNone = document.createElement('li');
        liNone.textContent = '적용 안함';
        liNone.style.cursor = 'pointer';
        liNone.dataset.userCouponId = '';
        couponList.appendChild(liNone);

        // 쿠폰 목록
        coupons.forEach(c => {
            const li = document.createElement('li');
            li.textContent = `${c.name} - ${c.discount.toLocaleString()}원`;
            li.dataset.userCouponId = c.userCouponId;
            li.style.cursor = 'pointer';
            couponList.appendChild(li);
        });

        couponModal.style.display = 'flex';
    });

    // ===== 쿠폰 선택 시 체크 표시 =====
    couponList.addEventListener('click', (event) => {
        if (event.target.tagName !== 'LI') return;
        Array.from(couponList.children).forEach(li => li.classList.remove('selected'));
        event.target.classList.add('selected');
        selectedCouponId = event.target.dataset.userCouponId || null;
    });

    // ===== 확인 버튼 클릭 =====
    applyCouponBtn.addEventListener('click', async () => {
        // 할인액 계산
        disCount = await calculateDiscount(selectedCouponId);

        // 할인액 input 업데이트
        if (discountAmountInput) discountAmountInput.value = disCount;

        // 할인액 표시 영역 업데이트
        if (disCount > 0) {
            discountRow.style.display = 'flex';
            discountPriceElem.textContent = disCount.toLocaleString() + '원';
        } else {
            discountRow.style.display = 'none';
        }

        // 총 결제 금액 갱신
        if (totalPriceElem) totalPriceElem.textContent = (totalPrice - disCount).toLocaleString() + '원';

        // 모달 닫기
        couponModal.style.display = 'none';
    });

    // ===== 모달 닫기 =====
    closeModal.addEventListener('click', () => {
        couponModal.style.display = 'none';
    });
});
