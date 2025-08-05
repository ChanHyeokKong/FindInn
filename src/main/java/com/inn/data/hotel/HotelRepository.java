package com.inn.data.hotel;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestParam;

@Repository
public interface HotelRepository extends JpaRepository<HotelEntity, Integer> {

	 List<HotelEntity> findByhotelNameContaining(String keyword);

	@Query("SELECT new com.inn.data.hotel.HotelWithManagerDto(h, m.memberName) FROM HotelEntity h JOIN MemberDto m ON h.memberIdx = m.memberIdx")
    List<HotelWithManagerDto> findAllWithManagerName();

}
