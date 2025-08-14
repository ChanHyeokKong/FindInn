package com.inn.data.member.manager;

import com.inn.data.hotel.HotelEntity;
import com.inn.data.member.MyPageDto;
import com.inn.data.rooms.RoomTypes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ManageRepository extends JpaRepository<HotelEntity, Long> {

    // 호텔, 룸, 룸타입, 관리자 명
    @Query("SELECT new com.inn.data.member.manager.HotelWithManagerDto(h, r, m.memberName, rt, COUNT(*)) " +
            "FROM HotelEntity h " +
            "JOIN MemberDto m ON h.memberIdx = m.idx " +
            "JOIN Rooms r ON h.idx = r.hotelId " +
            "JOIN RoomTypes rt ON r.roomType.idx = rt.idx " +
            "WHERE m.idx = :memberIdx")
    List<HotelWithManagerDto> findHotelRoomsAndTypesByMemberIdx(@Param("memberIdx") Long memberIdx);

    @Query("select new com.inn.data.member.manager.HotelRoomTypeSummaryDto (" +
            "h.idx, h.hotelName, m.memberName, rt.typeName, rt.description, rt.capacity, COUNT(r.idx)) " +
            "from HotelEntity h " +
            "JOIN MemberDto m on h.memberIdx = m.idx " +
            "JOIN RoomTypes rt on h.idx = rt.hotel.idx " +   // RoomType은 반드시 있어야 함 (INNER JOIN)
            "LEFT JOIN Rooms r on rt.idx = r.roomType.idx " +      // Rooms는 없을 수도 있음 (LEFT JOIN)
            "where m.idx = :memberIdx " +
            "GROUP BY h.idx, h.hotelName, m.memberName, rt.typeName, rt.description, rt.capacity, rt.idx")
    List<HotelRoomTypeSummaryDto> findHotelRoomTypesByMemberIdx(@Param("memberIdx") Long memberIdx);

    @Query("select h from HotelEntity h where h.memberIdx = :memberIdx")
    List<HotelEntity> findHotelByMemberIdx(@Param("memberIdx") Long memberIdx);

    @Query("select rt from RoomTypes rt WHERE rt.hotel.idx = :hotelId")
    List<RoomTypes> findRoomTypesByHotelIdIn(@Param("hotelIds") List<Long> hotelIds);

    @Query("select new com.inn.data.member.MyPageDto (" +
            "h.hotelName, h.hotelAddress, rt.typeName, r.roomNumber, rs.checkIn, rs.checkOut, rs.idx, m.memberName) " +
            "from Reserve rs " +
            "join HotelEntity h on rs.reserveHotelId = h.idx " +
            "join rs.room r " +
            "join rs.roomType rt " +
            "join MemberDto m on m.idx = rs.reserveUserId " +
            "where rs.reserveHotelId IN :hotelIdxes")
    List<MyPageDto> findMyHotelReserves(@Param("hotelIdxes") List<Long> hotelIdxes);

    @Query("select h.idx from HotelEntity h where h.memberIdx = :memberIdx")
    List<Long> findMyHotelIdxesByMemberIdx(@Param("memberIdx") Long memberIdx);

    @Query("SELECT new com.inn.data.member.manager.HotelWithManagerDto(h, m.memberName, " +
           "  CAST((SUM(CASE WHEN r.idx IS NOT NULL THEN 1 ELSE 0 END) - " +
           "   SUM(CASE WHEN rs.idx IS NOT NULL AND CURRENT_DATE BETWEEN rs.checkIn AND rs.checkOut THEN 1 ELSE 0 END)) AS long) AS availableRoomCount" +
           ") " +
           "FROM HotelEntity h " +
           "JOIN MemberDto m ON h.memberIdx = m.idx " +
           "LEFT JOIN Rooms r ON r.hotelId = h.idx " +
           "LEFT JOIN Reserve rs ON rs.room = r AND CURRENT_DATE BETWEEN rs.checkIn AND rs.checkOut " +
           "GROUP BY h.idx, h.hotelName, m.memberName")
    List<HotelWithManagerDto> findAllWithManagerName();
}
