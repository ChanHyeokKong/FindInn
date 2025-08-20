package com.inn.data.coupon;

import com.inn.data.member.MemberDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserCouponRepository extends JpaRepository<UserCoupon, Long> {

    // ── 존재/조회 기본 ─────────────────────────────────────────────────────────
    boolean existsByMemberAndCoupon_Code(MemberDto member, String code);
    boolean existsByMemberAndCoupon(MemberDto member, Coupon coupon);

    List<UserCoupon> findAllByMemberAndUsedFalse(MemberDto member);
    Page<UserCoupon> findAllByMemberAndUsedFalse(MemberDto member, Pageable pageable);

    Optional<UserCoupon> findByIdAndMember(Long id, MemberDto member);
    List<UserCoupon> findAllByMember(MemberDto member);

    List<UserCoupon> findAllByMemberAndRelatedEvent(MemberDto member, String relatedEvent);
    long countByMemberAndRelatedEvent(MemberDto member, String relatedEvent);

    // 이벤트 코드는 입력 편차가 있을 수 있어 lower 비교로 안전장치
    @Query("select distinct uc.coupon.code " +
           "from UserCoupon uc " +
           "where uc.member = :member and lower(uc.relatedEvent) = lower(:event)")
    List<String> findCodesByMemberAndRelatedEvent(@Param("member") MemberDto member,
                                                  @Param("event") String event);

    // ── 사용 확정(원자적) ──────────────────────────────────────────────────────
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update UserCoupon uc " +
           "   set uc.used = true, uc.usedAt = :now " +
           " where uc.id = :id " +
           "   and uc.member = :member " +   // 소유자 검증
           "   and uc.used = false")
    int markUsedIfUnused(@Param("id") Long id,
                         @Param("member") MemberDto member,
                         @Param("now") LocalDateTime now);

    // ── 비관적 락 단건 조회 ───────────────────────────────────────────────────
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.QueryHints(
        @QueryHint(name = "jakarta.persistence.lock.timeout", value = "5000") // 5초 대기 (DB/드라이버에 따라 무시될 수 있음)
    )
    @Query("select uc from UserCoupon uc where uc.id = :id and uc.member = :member")
    Optional<UserCoupon> findForUpdateById(@Param("id") Long id,
                                           @Param("member") MemberDto member);

    // ── 되돌리기(원자적) ──────────────────────────────────────────────────────
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update UserCoupon uc " +
           "   set uc.used = false, uc.usedAt = null " +
           " where uc.id = :id " +
           "   and uc.member = :member " +   // 소유자 검증
           "   and uc.used = true")
    int revertUseIfUsed(@Param("id") Long id,
                        @Param("member") MemberDto member);

    // ── 편의 메서드(선택) ─────────────────────────────────────────────────────
    // 미사용 중인 특정 코드 쿠폰 1개만 바로 얻고 싶을 때(서비스에서 스트림 필터 줄이기)
    Optional<UserCoupon> findTopByMemberAndCoupon_CodeAndUsedFalseOrderByIdAsc(MemberDto member, String code);
}