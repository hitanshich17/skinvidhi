package com.skinvidhi.core.routine;

import static com.skinvidhi.core.ingredient.IngredientTag.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.skinvidhi.core.ingredient.IngredientTag;
import com.skinvidhi.core.routine.RoutinePlan.Note;
import com.skinvidhi.core.routine.RoutinePlan.Step;
import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class RoutineSelectorTest {

    private static RoutineProduct p(String id, String category, int priceCents, Object... tagPositions) {
        Map<IngredientTag, Integer> positions = new HashMap<>();
        for (int i = 0; i < tagPositions.length; i += 2) {
            positions.put((IngredientTag) tagPositions[i], (Integer) tagPositions[i + 1]);
        }
        return new RoutineProduct(id, "Brand", id, category, null, false, positions,
                new RoutineProduct.Offer("brand", priceCents, BigDecimal.valueOf(100), "ml", "https://example.com/" + id));
    }

    static final RoutineProduct CLEANSER = p("cleanser", "cleanser", 1500);
    static final RoutineProduct CHEAP_CLEANSER = p("cheap-cleanser", "cleanser", 500);
    static final RoutineProduct SERUM = p("serum", "treatment", 5000);
    static final RoutineProduct CHEAPER_SERUM = p("cheaper-serum", "treatment", 4000);
    static final RoutineProduct CREAM = p("cream", "moisturizer", 1000);
    static final RoutineProduct SPF = p("spf", "sunscreen", 1000);

    /** Cleanser and moisturizer lists are shared by AM and PM, so the same product fills both. */
    private static RoutinePlan plan() {
        Map<Step, List<RoutineProduct>> candidates = new EnumMap<>(Step.class);
        candidates.put(Step.AM_CLEANSER, List.of(CLEANSER, CHEAP_CLEANSER));
        candidates.put(Step.PM_CLEANSER, List.of(CLEANSER, CHEAP_CLEANSER));
        candidates.put(Step.PM_TREATMENT, List.of(SERUM, CHEAPER_SERUM));
        candidates.put(Step.AM_MOISTURIZER, List.of(CREAM));
        candidates.put(Step.PM_MOISTURIZER, List.of(CREAM));
        candidates.put(Step.AM_SUNSCREEN, List.of(SPF));
        return new RoutinePlan(candidates, NIACINAMIDE, List.of());
    }

    private static String pick(Routine routine, Step step) {
        return routine.picks().get(step).product().id();
    }

    @Test
    void withoutABudgetEveryStepGetsItsBestMatch() {
        Routine routine = RoutineSelector.select(plan(), null);

        assertThat(pick(routine, Step.AM_CLEANSER)).isEqualTo("cleanser");
        assertThat(pick(routine, Step.PM_TREATMENT)).isEqualTo("serum");
        assertThat(routine.totalCents()).isEqualTo(1500 + 5000 + 1000 + 1000); // repeats counted once
        assertThat(routine.withinBudget()).isTrue();
    }

    @Test
    void overBudgetGivesUpAsFewRankingPlacesAsPossible() {
        // Both swaps save $10; the treatment swap moves one step, the shared cleanser two.
        Routine routine = RoutineSelector.select(plan(), 7500);

        assertThat(pick(routine, Step.PM_TREATMENT)).isEqualTo("cheaper-serum");
        assertThat(pick(routine, Step.AM_CLEANSER)).isEqualTo("cleanser");
        assertThat(routine.totalCents()).isEqualTo(7500);
        assertThat(routine.notes()).doesNotContain(Note.OVER_BUDGET);
    }

    @Test
    void aRepeatedProductIsSwappedInBothSteps() {
        Routine routine = RoutineSelector.select(plan(), 6500);

        assertThat(pick(routine, Step.AM_CLEANSER)).isEqualTo("cheap-cleanser");
        assertThat(pick(routine, Step.PM_CLEANSER)).isEqualTo("cheap-cleanser");
        assertThat(routine.totalCents()).isEqualTo(6500);
    }

    @Test
    void cheaperOptionsAreShownAsAlternatives() {
        Routine routine = RoutineSelector.select(plan(), null);

        assertThat(routine.picks().get(Step.AM_CLEANSER).cheaperAlternatives()).containsExactly(CHEAP_CLEANSER);
        assertThat(routine.picks().get(Step.AM_SUNSCREEN).cheaperAlternatives()).isEmpty();
    }

    @Test
    void impossibleBudgetGivesTheCheapestRoutineAndSaysSo() {
        Routine routine = RoutineSelector.select(plan(), 1000);

        assertThat(routine.totalCents()).isEqualTo(500 + 4000 + 1000 + 1000);
        assertThat(routine.withinBudget()).isFalse();
        assertThat(routine.notes()).contains(Note.OVER_BUDGET);
    }

    @Test
    void stepWithoutCandidatesIsLeftOut() {
        Map<Step, List<RoutineProduct>> candidates = new EnumMap<>(plan().candidates());
        candidates.put(Step.PM_TREATMENT, List.of());
        Routine routine = RoutineSelector.select(new RoutinePlan(candidates, null, List.of()), null);

        assertThat(routine.picks()).doesNotContainKey(Step.PM_TREATMENT);
    }

    @Test
    void anAhaInAnyPickedProductShowsTheSunburnAlert() {
        Map<Step, List<RoutineProduct>> candidates = new EnumMap<>(plan().candidates());
        RoutineProduct glycolicCleanser = p("glycolic-cleanser", "cleanser", 900, AHA, 3);
        candidates.put(Step.AM_CLEANSER, List.of(glycolicCleanser));
        Routine routine = RoutineSelector.select(new RoutinePlan(candidates, NIACINAMIDE, List.of()), null);

        assertThat(routine.notes()).containsExactly(Note.AHA_SUNBURN_ALERT);
    }
}
