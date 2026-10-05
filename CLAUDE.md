# SkinVidhi: project context for Claude Code

## What this is
A US-first skincare routine builder and portfolio project (no monetization, no affiliate links).
Think "Skyscanner for skincare": brands build routines only from their own lines; SkinVidhi compares
products across every brand.

1. A quick quiz (about a minute): skin type, main concerns, sensitivities, budget, city, and an
   optional skin tone / sun-reaction question (closer to the Fitzpatrick scale than to ethnicity).
   The city gives climate (humidity, UV, pollution), so we do not ask about it.
2. SkinVidhi builds a morning and a night routine from products across all brands, within the budget.
3. For each suggested product the user can say "already tried it: liked / disliked";
   a dislike swaps in a replacement that does the same job without the suspect ingredients.
4. Later: a community section where people post home remedies they tried and others upvote them,
   with moderation and flags for known-harmful remedies.

Not medical advice. Rules and data make safety decisions (ingredient clashes such as retinoids with
AHAs, irritants, sensitivities). AI/ML is used where it solves a real problem:
- vision LLM reads ingredient lists from product images (fills gaps in product data)
- product embeddings in pgvector find replacements
- a ranking model learned from liked/disliked feedback, compared against the rule-based ranking
  on an evaluation set (only once there is enough feedback)
- an LLM explains *why* a routine was chosen, from the rules' output

## Product decisions (made by the author; follow them)
- Routine = "Core 4": AM cleanser, moisturizer, sunscreen; PM cleanser, treatment, moisturizer.
- Treatment is one category; products are told apart by their actives (retinoid, AHA/BHA, vitamin C, ...).
- Vitamin C treatments go in the PM routine only (the rule is for vitamin C treatments; moisturizers with some vitamin C are used AM and PM).
- Moisturizers with built-in SPF are left out of the catalog; moisturizer and sunscreen stay separate steps.
- The quiz is specified in docs/quiz.md (9 questions, ~1 minute). It asks about pregnancy/breastfeeding
  (with "prefer not to say"); "yes" excludes retinoids. Up to 2 concerns, the first is the priority.
  Optional preferences (fragrance-free, no imports, finish) are results-page filters, not quiz questions.
- Products outside the four routine categories stay in the catalog but are never picked for routines.
- A product can have several offers (retailer + size + price); the app shows the cheapest and compares.
- Budget: filter on the upfront total, and also show an estimated monthly cost.
  "Best match within budget": start from each step's best-ranked product and swap to cheaper ones only
  where the budget requires it; cheaper options are shown as alternatives.
- AM and PM cleanser/moisturizer may differ, but a product that fits both equally well is repeated
  (and paid for once).
- Sizes: fl oz -> ml, oz -> g; liquids sold in "oz" count as fl oz; creams, balms and sticks keep oz.
- OTC active ingredients (acne treatments, sunscreens) are stored separately with their percentage.
- Curated catalog: ~25 products per category; the author approves each list. Claude reads ingredients
  from brand pages; everything the author must supply goes into catalog/TO-FILL.md. When all categories
  are done, Claude tells the author to fill it, then waits until the author says it is done.
  Prices Claude cannot read are also supplied by the author. Never bypass bot protection or read
  retailer sites whose terms forbid automated access.

## Data
Free sources only: Open Beauty Facts (imported, weak US coverage), public Shopify product data from
brand stores (respect terms of service and robots.txt), and a hand-curated set of popular US
products with manually maintained prices. Product links are plain links.

## Architecture
- services/core-api: Java 21, Spring Boot 3.5, JPA, Flyway, Redis. Products, ingredients, quiz,
  routine rules engine, replacements, feedback.
- services/ai-service: Python 3.12, FastAPI. Label reading (vision LLM), embeddings, explanations,
  ranking model.
- PostgreSQL 16 + pgvector, Redis, S3 + SQS (LocalStack locally).
- Deployment later: one EC2 t4g.small running Docker Compose, S3/SQS, IAM, CloudWatch,
  all in Terraform, deployed by GitHub Actions via OIDC (no stored AWS keys). Budget is tight: avoid
  NAT gateways, load balancers, ElastiCache, EKS. Kubernetes manifests run on a local kind/minikube cluster.

## Conventions
- Schema changes go in new Flyway migrations (V3__..., never edit a released V migration).
  Curated reference data lives in repeatable R__ migrations.
- Ingredient names are normalized to canonical ingredients via ingredient_aliases.
  Synonyms read on labels are never saved as aliases (see IngredientResolver).
- Ingredient position matters (earlier = higher concentration).
- Every feature gets tests: JUnit (+ Testcontainers for DB), PyTest.
- Keep secrets in .env only; never commit keys.
- Explain *why* each tool is used; no tools added just for the resume.

## Roadmap
1. Foundation: services, local env, CI (done)
2. Ingredient pipeline: Open Beauty Facts import, ingredient normalization (done)
3. US product catalog: curated products, prices, categories (done: 101 products, 136 offers)
4. Quiz + routine rules engine: step templates, clash rules, budget fit, climate from city  <- NEXT
5. Replacements and feedback: "tried it? liked/disliked", pgvector similarity
6. AI: label reading, explanations; ranking model from feedback + evaluation set
7. Frontend (Next.js), AWS deployment
8. Community home remedies

## Author context
The developer is learning Spring Boot, Terraform, AWS, and Kubernetes through this project.
Explain new concepts briefly when introducing them. Work in small steps and check in between them.
