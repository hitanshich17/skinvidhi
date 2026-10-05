package com.skinvidhi.core.routine;

import com.skinvidhi.core.ingredient.IngredientTag;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/** Loads the curated routine catalog with each product's ingredient tags, label positions and cheapest price. */
@Repository
public class RoutineCatalog {

    private final JdbcTemplate jdbc;

    public RoutineCatalog(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<RoutineProduct> load() {
        record Row(String id, String brand, String name, String category, Integer spf, boolean imported,
                   Integer cheapest, Map<IngredientTag, Integer> positions) {
        }
        Map<Long, Row> rows = new LinkedHashMap<>();
        jdbc.query("""
                WITH tagged AS (
                    SELECT pi.product_id, t.tag, pi.position
                    FROM product_ingredients pi JOIN ingredient_tags t ON t.ingredient_id = pi.ingredient_id
                    UNION ALL
                    SELECT pa.product_id, t.tag, 0  -- declared OTC actives count as position 0
                    FROM product_actives pa JOIN ingredient_tags t ON t.ingredient_id = pa.ingredient_id
                )
                SELECT p.id, p.source_id, p.brand, p.name, p.category, p.spf, p.imported,
                       (SELECT min(o.price_cents) FROM product_offers o WHERE o.product_id = p.id) AS cheapest,
                       tg.tag, min(tg.position) AS first_position
                FROM products p LEFT JOIN tagged tg ON tg.product_id = p.id
                WHERE p.source = 'curated' AND p.category IN ('cleanser', 'treatment', 'moisturizer', 'sunscreen')
                GROUP BY p.id, tg.tag
                ORDER BY p.id
                """, rs -> {
            Row row = rows.computeIfAbsent(rs.getLong("id"), id -> {
                try {
                    return new Row(rs.getString("source_id"), rs.getString("brand"), rs.getString("name"),
                            rs.getString("category"), (Integer) rs.getObject("spf"), rs.getBoolean("imported"),
                            (Integer) rs.getObject("cheapest"), new EnumMap<>(IngredientTag.class));
                } catch (java.sql.SQLException e) {
                    throw new IllegalStateException(e);
                }
            });
            String tag = rs.getString("tag");
            if (tag != null) {
                row.positions().put(IngredientTag.valueOf(tag), rs.getInt("first_position"));
            }
        });
        return rows.values().stream()
                .map(r -> new RoutineProduct(r.id(), r.brand(), r.name(), r.category(), r.spf(), r.imported(),
                        r.positions(), r.cheapest()))
                .toList();
    }
}
