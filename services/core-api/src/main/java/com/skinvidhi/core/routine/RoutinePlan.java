package com.skinvidhi.core.routine;

import com.skinvidhi.core.ingredient.IngredientTag;
import java.util.List;
import java.util.Map;

/**
 * Ranked candidate products for each routine step, before the budget is applied.
 *
 * @param treatmentActive the active the night treatment was chosen for, or null if none fits
 */
public record RoutinePlan(Map<Step, List<RoutineProduct>> candidates, IngredientTag treatmentActive, List<Note> notes) {

    public RoutinePlan {
        candidates = Map.copyOf(candidates);
        notes = List.copyOf(notes);
    }

    /** The Core 4 routine: AM cleanser, moisturizer, sunscreen; PM cleanser, treatment, moisturizer. */
    public enum Step {
        AM_CLEANSER("cleanser"), AM_MOISTURIZER("moisturizer"), AM_SUNSCREEN("sunscreen"),
        PM_CLEANSER("cleanser"), PM_TREATMENT("treatment"), PM_MOISTURIZER("moisturizer");

        private final String category;

        Step(String category) {
            this.category = category;
        }

        public String category() {
            return category;
        }
    }

    /** Messages shown with the routine. Wording and sources are in docs/routine-rules.md. */
    public enum Note {
        RETINOID_START_SLOWLY("Use your retinoid at night only. Start every other night and build up as your skin "
                + "tolerates it, and wear sunscreen every day."),
        VITAMIN_C_NEEDS_SUNSCREEN("Vitamin C does not protect you from the sun. Use it together with a "
                + "broad-spectrum SPF 30+ sunscreen every morning."),
        AHA_SUNBURN_ALERT("This routine contains an alpha hydroxy acid (AHA), which may increase your skin's "
                + "sensitivity to the sun. Use sunscreen and limit sun exposure while using it and for a week afterwards."),
        SEE_DERMATOLOGIST_FOR_PATCHES("If patches are itchy, scaly, spreading, or don't improve within 2-3 weeks, "
                + "see a dermatologist. This can be a skin condition (such as eczema, psoriasis or seborrheic "
                + "dermatitis) rather than dryness."),
        NO_RETINOIDS_IN_PREGNANCY("Retinoids are left out because they should not be used during pregnancy or "
                + "while breastfeeding."),
        NO_TREATMENT_FITS("No treatment in our catalog fits all your answers, so your night routine has no "
                + "treatment step."),
        OVER_BUDGET("Even the lowest-priced routine that fits your answers costs more than your budget. "
                + "This is that routine.");

        private final String text;

        Note(String text) {
            this.text = text;
        }

        public String text() {
            return text;
        }
    }
}
