package grocery.app.scrape

/** One product observed in a store search, in a store-agnostic shape. */
data class ScrapedProduct(
    val sku: String,
    val name: String,
    val price: Double,
    val wasPrice: Double?,
    val isOnSpecial: Boolean,
    val packSizeText: String,   // e.g. "24 pack", "750mL"; may be empty
    val unitPrice: Double?,     // store's unit price
    val unitMeasure: String,    // basis, e.g. "100ML", "100ea" — comparisons stay within one measure
    val refreshKey: String = "", // store-specific handle for direct refresh (Coles: URL slug)
)

/**
 * A store's search backend. Implementations hit unofficial endpoints and may
 * break without notice; callers must tolerate failure — manual entry is the
 * reliable path.
 */
interface PriceSource {
    val store: String

    /** Results ordered by unit price ascending, best first. */
    fun search(query: String, topN: Int): List<ScrapedProduct>

    /**
     * Direct product-by-ID refresh for the weekly job — works even when the
     * product no longer matches any search query. [refreshKey] is whatever
     * [ScrapedProduct.refreshKey] held when the product was first tracked.
     */
    fun fetchProduct(sku: String, refreshKey: String): ScrapedProduct?
}
