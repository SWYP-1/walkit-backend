package com.walkit.walkit.domain.recommendation.service;

import com.walkit.walkit.domain.recommendation.model.CourseSearchResult;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GeoFilterService {

    private static final double EARTH_RADIUS_KM = 6371.0;

    public double haversine(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return EARTH_RADIUS_KM * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    public List<CourseSearchResult> filterByRadius(List<CourseSearchResult> candidates,
                                                    double userLat, double userLng,
                                                    double radiusKm) {
        return candidates.stream()
                .filter(c -> haversine(userLat, userLng, c.startLat(), c.startLng()) <= radiusKm)
                .toList();
    }
}
