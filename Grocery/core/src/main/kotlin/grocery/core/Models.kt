package grocery.core

/**
 * A quantity in some unit as written by a human ("6 fillet", "1.5 lb", "200 ml").
 * Units are free-form strings; [UnitResolver] turns them into a category's canonical unit.
 */
data class Quantity(val amount: Double, val unit: String)

/** Canonical unit families. Every category picks one canonical unit. */
enum class UnitKind { MASS, VOLUME, COUNT }

/**
 * Item-specific equivalence declared once by the user,
 * e.g. salmon: 6 "fillet" = 1000 "g".
 */
data class ConversionRule(
    val categoryId: Long,
    val fromUnit: String,
    val fromQty: Double,
    val toUnit: String,
    val toQty: Double,
)

/** A purchasable pack at a store, with its size in the category's canonical unit. */
data class Pack(
    val storeProductId: Long,
    val categoryId: Long,
    val store: String,
    val name: String,
    val packSize: Double,   // in canonical unit
    val currentPrice: Double?,
)

/** One source of demand for a category: a recipe line or an ad-hoc item. */
data class Need(
    val categoryId: Long,
    val sourceLabel: String,   // "Recipe: Salmon bowl" or "Ad-hoc"
    val amountCanonical: Double,
)

/** A line on the buy list, with full attribution for the explain screen. */
data class BuyLine(
    val categoryId: Long,
    val categoryName: String,
    val canonicalUnit: String,
    val totalNeeded: Double,
    val onHand: Double,
    val netNeeded: Double,
    val chosenPack: Pack?,
    val packsToBuy: Int,
    val breakdown: List<Need>,
) {
    /** Needed but no store product is known for it — keep it on the list, flagged. */
    val notFound: Boolean get() = netNeeded > 0 && chosenPack == null
}

data class PricePointData(val storeProductId: Long, val unitPrice: Double, val timestampMs: Long)
data class PurchaseData(val categoryId: Long, val unitPrice: Double, val timestampMs: Long)

enum class Verdict { STOCK_UP, NORMAL, WAIT, NO_DATA }

data class PriceAssessment(
    val verdict: Verdict,
    val percentile: Double?,        // 0.0 = cheapest ever, 1.0 = most expensive
    val cheaperThanLastK: Int?,     // how many of the last k purchases this beats
    val k: Int,
)
