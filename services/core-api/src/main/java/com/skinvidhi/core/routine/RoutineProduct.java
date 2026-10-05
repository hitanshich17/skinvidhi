package com.skinvidhi.core.routine;

import com.skinvidhi.core.ingredient.IngredientTag;
import java.util.Map;
import java.util.Set;

/**
 * A catalog product as the routine rules see it.
 *
 * @param firstPositions for each tag, the earliest label position of an ingredient with that tag;
 *                       0 means the ingredient is a declared OTC active (adapalene, benzoyl peroxide, UV filters)
 * @param cheapestPriceCents lowest offer price, or null if the product has no offer
 */
public record RoutineProduct(
        String id,
        String brand,
        String name,
        String category,
        Integer spf,
        boolean imported,
        Map<IngredientTag, Integer> firstPositions,
        Integer cheapestPriceCents) {

    /** Actives below this label position are minor (e.g. vitamin C used as an antioxidant preservative). */
    static final int MAIN_ACTIVE_MAX_POSITION = 10;

    /** Retinoids and peptides work at tiny concentrations, so their position says little about their role. */
    private static final Set<IngredientTag> POTENT_AT_LOW_LEVELS = Set.of(IngredientTag.RETINOID, IngredientTag.PEPTIDE);

    public RoutineProduct {
        firstPositions = Map.copyOf(firstPositions);
    }

    public boolean has(IngredientTag tag) {
        return firstPositions.containsKey(tag);
    }

    public boolean hasAny(Set<IngredientTag> tags) {
        return tags.stream().anyMatch(this::has);
    }

    /** True if the tag is a main active of this product rather than a minor ingredient. */
    public boolean hasMainActive(IngredientTag tag) {
        Integer position = firstPositions.get(tag);
        return position != null
                && (position <= MAIN_ACTIVE_MAX_POSITION || POTENT_AT_LOW_LEVELS.contains(tag));
    }

    /** Declared as an OTC drug active (e.g. adapalene 0.1%) rather than a cosmetic ingredient. */
    public boolean isOtcActive(IngredientTag tag) {
        return Integer.valueOf(0).equals(firstPositions.get(tag));
    }

    /** Mineral sunscreen: zinc oxide / titanium dioxide only, no chemical UV filters. */
    public boolean mineral() {
        return !has(IngredientTag.CHEMICAL_UV_FILTER);
    }

    public boolean tinted() {
        return has(IngredientTag.IRON_OXIDE);
    }

    /** Texture guessed from the product name until the catalog stores it. */
    public Texture texture() {
        String n = name.toLowerCase();
        if (n.matches(".*\\b(gel|water|fluid|essence|jelly|foam|whip|lotion|serum|liquid)\\b.*")) {
            return Texture.LIGHT;
        }
        if (n.matches(".*\\b(cream|balm|butter)\\b.*")) {
            return Texture.RICH;
        }
        return Texture.UNKNOWN;
    }

    public enum Texture { LIGHT, RICH, UNKNOWN }
}
