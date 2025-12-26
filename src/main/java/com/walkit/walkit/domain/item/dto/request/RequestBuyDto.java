package com.walkit.walkit.domain.item.dto.request;

import lombok.Getter;

import java.util.List;

@Getter
public class RequestBuyDto {

    private List<BuyItemDto> items;
    private int totalPrice;

    @Getter
    public static class BuyItemDto {
        private Long itemId;
    }
}
