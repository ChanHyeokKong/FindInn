package com.inn.data.member;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MemberDaoInter extends JpaRepository<MemberDto, Long> {
//    @Query("SELECT m FROM MemberDto m WHERE m.m_email = :email")
    MemberDto findByMemberEmail(@Param("email") String email);

    @Query("SELECT m FROM MemberDto m JOIN m.roles r WHERE r.roleName = 'ROLE_USER'")
    List<MemberDto> findAllUser();

    @Query("SELECT m FROM MemberDto m JOIN m.roles r WHERE r.roleName = 'ROLE_MANAGER'")
    List<MemberDto> findAllManager();

    @Query("select new com.inn.data.member.MyPageDto(" +
            "h.hotelName, h.hotelAddress, h.idx, rt.typeName, r.roomNumber, b.checkin, b.checkout, b.idx, m.memberName, b.status) " +
            "from BookingEntity b, Rooms r, MemberDto m " +
            "join r.hotel h " +
            "join r.roomType rt " +
            "where b.memberIdx = :memberIdx and b.roomIdx = r.idx and b.memberIdx = m.idx")
    List<MyPageDto> findMyReserve(@Param("memberIdx") Long memberIdx);

    MemberDto findByIdx(Long idx);

    List<MemberDto> findAllByStatus(Long status);

    void deleteByIdx(Long idx);
}
