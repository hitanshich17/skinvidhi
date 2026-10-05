# Data to fill in by hand

Sites below block automated reading or don't show the value, so copy it from the page in your browser.
When done, delete the line here. Validate with `cd services/core-api && ./mvnw -q test -Dtest=CatalogFilesTest`.

Conventions: prices as `15.99`; size + unit as on the label (`ml`, `g`, `fl oz`, `oz`; liquids sold in "oz" count
as `fl oz`); ingredients copied exactly; OTC active ingredients go in `actives` as `Name N%; Name N%`.
US sunscreens must list their UV filters in `actives`; imported sunscreens (not FDA-approved) get `yes` in the
last column, `import`.

## Cleansers

- [x] **Cetaphil Gentle Skin Cleanser**: price and size (add a row to offers.csv).
  Page: https://www.cetaphil.com/us/products/product-categories/all-cleansers/cetaphil-gentle-skin-cleanser/302990110227.html
  (no price on the brand site, so use a retailer and set `retailer` to e.g. `target`)
- [x] **La Roche-Posay Toleriane Hydrating Gentle Cleanser**: whole product (row in products.csv and offers.csv).
  Page: https://www.laroche-posay.us/our-products/face/face-wash/toleriane-hydrating-gentle-facial-cleanser-tolerianehydratinggentlefacialcleanser.html
  id: `la-roche-posay-toleriane-hydrating-gentle-cleanser`
- [x] ~~**La Roche-Posay Effaclar Medicated Gel Cleanser**~~: discontinued, dropped.
- [x] **Vanicream Gentle Facial Cleanser**: price and size (not sold on the brand site).
  Page: https://www.vanicream.com/product/vanicream-facial-cleanser
