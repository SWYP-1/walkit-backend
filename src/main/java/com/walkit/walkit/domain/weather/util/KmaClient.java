package com.walkit.walkit.domain.weather.util;


import com.walkit.walkit.domain.weather.dto.KmaItem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Component
public class KmaClient {

    private final WebClient webClient;
    private final String serviceKey; // "인코딩키" 권장

    public KmaClient(
            @Value("${kma.baseUrl}") String baseUrl,
            @Value("${kma.serviceKey}") String serviceKey
    ) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
        this.serviceKey = serviceKey;
    }

    public List<KmaItem> getUltraSrtNcst(String baseDate, String baseTime, int nx, int ny) {
        KmaApiResponse res = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/getUltraSrtNcst")
                        .queryParam("serviceKey", serviceKey)
                        .queryParam("pageNo", 1)
                        .queryParam("numOfRows", 1000)
                        .queryParam("dataType", "JSON")
                        .queryParam("base_date", baseDate)
                        .queryParam("base_time", baseTime)
                        .queryParam("nx", nx)
                        .queryParam("ny", ny)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(KmaApiResponse.class)
                .block();

        return res.itemsOrThrow();
    }

    public List<KmaItem> getUltraSrtFcst(String baseDate, String baseTime, int nx, int ny) {
        KmaApiResponse res = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/getUltraSrtFcst")
                        .queryParam("serviceKey", serviceKey)
                        .queryParam("pageNo", 1)
                        .queryParam("numOfRows", 1000)
                        .queryParam("dataType", "JSON")
                        .queryParam("base_date", baseDate)
                        .queryParam("base_time", baseTime)
                        .queryParam("nx", nx)
                        .queryParam("ny", ny)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(KmaApiResponse.class)
                .block();

        return res.itemsOrThrow();
    }
}
