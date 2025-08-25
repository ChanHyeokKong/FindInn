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
            "JOIN Rooms r ON h.idx = r.hotel.idx " +
            "JOIN RoomTypes rt ON r.roomType.idx = rt.idx " +
            "WHERE m.idx = :memberIdx")
    List<HotelWithManagerDto> findHotelRoomsAndTypesByMemberIdx(@Param("memberIdx") Long memberIdx);

    @Query("SELECT new com.inn.data.member.manager.HotelRoomTypeSummaryDto(" +
           "    h.idx, " +
           "    h.hotelName, " +
           "    COUNT(r.idx)) " +
           "FROM HotelEntity h " +
           "LEFT JOIN h.roomTypes rt " +
           "LEFT JOIN rt.rooms r " +
           "WHERE h.memberIdx = :memberIdx " +
           "GROUP BY h.idx, h.hotelName")
    List<HotelRoomTypeSummaryDto> findHotelRoomTypesByMemberIdx(@Param("memberIdx") Long memberIdx);

    @Query("select h from HotelEntity h where h.memberIdx = :memberIdx")
    List<HotelEntity> findHotelByMemberIdx(@Param("memberIdx") Long memberIdx);

    @Query("SELECT rt FROM RoomTypes rt WHERE rt.hotel.idx IN :hotelIds")
    List<RoomTypes> findRoomTypesByHotelIdIn(@Param("hotelIds") List<Long> hotelIds);

    @Query("select new com.inn.data.member.MyPageDto(" +
            "h.hotelName, h.hotelAddress, h.idx, rt.typeName, r.roomNumber, b.checkin, b.checkout, b.idx, m.memberName, b.status) " +
            "from BookingEntity b, Rooms r, MemberDto m " +
            "join r.hotel h " +
            "join r.roomType rt " +
            "where r.hotel.idx in :hotelIdxes and b.roomIdx = r.idx and b.memberIdx = m.idx")
    List<MyPageDto> findMyHotelReserves(@Param("hotelIdxes") List<Long> hotelIdxes);

    @Query("select h.idx from HotelEntity h where h.memberIdx = :memberIdx")
    List<Long> findMyHotelIdxesByMemberIdx(@Param("memberIdx") Long memberIdx);

    @Query("SELECT new com.inn.data.member.manager.HotelWithManagerDto(h, m.memberName, " +
           "  CAST((COUNT(r.idx) - COUNT(b.idx)) AS long)) " +
           "FROM HotelEntity h " +
           "JOIN MemberDto m ON h.memberIdx = m.idx " +
           "LEFT JOIN Rooms r ON r.hotel.idx = h.idx " +
           "LEFT JOIN BookingEntity b ON b.roomIdx = r.idx AND CURRENT_DATE BETWEEN b.checkin AND b.checkout " +
           "GROUP BY h.idx, h.hotelName, m.memberName")
    List<HotelWithManagerDto> findAllWithManagerName();
}
