package com.inn.data.member.manager;

import com.inn.data.hotel.HotelEntity;
import com.inn.data.rooms.RoomTypes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ManageRepository extends JpaRepository<HotelEntity, Long> {

    @Query("SELECT new com.inn.data.member.manager.HotelWithManagerDto(h, r, m.memberName, rt) " +
            "FROM HotelEntity h " +
            "JOIN MemberDto m ON h.memberIdx = m.idx " +
            "JOIN Rooms r ON h.idx = r.hotelId " +
            "JOIN RoomTypes rt ON r.roomType.idx = rt.idx " +
            "WHERE m.idx = :memberIdx")
    List<HotelWithManagerDto> findHotelRoomsAndTypesByMemberIdx(@Param("memberIdx") Long memberIdx);

    @Query("SELECT new com.inn.data.member.manager.HotelWithManagerDto(h, m.memberName) " +
            "FROM HotelEntity h " +
            "JOIN MemberDto m ON h.memberIdx = m.idx")
    List<HotelWithManagerDto> findAllWithManagerName();

    @Query("select new com.inn.data.member.manager.HotelRoomTypeSummaryDto (" +
            "h.idx, h.hotelName, m.memberName, rt.typeName, rt.description, rt.capacity, COUNT(r.idx)) " +
            "from HotelEntity h " +
            "JOIN MemberDto m on h.memberIdx = m.idx " +
            "JOIN RoomTypes rt on h.idx = rt.hotelId " +   // RoomType은 반드시 있어야 함 (INNER JOIN)
            "LEFT JOIN Rooms r on rt.idx = r.roomType.idx " +      // Rooms는 없을 수도 있음 (LEFT JOIN)
            "where m.idx = :memberIdx " +
            "GROUP BY h.idx, h.hotelName, m.memberName, rt.typeName, rt.description, rt.capacity, rt.idx")
    List<HotelRoomTypeSummaryDto> findHotelRoomTypesByMemberIdx(@Param("memberIdx") Long memberIdx);

    @Query("select h from HotelEntity h where h.memberIdx = :memberIdx")
    List<HotelEntity> findHotelByMemberIdx(@Param("memberIdx") Long memberIdx);

    @Query("select rt from RoomTypes rt where rt.hotelId in :hotelIds")
    List<RoomTypes> findRoomTypesByHotelIdIn(@Param("hotelIds") List<Long> hotelIds);
}
