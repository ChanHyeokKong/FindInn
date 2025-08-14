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
    private Long sender;

    @Column
    private String content;

    @Column
    private Timestamp sendTime;

}
