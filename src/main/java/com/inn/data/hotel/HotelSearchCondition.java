package com.inn.data.hotel;

import java.util.List;

import lombok.Data;

@Data
public class HotelSearchCondition {
    private String keyword;
    private String category;
    private List<String> tags;
    private long price;
}