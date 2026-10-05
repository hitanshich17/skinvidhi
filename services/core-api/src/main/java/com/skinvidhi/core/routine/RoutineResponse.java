package com.skinvidhi.core.routine;

import com.skinvidhi.core.ingredient.IngredientTag;
import com.skinvidhi.core.routine.RoutinePlan.Note;
import com.skinvidhi.core.routine.RoutinePlan.Step;
import java.util.List;

/**
 * The routine as the API returns it. Prices are in cents so the frontend never does float math on money.
 *
 * @param treatmentActive what the night treatment was chosen for (e.g. "RETINOID"), or null if none fits
 */
public record RoutineResponse(List<StepPick> am, List<StepPick> pm, IngredientTag treatmentActive,
                              int totalCents, Integer budgetCents, boolean withinBudget, Integer monthlyCents,
                              List<NoteText> notes) {

    /** @param step the step's category: cleanser, treatment, moisturizer or sunscreen */
    public record StepPick(String step, Product product, List<Product> cheaperAlternatives) {
    }

    public record Product(String id, String brand, String name, Integer spf, boolean imported,
                          RoutineProduct.Offer offer) {

        static Product of(RoutineProduct p) {
            return new Product(p.id(), p.brand(), p.name(), p.spf(), p.imported(), p.offer());
        }
    }

    public record NoteText(Note code, String text) {
    }

    static RoutineResponse of(Routine routine) {
        return new RoutineResponse(steps(routine, "AM_"), steps(routine, "PM_"), routine.treatmentActive(),
                routine.totalCents(), routine.budgetCents(), routine.withinBudget(), routine.monthlyCents(),
                routine.notes().stream().map(n -> new NoteText(n, n.text())).toList());
    }

    private static List<StepPick> steps(Routine routine, String prefix) {
        return java.util.Arrays.stream(Step.values()) // enum order is the order of application
                .filter(step -> step.name().startsWith(prefix) && routine.picks().containsKey(step))
                .map(step -> {
                    Routine.Pick pick = routine.picks().get(step);
                    return new StepPick(step.category(), Product.of(pick.product()),
                            pick.cheaperAlternatives().stream().map(Product::of).toList());
                })
                .toList();
    }
}
