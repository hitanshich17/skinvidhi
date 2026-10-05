package com.skinvidhi.core.routine;

import static com.skinvidhi.core.ingredient.IngredientTag.*;

import com.skinvidhi.core.ingredient.IngredientTag;
import com.skinvidhi.core.routine.QuizAnswers.ActivesExperience;
import com.skinvidhi.core.routine.QuizAnswers.Avoid;
import com.skinvidhi.core.routine.QuizAnswers.Concern;
import com.skinvidhi.core.routine.QuizAnswers.Reactivity;
import com.skinvidhi.core.routine.QuizAnswers.SkinType;
import com.skinvidhi.core.routine.RoutinePlan.Note;
import com.skinvidhi.core.routine.RoutinePlan.Step;
import com.skinvidhi.core.routine.RoutineProduct.Texture;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Turns quiz answers into ranked candidates for each routine step. Every rule here is documented, with its
 * evidence and source, in docs/routine-rules.md; keep the two in step.
 *
 * <p>Pure logic with no database access, so each rule can be unit-tested.
 */
public final class RoutineRules {

    /** Night treatment actives per concern, in order of preference (docs/routine-rules.md, section 1). */
    static final Map<Concern, List<IngredientTag>> TREATMENTS = Map.of(
            Concern.BREAKOUTS, List.of(RETINOID, AZELAIC_ACID, BHA, NIACINAMIDE),
            Concern.DARK_SPOTS, List.of(VITAMIN_C, AZELAIC_ACID, TRANEXAMIC_ACID, NIACINAMIDE, RETINOID),
            Concern.UNEVEN_TONE, List.of(TRANEXAMIC_ACID, AZELAIC_ACID, VITAMIN_C, NIACINAMIDE),
            Concern.FINE_LINES, List.of(RETINOID, VITAMIN_C, PEPTIDE),
            Concern.REDNESS, List.of(AZELAIC_ACID, NIACINAMIDE, CENTELLA),
            Concern.DULLNESS, List.of(AHA, BHA, VITAMIN_C),
            Concern.DRYNESS, List.of(HYALURONIC_ACID, CERAMIDE),
            Concern.FLAKY_PATCHES, List.of(CERAMIDE, HYALURONIC_ACID),
            Concern.OILINESS, List.of(NIACINAMIDE, BHA));

    /** Retinoids listed this early are stronger (e.g. 1% retinol); beginners get the gentler ones. */
    static final int STRONG_RETINOID_MAX_POSITION = 15;

    private static final Set<IngredientTag> FRAGRANCE_FAMILY = EnumSet.of(FRAGRANCE, FRAGRANCE_ALLERGEN, ESSENTIAL_OIL);
    private static final Set<IngredientTag> EXFOLIANTS = EnumSet.of(AHA, BHA);

    private final QuizAnswers answers;

    private RoutineRules(QuizAnswers answers) {
        this.answers = answers;
    }

    public static RoutinePlan plan(QuizAnswers answers, List<RoutineProduct> catalog) {
        return new RoutineRules(answers).plan(catalog);
    }

    private RoutinePlan plan(List<RoutineProduct> catalog) {
        List<Note> notes = new ArrayList<>();
        List<RoutineProduct> allowed = catalog.stream().filter(this::passesEveryStepFilters).toList();

        IngredientTag treatmentActive = null;
        List<RoutineProduct> treatments = List.of();
        for (Concern concern : answers.concerns().subList(0, 1)) { // the first concern picks the treatment
            for (IngredientTag active : TREATMENTS.get(concern)) {
                List<RoutineProduct> matching = inCategory(allowed, "treatment").stream()
                        .filter(p -> p.hasMainActive(active))
                        .filter(this::treatmentIsSuitable)
                        .toList();
                if (!matching.isEmpty()) {
                    treatmentActive = active;
                    treatments = rank(matching, secondConcernCoverage().thenComparing(byPrice()));
                    break;
                }
            }
        }
        boolean retinoidNight = treatmentActive == RETINOID;

        Map<Step, List<RoutineProduct>> candidates = new EnumMap<>(Step.class);
        candidates.put(Step.PM_TREATMENT, treatments);
        candidates.put(Step.AM_CLEANSER, rank(cleansers(allowed, retinoidNight, true), cleanserOrder()));
        candidates.put(Step.PM_CLEANSER, rank(cleansers(allowed, retinoidNight, false), cleanserOrder()));
        List<RoutineProduct> moisturizers = rank(moisturizers(allowed, retinoidNight), moisturizerOrder());
        candidates.put(Step.AM_MOISTURIZER, moisturizers);
        candidates.put(Step.PM_MOISTURIZER, moisturizers);
        candidates.put(Step.AM_SUNSCREEN, sunscreens(allowed, retinoidNight));

        if (treatmentActive == null) {
            notes.add(Note.NO_TREATMENT_FITS);
        } else if (treatmentActive == RETINOID) {
            notes.add(Note.RETINOID_START_SLOWLY);
        } else if (treatmentActive == VITAMIN_C) {
            notes.add(Note.VITAMIN_C_NEEDS_SUNSCREEN);
        } else if (treatmentActive == AHA) {
            notes.add(Note.AHA_SUNBURN_ALERT);
        }
        if (answers.pregnant()) {
            notes.add(Note.NO_RETINOIDS_IN_PREGNANCY);
        }
        if (answers.has(Concern.FLAKY_PATCHES)) {
            notes.add(Note.SEE_DERMATOLOGIST_FOR_PATCHES);
        }
        return new RoutinePlan(candidates, treatmentActive, notes);
    }

