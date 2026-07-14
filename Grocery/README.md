# Grocery

Local-first Android app combining a price tracker, pantry run-out projection, and a
weekly meal-plan buy list. No server; all data lives on-device in Room (SQLite).

## Modules

- **`core/`** — pure Kotlin, no Android dependencies. All the decision logic, fully unit-tested:
  - `UnitResolver` — standard conversions (lb→g, cup→ml) plus user-declared rules
    ("6 fillet = 1000 g"), declared once when first needed.
  - `BuyListPlanner` — aggregates needs across recipes + ad-hoc items, subtracts
    on-hand amounts, rounds up to whole packs, keeps per-source attribution.
  - `PriceVerdict` — "stock up / normal / wait" from historical percentile and
    the last-k-purchases comparison (last-k dominates while history is sparse).
  - `RunOutProjector` — run-out date from the average of the last 2–3 purchase gaps,
    or a fixed replacement interval (toothbrush-style).
- **`app/`** — Android app:
  - `data/db` — Room schema (Category, StoreProduct, PricePoint, Purchase,
    ConversionRule, Recipe, plan tables, OnHand, ScrapeConfig).
  - `data/Repository` — bridges Room to core; `logPurchase` is the shared flow that
    feeds both price history and run-out projection.
  - `scrape/` — two mechanisms sharing one persistence path (`PriceFinder`):
    1. **On-demand ("Find prices" button)**: after the plan is finalized, search
       Woolworths + Coles per category, report the cheapest offer (price /
       product / store), and add the top-5 cheapest per store to the
       price-tracking-list (StoreProduct table; deduped by the store's stable
       product ID via the unique (store, storeSku) index — barcodes aren't
       exposed by either API).
    2. **Weekly WorkManager job**: re-runs every enabled ScrapeConfig
       (refreshing tracked products that still rank and admitting new top-N
       entrants), then directly refreshes any remaining tracked product by its
       store product ID (Woolworths `/apis/ui/product/detail/{stockcode}`,
       Coles product data route via the slug captured at scrape time). Results are normalized to canonical units and filtered to the
       dominant unit measure.
    Both `PriceSource` implementations were verified against the live endpoints
    on 2026-07-12 with the probe scripts in `tools/`: Woolworths (POST search
    API; needs a homepage warm-up GET for Akamai cookies) and Coles (Next.js
    data route; needs the buildId scraped from the homepage, sorted
    client-side). Scrape failure is tolerated; manual entry is the reliable path.
  - `ui/` — Compose screens: Plan (pick recipes), Buy list (with verdict chips),
    Explain (attribution breakdown + run-out), Pantry.

## Building

Open this directory in Android Studio (it supplies the Android SDK and Gradle —
no wrapper is committed). To run just the core logic tests:

```
./gradlew :core:test
```

## Status

All v1 features are implemented (none of the Android module has been compiled
yet — first build happens in Android Studio; expect minor mechanical fixes):

- [x] Recipe + ingredient entry (Recipes tab)
- [x] Conversion-rule declaration prompt when the planner hits an unknown unit
- [x] Ad-hoc weekly items on the Plan tab
- [x] Purchase logging + on-hand dialogs on the Pantry tab (the shared entry flow)
- [x] Run-out notifications (daily `RunOutWorker`, warns within 7 days)
- [x] Price history chart + min/now/max on the Explain screen
- [x] "Find prices" on-demand search + weekly refresh (search + direct by product ID)
- [x] Endpoints verified live 2026-07-12 (`tools/*_probe.py`)

First steps on the Android Studio machine: open this folder, let Gradle sync,
run `./gradlew :core:test`, then build the app module and fix whatever the
compiler flags.
