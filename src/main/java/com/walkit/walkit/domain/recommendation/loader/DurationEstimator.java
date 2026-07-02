package com.walkit.walkit.domain.recommendation.loader;

import com.walkit.walkit.domain.recommendation.model.Course;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("data-load")
public class DurationEstimator {

    private static final int WALKING_PACE_M_PER_MIN = 60;

    public int estimateMinutes(Course course) {
        if (course.hasProvidedEstimatedTime()) {
            return clamp(course.getProvidedEstimatedMinutes());
        }
        int estimated = (int) Math.ceil((double) course.getDistanceM() / WALKING_PACE_M_PER_MIN);
        return clamp(estimated);
    }

    private int clamp(int minutes) {
        return Math.max(5, Math.min(minutes, 300));
    }
}
