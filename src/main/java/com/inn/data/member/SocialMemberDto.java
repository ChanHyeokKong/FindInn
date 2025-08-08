package com.inn.data.member;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name="social_member")
public class SocialMemberDto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long idx;

    @Column
    String socialProvider;

    @Column
    String socialProviderKey;

    @Column
    Long memberIdx;


}
