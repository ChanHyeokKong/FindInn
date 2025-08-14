package com.inn.data.chat;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name="chat_room")
public class ChatRoomDto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @Column
    private Long memberIdx;

    @Column
    private Long hotelIdx;

    @Transient
    private String hotelName;

    @Transient
    private String memberName;

    @Transient
    private String lastMessage;

    @Transient
    private LocalDateTime lastMessageTime;

}
