package com.skinvidhi.core.routine;

import com.skinvidhi.core.ingredient.IngredientTag;
import com.skinvidhi.core.routine.RoutinePlan.Note;
import com.skinvidhi.core.routine.RoutinePlan.Step;
import java.util.List;
import java.util.Map;

/**
 * The routine shown to the user: one product per step, with cheaper alternatives.
 *
 * @param picks the chosen product per step; a step with no fitting product (e.g. no treatment) is left out
 * @param totalCents upfront cost; a product used in both AM and PM is counted once
 * @param budgetCents the user's budget, or null for no limit
 */
public record Routine(Map<Step, Pick> picks, IngredientTag treatmentActive, int totalCents, Integer budgetCents,
                      List<Note> notes) {

    public Routine {
        picks = Map.copyOf(picks);
        notes = List.copyOf(notes);
    }

    public boolean withinBudget() {
        return budgetCents == null || totalCents <= budgetCents;
    }

    /** @param cheaperAlternatives other products for this step that cost less, best match first */
    public record Pick(RoutineProduct product, List<RoutineProduct> cheaperAlternatives) {

        public Pick {
            cheaperAlternatives = List.copyOf(cheaperAlternatives);
        }
    }
}
