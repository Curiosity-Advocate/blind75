package grocery.app.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** Canonical item family, e.g. "toilet paper" (unit: roll), "cooking oil" (unit: ml). */
@Entity
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val canonicalUnit: String,          // "g", "ml", "each", "roll"...
    val fixedIntervalDays: Int? = null, // toothbrush-style replacement window
    val colorArgb: Long? = null,        // user-chosen label colour
)

/** A specific SKU at a specific store. Created manually or by the scraper. */
@Entity(indices = [Index("categoryId"), Index(value = ["store", "storeSku"], unique = true)])
data class StoreProduct(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val categoryId: Long,
    val store: String,                  // "Woolworths", "Coles"
    val storeSku: String,               // store's product id; stable key for scrape history
    val name: String,
    val packSize: Double,               // in the category's canonical unit
    val unitMeasure: String = "",       // store's unit-price basis, e.g. "100ML"; comparisons stay within one measure
    val refreshKey: String = "",        // store-specific handle for direct price refresh (Coles: URL slug)
)

/** Append-only price observation (manual entry or scrape). */
@Entity(indices = [Index("storeProductId"), Index("timestampMs")])
data class PricePoint(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val storeProductId: Long,
    val price: Double,
    val timestampMs: Long,
    val source: String,                 // "manual" | "scrape"
)

/** The shared flow: one purchase updates price history AND run-out projection. */
@Entity(indices = [Index("storeProductId"), Index("timestampMs")])
data class Purchase(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val storeProductId: Long,
    val price: Double,
    val quantity: Int,
    val timestampMs: Long,
)

/** User-declared equivalence, e.g. salmon: 6 fillet = 1000 g. */
@Entity(indices = [Index("categoryId")])
data class ConversionRuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val categoryId: Long,
    val fromUnit: String,
    val fromQty: Double,
    val toUnit: String,
    val toQty: Double,
)

@Entity
data class Recipe(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val servings: Int = 1,
    val country: String = "",           // cuisine / country of origin
    val sourceUrl: String = "",         // website the recipe came from
    val createdAtMs: Long = 0,
)

@Entity(indices = [Index("recipeId"), Index("categoryId")])
data class RecipeIngredient(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val recipeId: Long,
    val categoryId: Long,
    val amount: Double,
    val unit: String,                   // as written; resolved via rules at plan time
)

/** This week's plan: selected recipes + ad-hoc items (veggies, fruit...). */
@Entity
data class PlanRecipe(@PrimaryKey val recipeId: Long)

@Entity(indices = [Index("categoryId")])
data class PlanAdhocItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val categoryId: Long,
    val amount: Double,
    val unit: String,
)

/** Rough pantry state: amount on hand in canonical unit (manual, optional). */
@Entity
data class OnHand(
    @PrimaryKey val categoryId: Long,
    val amountCanonical: Double,
)

/**
 * User edits layered over the computed buy list: bump the pack count or hide a
 * row entirely. Computed values are never mutated; clearing overrides restores them.
 */
@Entity
data class BuyOverride(
    @PrimaryKey val categoryId: Long,
    val packsOverride: Int? = null,
    val hidden: Boolean = false,
    val note: String = "",              // free text; shown on the list and included in shares
)

/** Scrape config: search this store for a query, track the top-N cheapest by unit price. */
@Entity(indices = [Index("categoryId")])
data class ScrapeConfig(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val categoryId: Long,
    val store: String,
    val query: String,
    val topN: Int = 5,
    val enabled: Boolean = true,
)