    // ---- Filters for every step (docs/routine-rules.md, section 2) ----

    private boolean passesEveryStepFilters(RoutineProduct p) {
        return !p.hasAny(excludedEverywhere()) && excludedMainActivesEverywhere().stream().noneMatch(p::hasMainActive);
    }

    /** Ingredients no product in the routine may contain at all. */
    Set<IngredientTag> excludedEverywhere() {
        Set<IngredientTag> tags = EnumSet.noneOf(IngredientTag.class);
        for (Avoid avoid : answers.avoid()) {
            switch (avoid) {
                case FRAGRANCE -> tags.addAll(EnumSet.of(FRAGRANCE, FRAGRANCE_ALLERGEN));
                case ESSENTIAL_OILS -> tags.add(ESSENTIAL_OIL);
                case ALCOHOL -> tags.add(DRYING_ALCOHOL);
                case NUTS -> tags.add(NUT);
                case SOY -> tags.add(SOY);
            }
        }
        if (answers.reactivity() == Reactivity.OFTEN || answers.has(Concern.REDNESS)
                || answers.has(Concern.FLAKY_PATCHES)) {
            tags.addAll(FRAGRANCE_FAMILY);
        }
        if (answers.has(Concern.REDNESS)) {
            tags.addAll(EnumSet.of(DRYING_ALCOHOL, MENTHOL, CAMPHOR, SODIUM_LAURYL_SULFATE, UREA, AHA));
        }
        if (answers.pregnant()) {
            tags.add(RETINOID);
        }
        return tags;
    }

    /** Actives that may appear in small amounts but not as a product's main active. */
    Set<IngredientTag> excludedMainActivesEverywhere() {
        Set<IngredientTag> tags = EnumSet.noneOf(IngredientTag.class);
        if (answers.has(Concern.FLAKY_PATCHES)) {
            tags.addAll(EnumSet.of(AHA, BHA, RETINOID, BENZOYL_PEROXIDE)); // barrier repair only
        }
        if (answers.reactivity() == Reactivity.OFTEN) {
            tags.addAll(EnumSet.of(AHA, L_ASCORBIC_ACID, BENZOYL_PEROXIDE)); // strong actives
        }
        return tags;
    }

    // ---- Night treatment ----

    private boolean treatmentIsSuitable(RoutineProduct p) {
        if (p.hasMainActive(RETINOID) && !retinoidStrengthAllowed(p)) {
            return false;
        }
        // Pure L-ascorbic acid can sting reactive skin; derivatives are gentler.
        return !(p.hasMainActive(L_ASCORBIC_ACID) && answers.has(Concern.REDNESS));
    }

    private boolean retinoidStrengthAllowed(RoutineProduct p) {
        boolean adapalene = p.isOtcActive(RETINOID);
        boolean strong = adapalene || p.firstPositions().get(RETINOID) <= STRONG_RETINOID_MAX_POSITION;
        ActivesExperience experience = answers.reactivity() == Reactivity.OFTEN
                ? ActivesExperience.NEVER : answers.activesExperience();
        return switch (experience) {
            case NEVER -> !strong;
            case A_LITTLE -> !adapalene;
            case REGULARLY -> true;
        };
    }

