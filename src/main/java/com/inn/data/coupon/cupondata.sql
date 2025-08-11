 1) 기본 쿠폰 마스터
   ========================= */

/* 전 호텔 5% (스택 불가, 배타그룹 MAIN, 8/31 하드컷) */
INSERT INTO coupon
(id, code, name, type, discount_value, min_order_amount, max_discount_amount,
 valid_from, valid_to, valid_days_from_issue, hard_end_date,
 stackable, exclusive_group, issued_from, applicable_trait, applies_all_hotels)
VALUES
(100, 'GLOBAL_5P', '전 호텔 5% 쿠폰', 'PERCENT', 5, NULL, NULL,
 NOW(), TIMESTAMPADD(DAY, 999, NOW()), NULL, DATE '2025-08-31',
 FALSE, 'MAIN', 'event-test', NULL, TRUE);

/* 8월팩 7% (≥80,000, 상한 20,000, 발급+10일, 8/31 하드컷, 전호텔) */
INSERT INTO coupon
(id, code, name, type, discount_value, min_order_amount, max_discount_amount,
 valid_from, valid_to, valid_days_from_issue, hard_end_date,
 stackable, exclusive_group, issued_from, applicable_trait, applies_all_hotels)
VALUES
(101, 'AUG_7P', '8월팩 7%', 'PERCENT', 7, 80000, 20000,
 NOW(), '2025-08-31 23:59:59', 10, DATE '2025-08-31',
 FALSE, 'MAIN', 'aug-pack', NULL, TRUE);

/* 8월팩 10% (≥120,000, 상한 40,000, 발급+10일, 8/31 하드컷, 전호텔) */
INSERT INTO coupon
(id, code, name, type, discount_value, min_order_amount, max_discount_amount,
 valid_from, valid_to, valid_days_from_issue, hard_end_date,
 stackable, exclusive_group, issued_from, applicable_trait, applies_all_hotels)
VALUES
(102, 'AUG_10P', '8월팩 10%', 'PERCENT', 10, 120000, 40000,
 NOW(), '2025-08-31 23:59:59', 10, DATE '2025-08-31',
 FALSE, 'MAIN', 'aug-pack', NULL, TRUE);

/* 지정 호텔 20% (8/31까지, 전호텔 아님) */
INSERT INTO coupon
(id, code, name, type, discount_value, min_order_amount, max_discount_amount,
 valid_from, valid_to, valid_days_from_issue, hard_end_date,
 stackable, exclusive_group, issued_from, applicable_trait, applies_all_hotels)
VALUES
(103, 'HOTEL_20P', '지정호텔 20%', 'PERCENT', 20, NULL, NULL,
 NOW(), '2025-08-31 23:59:59', NULL, DATE '2025-08-31',
 FALSE, 'MAIN', 'campaign', NULL, FALSE);

/* 심리테스트 4종 7% (상한 20,000, 발급+10일, 8/31 컷, 지정호텔 전용) */
INSERT INTO coupon
(id, code, name, type, discount_value, min_order_amount, max_discount_amount,
 valid_from, valid_to, valid_days_from_issue, hard_end_date,
 stackable, exclusive_group, issued_from, applicable_trait, applies_all_hotels)
VALUES
(201, 'TRAIT_HEAL_7P', '힐링 7%', 'PERCENT', 7, NULL, 20000,
 NOW(), '2025-08-31 23:59:59', 10, DATE '2025-08-31',
 FALSE, 'MAIN', 'event-test', 'healing', FALSE),
(202, 'TRAIT_EMO_7P',  '감성 7%', 'PERCENT', 7, NULL, 20000,
 NOW(), '2025-08-31 23:59:59', 10, DATE '2025-08-31',
 FALSE, 'MAIN', 'event-test', 'emotion', FALSE),
(203, 'TRAIT_ACT_7P',  '액티브 7%', 'PERCENT', 7, NULL, 20000,
 NOW(), '2025-08-31 23:59:59', 10, DATE '2025-08-31',
 FALSE, 'MAIN', 'event-test', 'activity', FALSE),
(204, 'TRAIT_CHL_7P',  '도전 7%', 'PERCENT', 7, NULL, 20000,
 NOW(), '2025-08-31 23:59:59', 10, DATE '2025-08-31',
 FALSE, 'MAIN', 'event-test', 'challenge', FALSE);

/* 여름휴가 5,000원 (중복 사용 가능 = stackable TRUE, 전호텔) */
INSERT INTO coupon
(id, code, name, type, discount_value, min_order_amount, max_discount_amount,
 valid_from, valid_to, valid_days_from_issue, hard_end_date,
 stackable, exclusive_group, issued_from, applicable_trait, applies_all_hotels)
VALUES
(300, 'SUMMER_5K', '여름휴가 지원금 5,000원', 'AMOUNT', 5000, NULL, NULL,
 NOW(), '2025-08-31 23:59:59', NULL, DATE '2025-08-31',
 TRUE, NULL, 'event-test', NULL, TRUE);


/* =========================
   2) 지정 호텔 매핑 (예시)
   - 실제 호텔 PK로 바꿔서 쓰세요!
   ========================= */

-- HOTEL_20P는 호텔 11,12에서만
INSERT INTO coupon_allowed_hotels (coupon_id, hotel_id) VALUES
(103, 11), (103, 12);

-- 성향 쿠폰은 각자 다른 호텔군에만 (예시)
INSERT INTO coupon_allowed_hotels (coupon_id, hotel_id) VALUES
(201, 21), (201, 22), (201, 23),   -- healing
(202, 31), (202, 32),              -- emotion
(203, 41), (203, 42), (203, 43),   -- activity
(204, 51);                         -- challenge
