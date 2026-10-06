# "Tried it?" feedback and replacements (step 5)

Decisions by the author; the details below follow from them.

## Storage and privacy

- No accounts. The browser creates a random **client ID** (a UUID) and sends it with each request.
  The server stores feedback under it and nothing that identifies a person.
- With each routine the server stores the quiz answers **without pregnancy and city**. Pregnancy is sensitive
  health data and only a hard filter, so the ranking model (step 6) doesn't need it. The city is replaced by its
  climate signals (e.g. `HUMID`), which is all the model needs.
- One verdict per client and product; a new verdict replaces the old one, and it can be undone.

## Liked

- A liked product stays in its step even if another product ranks higher, **as long as it still passes every
  safety rule** (e.g. a liked retinoid is dropped if the user later answers "pregnant: yes", with a note saying so).
- It costs $0 in the upfront total (the user likely owns it); the monthly estimate still counts it, since it will
  need rebuying.
- If a liked product fits both AM and PM, it is used in both.
- A liked treatment can choose the treatment type: if its main active is one of the first concern's actives, that
  active goes first (e.g. a liked niacinamide serum beats the retinoid for breakouts).
- A liked product that fits nowhere (e.g. no longer safe) gets the note "A product you liked isn't in this routine
  because it doesn't fit your current answers."

## Disliked

A disliked product never comes back for that client. The optional reason decides the replacement:

| Reason | Replacement |
|---|---|
| Irritated / broke me out (also the default when no reason is given, the safest choice) | Same step, without the **suspect ingredients** (below) |
| Didn't work | Same step; for a treatment, a **different active** where the concern allows one |
| Texture or smell | Same step, a different texture (rich instead of light, or the other way round) |
| Too pricey | Same step, cheaper |

### Suspect ingredients (for "irritated" dislikes)

1. **First dislike**: the product's tagged irritants (fragrance, fragrance allergens, essential oils, drying
   alcohol, menthol, camphor, sodium lauryl sulfate) and its strong main actives (AHA, BHA, retinoid,
   benzoyl peroxide, L-ascorbic acid; not niacinamide or azelaic acid). A strong active that is only a trace
   in another product (past position 10) doesn't rule that product out.
2. **Two or more dislikes**: also every ingredient found in at least two disliked products and in no liked
   product. Ingredients in more than half of the catalog (water, glycerin, ...) are never suspects: they
   can't tell products apart.

Suspects are excluded from **every** step, not only the step of the disliked product: a reaction to fragrance in
a cleanser is a reaction to fragrance. If that leaves a step with no product, the step stays empty with a note,
rather than suggesting a product with a suspect ingredient. The results page lists what was left out
(`avoidedForYou`, e.g. "Fragrance").

Order of work: disliked products and suspects are removed first, so every safety rule still applies to what is
left; reasons and likes then only reorder or narrow each step's ranked list.

## Similarity (pgvector)

- Each product gets a vector of its ingredients (TF-IDF style): weight = `1 / sqrt(position)` x
  `ln(N / products containing it)`. Earlier = higher concentration; rarity means water and glycerin (in nearly
  everything) weigh ~0, so products look alike only when they share distinctive ingredients. Declared OTC actives
  count as position 1. Stored with pgvector as a `sparsevec` keyed by ingredient ID (migration V10), rebuilt after
  every catalog import.
- Score per product: cosine similarity to the closest liked product minus cosine similarity to the closest disliked
  one, computed in SQL by pgvector.
- Use (author's decision): rules first, then similarity, then price. Similarity is rounded to 0.1, so tiny
  differences count as a tie and price decides. Safety rules and fit (skin type, concerns) are never overridden.
- Checked on the real catalog: liking COSRX's snail essence ranks COSRX's snail cream first (0.87, everything
  else < 0.1); liking CeraVe's cleanser ranks the CeraVe moisturizers first (shared ceramide blend).
- Why pgvector rather than Java: the same search must later scale to the Open Beauty Facts import (thousands of
  products) and to AI embeddings in step 6, and nearest-neighbour search belongs in the database.

## Plan

- **5a** Schema (feedback, stored quiz answers), feedback API, client ID in the routine request.
- **5b** Engine: liked pinning, disliked exclusion, reasons, suspect ingredients; tests.
- **5c** pgvector ingredient vectors and the similarity tie-break.
