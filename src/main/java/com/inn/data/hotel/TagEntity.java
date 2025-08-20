package com.inn.data.hotel;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="hotel_tags")
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = "hotel")
public class TagEntity {

    @Id
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "hotel_idx", referencedColumnName = "idx")
    private HotelEntity hotel;

    private boolean sauna;

    private boolean swimming_pool;

    private boolean restaurant;

    private boolean fitness;

    private boolean golf;

    private boolean pc;

    private boolean kitchen;

    private boolean washing_Machine;

    private boolean parking;

    private boolean spa;

    private boolean ski;

    private boolean in_Room_Eating;

    private boolean breakfast;

    private boolean smoking;

    private boolean luggage;

    private boolean disabled;

    private boolean pickup;

    private boolean family;

    private boolean waterpool;

    private boolean view;

    private boolean beach;

    private boolean nicemeal;

    private boolean coupon;

    private boolean discount;
}
