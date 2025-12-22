package com.walkit.walkit.domain.weather.util;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.walkit.walkit.domain.weather.dto.KmaItem;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record KmaApiResponse(Response response) {

    public List<KmaItem> itemsOrThrow() {
        if (response == null || response.body == null || response.body.items == null || response.body.items.item == null) {
            throw new IllegalStateException("KMA response parsing failed.");
        }
        return response.body.items.item;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Response(Header header, Body body) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Header(String resultCode, String resultMsg) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Body(Items items) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Items(List<KmaItem> item) {}
}
