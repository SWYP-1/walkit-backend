package com.walkit.walkit.domain.recommendation.loader;

import com.walkit.walkit.domain.recommendation.model.Emotion;
import com.walkit.walkit.domain.recommendation.model.TaggedCourse;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
@Profile("data-load")
public class TagValidator {

    private static final Set<String> ALLOWED_ENVIRONMENT_TAGS = Set.of(
            "조용함", "활기참", "자연", "도심", "물길", "숲", "공원", "야경", "짧음", "길음");
    private static final Set<String> ALLOWED_FACILITY_TAGS = Set.of(
            "벤치많음", "화장실", "카페근처");
    private static final Set<String> ALLOWED_CROWD_TAGS = Set.of(
            "인적드묾", "보통", "붐빔");
    private static final Set<String> ALLOWED_EMOTIONS = Set.of(
            Emotion.JOYFUL.name(), Emotion.DELIGHTED.name(), Emotion.HAPPY.name(),
            Emotion.DEPRESSED.name(), Emotion.TIRED.name(), Emotion.IRRITATED.name());

    private static final int MAX_ENVIRONMENT_TAGS = 5;
    private static final int MAX_EMOTIONS = 3;

    public TaggedCourse validateAndFix(TaggedCourse tagged) {
        filterAllowedEnvironmentTags(tagged);
        filterAllowedFacilityTags(tagged);
        filterAllowedCrowdTag(tagged);
        filterAllowedEmotions(tagged);
        applyFactBasedTags(tagged);
        applyEmotionFallback(tagged);
        limitTagCounts(tagged);
        return tagged;
    }

    private void filterAllowedEnvironmentTags(TaggedCourse tagged) {
        tagged.setEnvironmentTags(tagged.getEnvironmentTags().stream()
                .filter(ALLOWED_ENVIRONMENT_TAGS::contains)
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new)));
    }

    private void filterAllowedFacilityTags(TaggedCourse tagged) {
        tagged.setFacilityTags(tagged.getFacilityTags().stream()
                .filter(ALLOWED_FACILITY_TAGS::contains)
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new)));
    }

    private void filterAllowedCrowdTag(TaggedCourse tagged) {
        if (tagged.getCrowdTag() != null && !ALLOWED_CROWD_TAGS.contains(tagged.getCrowdTag())) {
            tagged.setCrowdTag(null);
        }
    }

    private void filterAllowedEmotions(TaggedCourse tagged) {
        tagged.setRecommendedEmotions(tagged.getRecommendedEmotions().stream()
                .filter(ALLOWED_EMOTIONS::contains)
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new)));
    }

    private void applyFactBasedTags(TaggedCourse tagged) {
        int distanceM = tagged.getCourse().getDistanceM();
        if (distanceM < 2000) tagged.addEnvironmentTag("짧음");
        if (distanceM > 5000) tagged.addEnvironmentTag("길음");
    }

    private void applyEmotionFallback(TaggedCourse tagged) {
        if (!tagged.getRecommendedEmotions().isEmpty()) return;

        List<String> fallback = new ArrayList<>();
        Set<String> envTags = Set.copyOf(tagged.getEnvironmentTags());

        if (envTags.contains("물길") || envTags.contains("자연") || envTags.contains("숲")) {
            fallback.add(Emotion.DEPRESSED.name());
        }
        if (envTags.contains("공원")) {
            fallback.add(Emotion.TIRED.name());
        }
        if (envTags.contains("활기참") || envTags.contains("도심")) {
            fallback.add(Emotion.JOYFUL.name());
        }
        if (fallback.isEmpty()) {
            fallback.add(Emotion.HAPPY.name());
        }
        tagged.setRecommendedEmotions(fallback);
    }

    private void limitTagCounts(TaggedCourse tagged) {
        if (tagged.getEnvironmentTags().size() > MAX_ENVIRONMENT_TAGS) {
            tagged.setEnvironmentTags(tagged.getEnvironmentTags().subList(0, MAX_ENVIRONMENT_TAGS));
        }
        if (tagged.getRecommendedEmotions().size() > MAX_EMOTIONS) {
            tagged.setRecommendedEmotions(tagged.getRecommendedEmotions().subList(0, MAX_EMOTIONS));
        }
    }
}
