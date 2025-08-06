package com.inn.data.member.manager;

import com.inn.data.hotel.HotelEntity;
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
            "join Rooms r on h.hotelIdx = r.hotelId " +
            "join RoomTypes rt on r.roomType.id = rt.id " +
            "where m.memberIdx = :memberIdx " +
            "GROUP BY h.hotelIdx, h.hotelName, m.memberName, rt.typeName, rt.description, rt.capacity")
    List<HotelRoomTypeSummaryDto> findHotelRoomTypesByMemberIdx(@Param("memberIdx") Long memberIdx);

}
