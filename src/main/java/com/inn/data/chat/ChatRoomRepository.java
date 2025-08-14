package com.inn.data.chat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; // Import Query
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param; // Import Param

import java.util.List;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoomDto, Long> {

    List<ChatRoomDto> findAllByHotelIdx(Long hotelIdx);

    // Explicitly define the query to select only the 'idx' field
    @Query("SELECT cr.idx FROM ChatRoomDto cr WHERE cr.memberIdx = :memberIdx AND cr.hotelIdx = :hotelIdx")
    Long findIdxByMemberIdxAndHotelIdx(@Param("memberIdx") Long memberIdx, @Param("hotelIdx") Long hotelIdx);

    List<ChatRoomDto> findAllByMemberIdx(Long memberIdx);

    List<ChatRoomDto> findAllByHotelIdxIn(List<Long> hotelIdxes);

}
