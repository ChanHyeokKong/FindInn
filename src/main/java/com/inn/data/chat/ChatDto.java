package com.inn.data.chat;


import jakarta.persistence.*;
import lombok.Data;

import java.sql.Timestamp;

@Entity
@Data
@Table(name="chat")
public class ChatDto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @Column
    private Long chatRoomIdx;

    @Column
    private Long senderIdx; // Renamed from 'sender'

    @Column
    private String message; // Renamed from 'content'

    @Column
    private Timestamp sendTime;

    // Add hotelIdx for the initial message POST request
    @Transient // This field is not persisted in the database
    private Long hotelIdx;

}
