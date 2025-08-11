package com.inn.data.coupon;

import com.inn.data.member.MemberDto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserCouponRepository extends JpaRepository<UserCoupon, Long> {
    boolean existsByMemberAndCoupon(MemberDto member, Coupon coupon);
    List<UserCoupon> findAllByMemberAndUsedFalse(MemberDto member);
    Optional<UserCoupon> findByIdAndMember(Long id, MemberDto member);
}