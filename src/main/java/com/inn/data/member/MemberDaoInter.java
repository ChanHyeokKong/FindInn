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

    @Query("select new com.inn.data.member.MyPageDto (" +
            "h.hotelName, h.hotelAddress, rt.typeName, r.roomNumber, rs.checkIn, rs.checkOut, rs.idx, m.memberName) " +
            "from Reserve rs " +
            "join HotelEntity h on rs.reserveHotelId = h.idx " +
            "join rs.room r " +
            "join rs.roomType rt " +
            "join MemberDto m on m.idx = :memberIdx " +
            "where rs.reserveUserId = :memberIdx")
    List<MyPageDto> findMyReserve(@Param("memberIdx") Long memberIdx);
}
