package com.walkit.walkit.domain.spot.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum SpotCategoryImage {
    HYPERMARKET("대형마트", "MT1"),
    CONVENIENCE_STORE("편의점", "CS2"),
    DAYCARE_KINDERGARTEN("어린이집, 유치원", "PS3"),
    SCHOOL("학교", "SC4"),
    ACADEMY("학원", "AC5"),
    PARKING_LOT("주차장", "PK6"),
    GAS_STATION_EV_CHARGER("주유소, 충전소", "OL7"),
    SUBWAY_STATION("지하철역", "SW8"),
    BANK("은행", "BK9"),
    CULTURAL_FACILITY("문화시설", "CT1"),
    REAL_ESTATE_AGENCY("중개업소", "AG2"),
    PUBLIC_INSTITUTION("공공기관", "PO3"),
    TOURIST_ATTRACTION("관광명소", "AT4"),
    ACCOMMODATION("숙박", "AD5"),
    RESTAURANT("음식점", "FD6"),
    CAFE("카페", "CE7"),
    HOSPITAL("병원", "HP8"),
    PHARMACY("약국", "PM9");

    private final String categoryGroupName;
    private final String kakaoGroupCode;

    private static final Map<String, SpotCategoryImage> BY_CATEGORY =
            Arrays.stream(values()).collect(Collectors.toMap(e -> e.categoryGroupName, e -> e));

    SpotCategoryImage(String categoryGroupName, String kakaoGroupCode) {
        this.categoryGroupName = categoryGroupName;
        this.kakaoGroupCode = kakaoGroupCode;
    }

    public String getKakaoGroupCode() {
        return kakaoGroupCode;
    }

    public static String toImageUrl(String categoryName, String ncpBaseUrl) {
        if (categoryName == null || categoryName.isBlank()) return null;
        for (String segment : categoryName.split(" > ")) {
            SpotCategoryImage matched = BY_CATEGORY.get(segment.trim());
            if (matched != null) return ncpBaseUrl + "/" + matched.name() + ".png";
        }
        return null;
    }
}