- [x] **Neutrogena Oil-Free Acne Wash**: price and size (the page doesn't include a price).
  Page: https://www.neutrogena.com/products/skincare/oil-free-acne-wash-with-salicylic-acid/6811719
- [x] **Paula's Choice CLEAR Pore Normalizing Cleanser**: ingredients and size (loaded by JavaScript).
  Price on the page: $28. It contains 0.5% salicylic acid; if the label lists it as an active, put it in `actives`.
  Page: https://www.paulaschoice.com/clear-pore-normalizing-cleanser/600.html
  id: `paulas-choice-clear-pore-normalizing-cleanser`
- [x] **Paula's Choice CALM Ultra-Gentle Cleanser**: ingredients and size (loaded by JavaScript). Price on the page: $28.
  Page: https://www.paulaschoice.com/calm-ultra-gentle-cleanser/9190.html
  id: `paulas-choice-calm-ultra-gentle-cleanser`
- [x] **Kiehl's Ultra Facial Cleanser**: whole product (site blocks automated reading).
  Page: https://www.kiehls.com/skincare/face-cleansers-scrubs/ultra-facial-cleanser/714.html
  id: `kiehls-ultra-facial-cleanser`
- [x] **Senka Perfect Whip**: whole product, from the US Walmart listing (no official US store; Japanese
  imports can have a different formula, so use the US listing).
  Page: https://www.walmart.com/ip/Shiseido-Senka-Perfect-Whip-Cream-Face-Wash-4-23-oz/908991070
  id: `senka-perfect-whip`, retailer: `walmart`
- [x] **Drunk Elephant Beste No. 9 Jelly Cleanser**: price + size. The page shows $18.00 without saying which
  size (possibly the travel size), so check it in the browser.
  Page: https://www.drunkelephant.com/beste-no.-9-jelly-cleanser-812343032415.html
- [x] **Fresh Soy Face Cleanser**: whole product (site blocks automated reading).
  Page: https://www.fresh.com/us/skincare/categories/cleansers/soy-face-cleanser-H00006238.html
  id: `fresh-soy-face-cleanser`
- [x] **Youth To The People Superfood Cleanser**: whole product (site blocks automated reading).
  Page: https://www.youthtothepeople.com/skincare/cleansers/superfood-cleanser/YTTP-10100.html
  id: `youth-to-the-people-superfood-cleanser`

## Treatments

- [x] **Differin Adapalene Gel 0.1%**: price and size (offer row, any retailer).
  Page: https://differin.com/shop/differin-gel/3029949.html
- [x] **Neutrogena Rapid Wrinkle Repair Serum**: price and size.
  Page: https://www.neutrogena.com/products/skincare/rapid-wrinkle-repair-serum/6812014

- [x] **Paula's Choice 2% BHA Liquid Exfoliant**: ingredients (loaded by JavaScript) and size. *(Done; size still unknown, optional.)* Price on the page: $37.
  Page: https://www.paulaschoice.com/skin-perfecting-2pct-bha-liquid-exfoliant/201-2010.html
  id: `paulas-choice-2-bha-liquid-exfoliant`
- [x] **Good Molecules Discoloration Correcting Serum**: ingredients (loaded by JavaScript). Then add this offer:
  `good-molecules-discoloration-correcting-serum,brand,12.00,30,ml,https://www.goodmolecules.com/s/good-molecules-discoloration-correcting-serum-30ml,2026-09-23`
  Page: https://www.goodmolecules.com/s/good-molecules-discoloration-correcting-serum-30ml
  id: `good-molecules-discoloration-correcting-serum`
- [x] **Drunk Elephant C-Firma Fresh Day Serum** (28 ml): price.
  Page: https://www.drunkelephant.com/collections/serums/c-firma-fresh-vitamin-c-day-serum-812343034358.html
- [x] **Drunk Elephant A-Passioni Retinol Cream** (30 ml): price.
  Page: https://www.drunkelephant.com/collections/masks/a-passioni-retinol-cream-812343032392.html
- [x] ~~**Sunday Riley Good Genes**~~: dropped (only a grouped, unordered ingredient list is published).
- [x] **Kiehl's Clearly Corrective Dark Spot Solution** (now called Dark Spot Serum): whole product (site blocks automated reading).
  id: `kiehls-clearly-corrective-dark-spot-solution`
- [x] **SkinCeuticals C E Ferulic**: whole product (site blocks automated reading).
  Page: https://www.skinceuticals.com/skincare/vitamin-c-serums/c-e-ferulic-with-15-l-ascorbic-acid/S17.html
  id: `skinceuticals-c-e-ferulic`
- [x] ~~**La Roche-Posay Effaclar Duo**~~: discontinued, dropped.
## Moisturizers

- [x] **CeraVe Moisturizing Cream**: price + size (the page's $14.99 sits next to a "16 oz + 1.89 oz" bundle, so unclear).
  Page: https://www.cerave.com/skincare/moisturizers/moisturizing-cream
- [x] **Cetaphil Moisturizing Cream**: price + size.
  Page: https://www.cetaphil.com/us/products/product-categories/all-moisturizers/moisturizing-cream/302993917564.html
- [x] **Vanicream Daily Facial Moisturizer**: price + size. *(Price done; size still unknown, optional.)*
  Page: https://www.vanicream.com/product/vanicream-daily-facial-moisturizer
- [x] **Neutrogena Hydro Boost Water Gel (Fragrance Free)**: price + size.
  Page: https://www.neutrogena.com/products/skincare/neutrogena-hydro-boost-water-gel-fragrance-free-moisturizer/6806482
- [x] **Aestura Atobarrier 365 Cream**: US price + size (the international store shows prices in JPY).
  Page: https://int.aestura.com/products/atobarrier365-cream  (US retailers: Olive Young US, Amazon)
- [x] ~~**Illiyoon Ceramide Ato Concentrate Cream**~~: dropped (full ingredient list not available).
- [x] **Dr.Althea 345 Relief Cream**: whole product (no readable official store).
  id: `dr-althea-345-relief-cream`
- [x] **Clinique Moisture Surge 100H**: price + size (the page's $89 is the default variant, possibly the jumbo).
  Page: https://www.clinique.com/products/moisture-surge-100h-auto-replenishing-hydrator
- [x] **Drunk Elephant Protini Polypeptide Cream** (50 ml): price.
  Page: https://www.drunkelephant.com/protini-polypeptide-firming-refillable-moisturizer-856556004739.html
- [x] ~~**BYOMA Moisturising Gel Cream**~~: dropped (only a grouped, unordered ingredient list is published).
- [x] **La Roche-Posay Toleriane Double Repair Face Moisturizer** (the version without SPF): whole product (blocked).
  id: `la-roche-posay-toleriane-double-repair-moisturizer`
- [x] **Paula's Choice CLEAR Oil-Free Moisturizer**: whole product (ingredients loaded by JavaScript).
  id: `paulas-choice-clear-oil-free-moisturizer`
- [x] **Kiehl's Ultra Facial Cream**: whole product (blocked). id: `kiehls-ultra-facial-cream`
- [x] **Youth To The People Superfood Air-Whip Moisture Cream**: whole product (blocked).
  id: `youth-to-the-people-superfood-air-whip-moisture-cream`
- [x] **Belif The True Cream Aqua Bomb**: whole product (no readable official store). id: `belif-the-true-cream-aqua-bomb`

## Sunscreens

- [x] **Round Lab Birch Moisturizing Sunscreen UVLock SPF 45** *(done via DailyMed)* (US): the actives percentages. The page names the
  filters (avobenzone, homosalate, octisalate) without %; check the Drug Facts on the page/box. Row ready except actives:
  id `round-lab-birch-moisturizing-sunscreen-uvlock-spf45`, price $24.99,
  page https://roundlab.com/products/birch-moisturizing-uv-sunscreen. Ingredients (copy as-is):
  "WATER, ACRYLATES COPOLYMER, CAPRYLYL METHICONE, SPARASSIS CRISPA EXTRACT, TOCOPHEROL (VITAMIN E), NIACINAMIDE,POLYGLYCERYL-3 DISTEARATE, AVOBENZONE, HOMOSALATE, CALCIUM ALUMINUM BOROSILICATE, 1,2-HEXANEDIOL, OCTISALATE, BUTYLOCTYL SALICYLATE, POLY C10-30 ALKYL ACRYLATE, CETEARYL ALCOHOL, TROMETHAMINE, GLYCERYL STEARATE CITRATE, BETULA PLATYPHYLLA JAPONICA JUICE, ARTEMISIA ANNUA EXTRACT, ANTHEMIS NOBILIS FLOWER OIL, ACRYLATES/C10-30 ALKYL ACRYLATE CROSSPOLYMER, GLYCERIN, BUTYLENE GLYCOL, SODIUM HYALURONATE, CARBOMER, ETHYLHEXYLGLYCERIN, HYALURONIC ACID, GLYCERYL GLUCOSIDE, PROPANEDIOL, PINUS SYLVESTRIS LEAF OIL, ALLANTOIN, PORTULACA OLERACEA EXTRACT, SODIUM STEAROYL GLUTAMATE, GLYCERYL POLYMETHACRYLATE, TRIETHOXYCAPRYLYLSILANE, PENTYLENE GLYCOL, METHYLPROPANEDIOL, BIOSACCHARIDE GUM-1, BENZOTRIAZOLYL DODECYL P-CRESOL, DIETHYLHEXYL 2,6-NAPHTHALATE, POLYMETHYLSILSESQUIOXANE"
- [x] **Round Lab Birch Mild-Up Sunscreen UVLock SPF 50** *(done via DailyMed)* (US, mineral): zinc oxide %. Row ready except actives:
  id `round-lab-birch-mild-up-sunscreen-uvlock-spf50`, price $24.99,
  page https://roundlab.com/products/birch-juice-mild-up-uvlock-sunscreen. Ingredients (copy as-is):
  "WATER, ZINC OXIDE, ISODODECANE, CAPRYLYL METHICONE, POLYGLYCERY-3 POLYDIMETHYLSILFOXYETHYL DIMETHICONE, METHYL TRIMETHICONE, DISTEARDIMONIUM HECTORITE, SODIUM HYALURONATE, HYALURONIC ACID, BETULA PLATYPHYLLA JAPONICA JUICE, ARTEMISIA ANNUA EXTRACT, METHYL METHACRYLATE CROSSPOLYMER, GLYCERIN, BUTYLENE GLYCOL, PROPANEDIOL, GLYCERYL GLUCOSIDE, MAGNESIUM SULFATE, TRIETHOXYCAPRYLYLSILANE, ANTHEMIS NOBILIS FLOWER OIL, PINUS SYLVESTRIS LEAF OIL, SACCHAROMYCES FERMENT FILTRATE, ETHYLHEXYLGLYCERIN, CAPRYLYL GLYCOL, GLYCERYL CAPRYLATE, BUTYLOCTYL SALICYLATE, POLYMETHYLSILSESQUIOXANE, CYCLOHEXASILOXANE, 1,2-HEXANEDIOL, LAURYL POLYGLYCERYL-3 POLYDIMETHYILSOXYETHYL DIMETHICONE, CRYPTOMERIA JAPONICA LEAF EXTRACT, TOCOPHEROL (VITAMIN E), ASCORBIC ACID (VITAMIN C)"
- [x] **SKIN1004 Hyalu-Cica Water-Fit Sun Serum UV** (US version): inactive ingredients (not in the page).
  Actives known: `Avobenzone 2.7%; Homosalate 13.6%; Octisalate 4.5%; Octocrylene 9%`. Price known: $15.20 / 50 ml.
  Page: https://www.skin1004.com/products/hyalu-cica-water-fit-sun-serum-uv
  id: `skin1004-hyalu-cica-water-fit-sun-serum-uv`
- [x] **Aestura Derma UV365 Barrier Hydro Mineral Sunscreen** (import): US price + size (int. store shows $0).
- [x] **Beauty of Joseon Relief Sun: Rice + Probiotics** (import): whole product; the US store page is 404, so check
  Olive Young US / Amazon. Set `import` to `yes`. id: `beauty-of-joseon-relief-sun`
- [x] **SKIN1004 Madagascar Centella Air-Fit Suncream Plus** (import): whole product (US store page is 404).
  id: `skin1004-centella-air-fit-suncream-plus`
- [x] **Torriden DIVE-IN Mild Sun Cream** (import): whole product (not on Torriden's US store). id: `torriden-dive-in-mild-suncream`
- [x] **EltaMD UV Daily SPF 40**: price + size. Page: https://eltamd.com/products/uv-daily-broad-spectrum-spf-40
- [x] **Supergoop! Unseen Sunscreen SPF 50**: inactive ingredients (not in the page).
  Actives known: `Avobenzone 3%; Homosalate 7%; Octisalate 5%; Octocrylene 9%`. id: `supergoop-unseen-sunscreen-spf50`.
  Offers ready: `supergoop-unseen-sunscreen-spf50,brand,19.00,0.68,fl oz,https://supergoop.com/products/unseen-sunscreen-spf-50,2026-09-23`,
  `...,brand,38.00,1.7,fl oz,...`, `...,brand,48.00,2.5,fl oz,...` (same link and date)
- [x] **Supergoop! Mineral Mattescreen SPF 40** (untinted): inactive ingredients (not in the page).
  Actives known: `Titanium Dioxide 1.33%; Zinc Oxide 17.42%`. id: `supergoop-mineral-mattescreen-spf40`.
  Offer ready: `supergoop-mineral-mattescreen-spf40,brand,40.00,1.5,fl oz,https://supergoop.com/products/smooth-and-poreless-mattescreen,2026-09-23`
- [x] ~~**d'Alba Waterfull Essence Sunscreen SPF 50+**~~: dropped (published list has no UV filters; no US label).
- [x] **Black Girl Sunscreen SPF 30**: **price + size only** (product added from its DailyMed label). id `black-girl-sunscreen-spf30`
- [x] **Unsun Mineral Tinted Face Sunscreen SPF 30**: **price + size only** (product added from its DailyMed label). id `unsun-mineral-tinted-spf30`
- [x] **Isntree Hyaluronic Acid Watery Sun Gel** (import): whole product. id: `isntree-hyaluronic-acid-watery-sun-gel`
- [x] **ISDIN Eryfotona Actinica** (1.7 fl oz page): price. Page: https://www.isdin.com/us/p/actinica-17-fl-oz/4266
- [x] **Neutrogena Ultra Sheer Dry-Touch SPF 55**: price + size.
  Page: https://www.neutrogena.com/products/sun/ultra-sheer-dry-touch-sunscreen-broad-spectrum-spf-55/6868790
- [x] **Bioré UV Aqua Rich Hydrating Facial Moisturizer SPF 50** (US): inactive ingredients (not in the page).
  Actives known: `Avobenzone 3%; Homosalate 10%; Octisalate 5%; Octocrylene 5%`. Price known: $15.99.
  Page: https://us.biore.com/products/biore-sunscreen-spf-50  id: `biore-uv-aqua-rich-spf50`
- [x] **Dr.G Green Mild Up Defense Mineral Sunscreen** *(done via DailyMed)* (US OTC version): actives % and ingredients. Price known: $29 / 50 ml.
  Page: https://www.dr-g.com/products/dr-g-green-mild-up-sun-50ml-%EC%82%AC%EB%B3%B8  id: `dr-g-green-mild-up-defense-mineral-sunscreen`
- [x] **Drunk Elephant Umbra Sheer Mineral Cream SPF 30** *(done via DailyMed)*: actives % and ingredients. Price on the page: $42.
  Page: https://www.drunkelephant.com/collections/sun-protection/umbra-sheer-mineral-cream-spf-30pa-812343032804.html
  id: `drunk-elephant-umbra-sheer-spf30`
- [x] **La Roche-Posay Anthelios Melt-in Milk SPF 60**: **price + size only** (product added from its DailyMed label). id `la-roche-posay-anthelios-melt-in-milk-spf60`
- [x] **La Roche-Posay Anthelios Mineral Tinted SPF 50**: **price + size only** (product added from its DailyMed label). id `la-roche-posay-anthelios-mineral-tinted-spf50`
- [x] ~~**Aveeno Protect + Soothe Mineral Sunscreen Stick SPF 50**~~: replaced by Aveeno Protect + Hydrate Sunscreen Lotion SPF 60.

## Reopened

- [ ] **Aestura Derma UV365 Barrier Hydro Mineral Sunscreen**: US price + size. The Amazon link given earlier
  (B09JDXC8PM) turned out to be SKIN1004 Air-Fit Suncream Plus, so that offer moved there.
