package grocery.app.scrape

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import grocery.app.data.db.GroceryDb
import grocery.app.data.db.PricePoint
import grocery.core.ConversionRule
import java.util.concurrent.TimeUnit

/**
 * The weekly mechanism: re-run every enabled ScrapeConfig and refresh prices
 * for the price-tracking-list. Shares PriceFinder's persistence path, which
 * both refreshes already-tracked products found in the results and adds any
 * new top-N cheapest entrants.
 */
class ScrapeWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val db = GroceryDb.get(applicationContext)
        val finder = PriceFinder(db)
        val sources = listOf(WoolworthsSource(), ColesSource()).associateBy { it.store }
        var anyFailure = false

        // Phase 1: re-run each category search — refreshes tracked products that
        // still rank and admits new top-N cheapest entrants.
        for (config in db.scrapeConfigs().enabled()) {
            val source = sources[config.store] ?: continue
            val category = db.categories().byId(config.categoryId) ?: continue
            val rules = db.rules().byCategory(config.categoryId).map {
                ConversionRule(it.categoryId, it.fromUnit, it.fromQty, it.toUnit, it.toQty)
            }
            val tracked = finder.scrapeAndPersist(source, category, rules, config.query, config.topN)
            if (tracked.isEmpty()) anyFailure = true
        }

        // Phase 2: direct product-by-ID refresh for every tracked product the
        // searches didn't just cover — so items that stop matching their search
        // query keep their price history going.
        val now = System.currentTimeMillis()
        for (product in db.products().all()) {
            val fresh = db.prices().latestForProduct(product.id)
                ?.let { now - it.timestampMs < RECENT_MS } == true
            if (fresh) continue
            val source = sources[product.store] ?: continue
            val fetched = try {
                source.fetchProduct(product.storeSku, product.refreshKey)
            } catch (e: Exception) {
                null
            } ?: continue // unavailable/delisted products just skip this week
            db.prices().insert(PricePoint(0, product.id, fetched.price, now, source = "scrape"))
        }
        return if (anyFailure) Result.retry() else Result.success()
    }

    companion object {
        /** Skip direct refresh if a search already priced the product this run (or recently). */
        private const val RECENT_MS = 12 * 60 * 60 * 1000L

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<ScrapeWorker>(7, TimeUnit.DAYS).build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "price-scrape", ExistingPeriodicWorkPolicy.UPDATE, request,
            )
        }
    }
}
