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
   alcohol, menthol, camphor, sodium lauryl sulfate) and its main actives (e.g. AHA, BHA, retinoid,
   benzoyl peroxide, L-ascorbic acid).
2. **Two or more dislikes**: also every ingredient found in at least two disliked products and in no liked
   product. Ingredients in more than half of the catalog (water, glycerin, ...) are never suspects: they
   can't tell products apart.

Suspects are excluded from **every** step, not only the step of the disliked product: a reaction to fragrance in
a cleanser is a reaction to fragrance.

## Similarity (pgvector)

- Each product gets a vector of its ingredients, weighted by position (earlier = higher concentration).
  Stored with pgvector as a sparse vector keyed by ingredient ID, compared by cosine distance.
- Use: among products the rules rank equally, prefer ones similar to the client's liked products and
  dissimilar to the disliked ones. Rules and safety always come first; similarity only breaks ties.
- Why pgvector rather than Java: the same search must later scale to the Open Beauty Facts import (thousands of
  products) and to AI embeddings in step 6, and nearest-neighbour search belongs in the database.

## Plan

- **5a** Schema (feedback, stored quiz answers), feedback API, client ID in the routine request.
- **5b** Engine: liked pinning, disliked exclusion, reasons, suspect ingredients; tests.
- **5c** pgvector ingredient vectors and the similarity tie-break.