    /** Treatments that also address the second concern come first. */
    private Comparator<RoutineProduct> secondConcernCoverage() {
        List<IngredientTag> second = answers.concerns().size() > 1 ? TREATMENTS.get(answers.concerns().get(1)) : List.of();
        return Comparator.comparing(p -> second.stream().noneMatch(p::hasMainActive));
    }

    // ---- Other steps (docs/routine-rules.md, sections 3 and 4) ----

    private List<RoutineProduct> cleansers(List<RoutineProduct> allowed, boolean retinoidNight, boolean morning) {
        boolean experienced = answers.activesExperience() == ActivesExperience.REGULARLY
                && answers.reactivity() != Reactivity.OFTEN;
        Predicate<RoutineProduct> keep = p -> true;
        if (retinoidNight) {
            // Acne cleansers stay out of a retinoid night; experienced users may use them in the morning.
            boolean allowActives = morning && experienced;
            keep = p -> allowActives || !(p.hasMainActive(BHA) || p.hasMainActive(BENZOYL_PEROXIDE) || p.hasMainActive(AHA));
        }
        return inCategory(allowed, "cleanser").stream().filter(keep).toList();
    }

    private Comparator<RoutineProduct> cleanserOrder() {
        Comparator<RoutineProduct> acneFirst = Comparator.comparing(p ->
                !(answers.has(Concern.BREAKOUTS) && (p.hasMainActive(BENZOYL_PEROXIDE) || p.hasMainActive(BHA))));
        return acneFirst.thenComparing(textureMatch()).thenComparing(byPrice());
    }

    private List<RoutineProduct> moisturizers(List<RoutineProduct> allowed, boolean retinoidNight) {
        boolean dry = answers.skinType() == SkinType.DRY || answers.has(Concern.DRYNESS);
        Set<IngredientTag> excluded = dry
                ? EnumSet.of(DRYING_ALCOHOL, AHA, FRAGRANCE, FRAGRANCE_ALLERGEN) // AAD dry-skin advice
                : EnumSet.noneOf(IngredientTag.class);
        return inCategory(allowed, "moisturizer").stream()
                .filter(p -> !p.hasAny(excluded))
                .filter(p -> !retinoidNight || EXFOLIANTS.stream().noneMatch(p::hasMainActive))
                .toList();
    }

    private Comparator<RoutineProduct> moisturizerOrder() {
        return textureMatch().thenComparing(byPrice());
    }

    private List<RoutineProduct> sunscreens(List<RoutineProduct> allowed, boolean retinoidNight) {
        List<RoutineProduct> sunscreens = inCategory(allowed, "sunscreen").stream()
                .filter(p -> p.spf() != null && p.spf() >= 30)
                .filter(p -> !retinoidNight || EXFOLIANTS.stream().noneMatch(p::hasMainActive))
                .toList();
        boolean mineralPreferred = answers.pregnant() || answers.has(Concern.REDNESS)
                || answers.has(Concern.FLAKY_PATCHES) || answers.reactivity() == Reactivity.OFTEN;
        if (mineralPreferred && sunscreens.stream().anyMatch(RoutineProduct::mineral)) {
            sunscreens = sunscreens.stream().filter(RoutineProduct::mineral).toList();
        }
        boolean tintPreferred = answers.has(Concern.DARK_SPOTS) || answers.has(Concern.UNEVEN_TONE)
                || (answers.skinTone() != null && answers.skinTone() >= 4);
        Comparator<RoutineProduct> tintFirst = Comparator.comparing(p -> !(tintPreferred && p.tinted()));
        return rank(sunscreens, tintFirst.thenComparing(byPrice()));
    }

    // ---- Helpers ----

    /** Dry skin prefers rich textures, oily skin light ones. */
    private Comparator<RoutineProduct> textureMatch() {
        Texture wanted = switch (answers.skinType()) {
            case DRY -> Texture.RICH;
            case OILY, COMBINATION -> Texture.LIGHT;
            default -> null;
        };
        return Comparator.comparing(p -> wanted != null && p.texture() != wanted);
    }

    private static Comparator<RoutineProduct> byPrice() {
        return Comparator.comparing(RoutineProduct::cheapestPriceCents, Comparator.nullsLast(Comparator.naturalOrder()));
    }

    private static List<RoutineProduct> inCategory(List<RoutineProduct> products, String category) {
        return products.stream().filter(p -> category.equals(p.category())).toList();
    }

    private static List<RoutineProduct> rank(List<RoutineProduct> products, Comparator<RoutineProduct> order) {
        return new ArrayList<>(new LinkedHashSet<>(products.stream().sorted(order).toList()));
    }
}
