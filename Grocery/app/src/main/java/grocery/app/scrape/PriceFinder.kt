package grocery.app.scrape

import grocery.app.data.db.Category
import grocery.app.data.db.GroceryDb
import grocery.app.data.db.PricePoint
import grocery.app.data.db.ScrapeConfig
import grocery.app.data.db.StoreProduct
import grocery.core.ConversionRule
import grocery.core.PackNormalizer

/**
 * The on-demand mechanism: after the plan is finalized, search every store for
 * each category, report the cheapest offer (price / product / store), and add
 * the top-N cheapest per store to the price-tracking-list (StoreProduct table,
 * deduped by the store's stable product ID via the unique (store, storeSku)
 * index — neither store exposes barcodes, but Woolworths Stockcode and the
 * Coles product id serve the same purpose).
 *
 * Also used by the weekly ScrapeWorker so both mechanisms share one persistence
 * path.
 */
class PriceFinder(
    private val db: GroceryDb,
    private val sources: List<PriceSource> = listOf(WoolworthsSource(), ColesSource()),
) {

    /** The cheapest offer found for a category, for the "here's your best buy" report. */
    data class BestOffer(
        val categoryId: Long,
        val categoryName: String,
        val store: String,
        val productName: String,
        val price: Double,
        val packSize: Double,          // canonical unit
        val unitPrice: Double,         // price / packSize, canonical — cross-store comparable
    )

    /**
     * Search all stores for each category, persist top-N per store into the
     * tracking list, and return the cheapest offer per category (by normalized
     * unit price). Categories where every store came up empty are absent from
     * the result — the buy list already flags them "Not found".
     */
    suspend fun findAndTrack(categoryIds: Collection<Long>, topN: Int = 5): List<BestOffer> {
        val offers = mutableListOf<BestOffer>()
        for (categoryId in categoryIds) {
            val category = db.categories().byId(categoryId) ?: continue
            val rules = db.rules().byCategory(categoryId).map {
                ConversionRule(it.categoryId, it.fromUnit, it.fromQty, it.toUnit, it.toQty)
            }
            var best: BestOffer? = null
            for (source in sources) {
                ensureConfig(category, source.store, topN)
                val tracked = scrapeAndPersist(source, category, rules, query = category.name, topN = topN)
                val cheapest = tracked.minByOrNull { it.unitPrice } ?: continue
                if (best == null || cheapest.unitPrice < best!!.unitPrice) best = cheapest
            }
            best?.let { offers += it }
        }
        return offers
    }

    /** Make sure the weekly worker will keep refreshing this category at this store. */
    private suspend fun ensureConfig(category: Category, store: String, topN: Int) {
        val exists = db.scrapeConfigs().enabled().any { it.categoryId == category.id && it.store == store }
        if (!exists) {
            db.scrapeConfigs().insert(
                ScrapeConfig(categoryId = category.id, store = store, query = category.name, topN = topN)
            )
        }
    }

    /**
     * One store search: normalize, keep the dominant unit measure, persist the
     * top-N cheapest as tracked products + price points, and also refresh the
     * price of any already-tracked product that showed up in the results even
     * if it fell out of the top-N.
     */
    suspend fun scrapeAndPersist(
        source: PriceSource,
        category: Category,
        rules: List<ConversionRule>,
        query: String,
        topN: Int,
    ): List<BestOffer> {
        val all = try {
            source.search(query, topN = 36)
        } catch (e: Exception) {
            return emptyList()
        }
        // A search can mix unit-price bases (olive oil "100ML" vs spreads "100G").
        val dominantMeasure = all.groupingBy { it.unitMeasure }.eachCount()
            .maxByOrNull { it.value }?.key
        val normalized = all.filter { it.unitMeasure == dominantMeasure }.mapNotNull { r ->
            val packSize = PackNormalizer.canonicalPackSize(
                r.packSizeText, r.price, r.unitPrice, r.unitMeasure, category.canonicalUnit, rules,
            ) ?: return@mapNotNull null
            r to packSize
        }.sortedBy { (r, size) -> r.price / size }

        val now = System.currentTimeMillis()
        val out = mutableListOf<BestOffer>()
        for ((index, entry) in normalized.withIndex()) {
            val (r, packSize) = entry
            val existing = db.products().bySku(source.store, r.sku)
            // Beyond top-N: only refresh products already on the tracking list.
            if (index >= topN && existing == null) continue
            val productId = existing?.id ?: db.products().insert(
                StoreProduct(
                    categoryId = category.id,
                    store = source.store,
                    storeSku = r.sku,
                    name = r.name,
                    packSize = packSize,
                    unitMeasure = r.unitMeasure,
                    refreshKey = r.refreshKey,
                )
            )
            db.prices().insert(PricePoint(0, productId, r.price, now, source = "scrape"))
            if (index < topN) {
                out += BestOffer(
                    categoryId = category.id,
                    categoryName = category.name,
                    store = source.store,
                    productName = r.name,
                    price = r.price,
                    packSize = packSize,
                    unitPrice = r.price / packSize,
                )
            }
        }
        return out
    }
}
