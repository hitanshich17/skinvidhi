package com.skinvidhi.core.routine;

import com.skinvidhi.core.climate.Climate;
import com.skinvidhi.core.ingredient.IngredientTag;
import com.skinvidhi.core.routine.RoutinePlan.Note;
import com.skinvidhi.core.routine.RoutinePlan.Step;
import java.util.List;

/**
 * The routine as the API returns it. Prices are in cents so the frontend never does float math on money.
 *
 * @param treatmentActive what the night treatment was chosen for (e.g. "RETINOID"), or null if none fits
 * @param climate the city's climate, or null if no city was given or it wasn't found
 */
public record RoutineResponse(List<StepPick> am, List<StepPick> pm, IngredientTag treatmentActive,
                              int totalCents, Integer budgetCents, boolean withinBudget, Integer monthlyCents,
                              CityClimate climate, List<NoteText> notes) {

    /**
     * What the routine was adjusted for. The page must credit "Weather data by Open-Meteo.com" (CC BY 4.0).
     *
     * @param signals which climate rules applied, e.g. HIGH_UV or DRY_AIR
     */
    public record CityClimate(String city, String state, double uvIndexMax, double dewPointC, double humidity,
                              Double pm25, java.util.Set<Climate.Signal> signals) {

        static CityClimate of(Climate c) {
            return c == null ? null
                    : new CityClimate(c.city(), c.state(), c.uvIndexMax(), c.dewPointC(), c.humidity(), c.pm25(),
                            c.signals());
        }
    }

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

    static RoutineResponse of(Routine routine, Climate climate) {
        return new RoutineResponse(steps(routine, "AM_"), steps(routine, "PM_"), routine.treatmentActive(),
                routine.totalCents(), routine.budgetCents(), routine.withinBudget(), routine.monthlyCents(),
                CityClimate.of(climate), routine.notes().stream().map(n -> new NoteText(n, n.text())).toList());
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
