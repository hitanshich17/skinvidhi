package com.skinvidhi.core.routine;

import static com.skinvidhi.core.ingredient.IngredientTag.AHA;

import com.skinvidhi.core.routine.RoutinePlan.Note;
import com.skinvidhi.core.routine.RoutinePlan.Step;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Picks one product per step from a {@link RoutinePlan}, within the budget ("best match within budget").
 *
 * <p>It starts from each step's best-ranked product. While the total is over budget, it makes the swap that saves
 * the most money per place given up in the rankings, always moving to a cheaper product further down a step's
 * list. A product used in both AM and PM is paid for once, so swapping it means swapping it in both steps.
 */
public final class RoutineSelector {

    /** How many cheaper alternatives each step shows. */
    static final int ALTERNATIVES = 3;

    private RoutineSelector() {
    }

    public static Routine select(RoutinePlan plan, Integer budgetCents) {
        Map<Step, List<RoutineProduct>> lists = new EnumMap<>(Step.class);
        plan.candidates().forEach((step, products) -> {
            if (!products.isEmpty()) {
                lists.put(step, products);
            }
        });
        Map<Step, Integer> chosen = new EnumMap<>(Step.class); // index into each step's list
        lists.keySet().forEach(step -> chosen.put(step, 0));

        while (budgetCents != null && total(lists, chosen) > budgetCents) {
            Map<Step, Integer> best = bestSwap(lists, chosen);
            if (best == null) {
                break; // nothing cheaper left anywhere
            }
            chosen.clear();
            chosen.putAll(best);
        }

        Map<Step, Routine.Pick> picks = new EnumMap<>(Step.class);
        chosen.forEach((step, index) -> {
            RoutineProduct product = lists.get(step).get(index);
            List<RoutineProduct> cheaper = lists.get(step).stream()
                    .filter(p -> !p.equals(product) && p.cheapestPriceCents() < product.cheapestPriceCents())
                    .limit(ALTERNATIVES)
                    .toList();
            picks.put(step, new Routine.Pick(product, cheaper));
        });

        List<Note> notes = new ArrayList<>(plan.notes());
        boolean ahaPicked = picks.values().stream().anyMatch(p -> p.product().hasMainActive(AHA));
        if (ahaPicked && !notes.contains(Note.AHA_SUNBURN_ALERT)) {
            notes.add(Note.AHA_SUNBURN_ALERT); // FDA alert applies to any AHA product, e.g. a glycolic cleanser
        }
        int total = total(lists, chosen);
        if (budgetCents != null && total > budgetCents) {
            notes.add(Note.OVER_BUDGET);
        }
        return new Routine(picks, plan.treatmentActive(), total, budgetCents, notes);
    }

    /** The cheaper choice that gives up the fewest ranking places per dollar saved, or null if none. */
    private static Map<Step, Integer> bestSwap(Map<Step, List<RoutineProduct>> lists, Map<Step, Integer> chosen) {
        int current = total(lists, chosen);
        Map<Step, Integer> best = null;
        double bestScore = 0;
        for (Step step : chosen.keySet()) {
            RoutineProduct from = lists.get(step).get(chosen.get(step));
            for (int i = chosen.get(step) + 1; i < lists.get(step).size(); i++) {
                Map<Step, Integer> next = replace(lists, chosen, from, lists.get(step).get(i), step);
                int saved = current - total(lists, next);
                if (saved <= 0) {
                    continue;
                }
                double score = (double) saved / placesGivenUp(chosen, next);
                if (score > bestScore) {
                    bestScore = score;
                    best = next;
                }
                break; // the best-ranked cheaper option for this step is the only one worth comparing
            }
        }
        return best;
    }

    /** Swaps {@code from} for {@code to} in {@code step}, and in any other step that uses {@code from} too. */
    private static Map<Step, Integer> replace(Map<Step, List<RoutineProduct>> lists, Map<Step, Integer> chosen,
                                              RoutineProduct from, RoutineProduct to, Step step) {
        Map<Step, Integer> next = new EnumMap<>(chosen);
        next.put(step, lists.get(step).indexOf(to));
        chosen.forEach((other, index) -> {
            int toIndex = lists.get(other).indexOf(to);
            if (other != step && lists.get(other).get(index).equals(from) && toIndex >= 0) {
                next.put(other, toIndex);
            }
        });
        return next;
    }

    private static int placesGivenUp(Map<Step, Integer> before, Map<Step, Integer> after) {
        return after.keySet().stream().mapToInt(s -> Math.abs(after.get(s) - before.get(s))).sum();
    }

    private static int total(Map<Step, List<RoutineProduct>> lists, Map<Step, Integer> chosen) {
        return chosen.entrySet().stream()
                .map(e -> lists.get(e.getKey()).get(e.getValue()))
                .distinct()
                .map(RoutineProduct::cheapestPriceCents)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();
    }
}
