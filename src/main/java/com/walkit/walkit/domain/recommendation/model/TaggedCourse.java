package com.walkit.walkit.domain.recommendation.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
public class TaggedCourse {
    private Course course;
    @Builder.Default
    private List<String> environmentTags = new ArrayList<>();
    @Builder.Default
    private List<String> facilityTags = new ArrayList<>();
    private String crowdTag;
    @Builder.Default
    private List<String> recommendedEmotions = new ArrayList<>();
    private String tagReason;

    public void addEnvironmentTag(String tag) {
        if (!environmentTags.contains(tag)) {
            environmentTags.add(tag);
        }
    }
}
