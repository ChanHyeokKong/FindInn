package com.inn.data.coupon;

import com.inn.data.member.MemberDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserCouponRepository extends JpaRepository<UserCoupon, Long> {

    // ✅ 중복 발급 방지(코드 기반; 엔티티 비교 피해서 안전)
    boolean existsByMemberAndCoupon_Code(MemberDto member, String code);

    // (선택) 동일 템플릿 엔티티로 직접 체크 (DTO를 엔티티로 바꾸기 전까진 가급적 위 메서드 사용 권장)
    boolean existsByMemberAndCoupon(MemberDto member, Coupon coupon);

    // 미사용 쿠폰 목록
    List<UserCoupon> findAllByMemberAndUsedFalse(MemberDto member);

    // 내 쿠폰 단건 조회(소유자 확인 포함)
    Optional<UserCoupon> findByIdAndMember(Long id, MemberDto member);

    // 내 모든 쿠폰
    List<UserCoupon> findAllByMember(MemberDto member);

    // 이벤트(캠페인)별 내 발급 목록
    List<UserCoupon> findAllByMemberAndRelatedEvent(MemberDto member, String relatedEvent);

    // ✅ 오타 수정 완료: countByMemberAndRelatedEvent
    long countByMemberAndRelatedEvent(MemberDto member, String relatedEvent);

    // (편의) 이벤트별 ‘쿠폰 코드’만 조회 — 컨트롤러/뷰에서 가볍게 쓰기 좋음
    @Query("select uc.coupon.code from UserCoupon uc where uc.member = :member and uc.relatedEvent = :event")
    List<String> findCodesByMemberAndRelatedEvent(@Param("member") MemberDto member,
                                                  @Param("event") String event);
}