package com.inn.data.member.manager;

import com.inn.data.hotel.HotelEntity;
import com.inn.rooms.RoomTypes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ManageRepository extends JpaRepository<HotelEntity, Integer> {

    @Query("SELECT new com.inn.data.member.manager.HotelWithManagerDto(h, r, m.memberName, rt) " +
            "FROM HotelEntity h " +
            "JOIN MemberDto m ON h.memberIdx = m.memberIdx " +
            "JOIN Rooms r ON h.hotelIdx = r.hotelId " +
            "JOIN RoomTypes rt ON r.roomType.id = rt.id " +
            "WHERE m.memberIdx = :memberIdx")
    List<HotelWithManagerDto> findHotelRoomsAndTypesByMemberIdx(@Param("memberIdx") Long memberIdx);

    @Query("SELECT new com.inn.data.member.manager.HotelWithManagerDto(h, m.memberName) " +
            "FROM HotelEntity h " +
            "JOIN MemberDto m ON h.memberIdx = m.memberIdx")
    List<HotelWithManagerDto> findAllWithManagerName();

    @Query("select new com.inn.data.member.manager.HotelRoomTypeSummaryDto (" +
            "h.hotelIdx, h.hotelName, m.memberName, rt.typeName, rt.description, rt.capacity, COUNT(r.id)) " +
            "from HotelEntity h " +
            "JOIN MemberDto m on h.memberIdx = m.memberIdx " +
            "JOIN RoomTypes rt on h.hotelIdx = rt.hotelId " +   // RoomType은 반드시 있어야 함 (INNER JOIN)
            "LEFT JOIN Rooms r on rt.id = r.roomType.id " +      // Rooms는 없을 수도 있음 (LEFT JOIN)
            "where m.memberIdx = :memberIdx " +
            "GROUP BY h.hotelIdx, h.hotelName, m.memberName, rt.typeName, rt.description, rt.capacity, rt.id")
    List<HotelRoomTypeSummaryDto> findHotelRoomTypesByMemberIdx(@Param("memberIdx") Long memberIdx);

    @Query("select h from HotelEntity h where h.memberIdx = :memberIdx")
    List<HotelEntity> findHotelByMemberIdx(@Param("memberIdx") Long memberIdx);

    @Query("select rt from RoomTypes rt where rt.hotelId in :hotelIds")
    List<RoomTypes> findRoomTypesByHotelIdIn(@Param("hotelIds") List<Integer> hotelIds);
}
