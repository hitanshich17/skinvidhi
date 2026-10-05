package com.skinvidhi.core.routine;

import java.util.List;
import java.util.Set;

/** Answers to the quiz in docs/quiz.md. */
public record QuizAnswers(
        SkinType skinType,
        List<Concern> concerns,
        Reactivity reactivity,
        Set<Avoid> avoid,
        ActivesExperience activesExperience,
        Pregnancy pregnancy,
        Integer budgetCents,
        String city,
        Integer skinTone) {

    public enum SkinType { OILY, DRY, COMBINATION, NORMAL, NOT_SURE }

    public enum Concern {
        BREAKOUTS, DARK_SPOTS, UNEVEN_TONE, FINE_LINES, REDNESS, DULLNESS, DRYNESS, FLAKY_PATCHES, OILINESS
    }

    public enum Reactivity { RARELY, SOMETIMES, OFTEN }

    public enum Avoid { FRAGRANCE, ESSENTIAL_OILS, ALCOHOL, NUTS, SOY }

    public enum ActivesExperience { NEVER, A_LITTLE, REGULARLY }

    public enum Pregnancy { YES, NO, PREFER_NOT_TO_SAY }

    public QuizAnswers {
        concerns = List.copyOf(concerns);
        avoid = Set.copyOf(avoid);
        if (concerns.isEmpty() || concerns.size() > 2) {
            throw new IllegalArgumentException("pick 1 or 2 concerns");
        }
        if (skinTone != null && (skinTone < 1 || skinTone > 6)) {
            throw new IllegalArgumentException("skin tone is 1 to 6");
        }
    }

    public boolean has(Concern concern) {
        return concerns.contains(concern);
    }

    public boolean pregnant() {
        return pregnancy == Pregnancy.YES;
    }
}
