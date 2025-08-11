package com.inn.data.coupon;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CouponRepository extends JpaRepository<Coupon, Long> {

    /** 코드로 단건 조회 (예: "AUG_7P") */
    Optional<Coupon> findByCode(String code);

    /** 심리테스트 trait 전용 쿠폰 조회 (예: "healing") */
    Optional<Coupon> findByApplicableTrait(String trait);

    /** (선택) 발급 출처로 조회 (운영/관리자 화면에서 유용) */
    List<Coupon> findAllByIssuedFrom(String issuedFrom);

    /** (선택) 배타 그룹으로 조회 */
    List<Coupon> findAllByExclusiveGroup(String exclusiveGroup);

    /** (선택) 스택 가능 쿠폰들 조회 */
    List<Coupon> findAllByStackableTrue();
}