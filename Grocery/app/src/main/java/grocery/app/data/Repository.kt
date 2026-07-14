package grocery.app.data

import grocery.app.data.db.GroceryDb
import grocery.app.data.db.PricePoint
import grocery.app.data.db.Purchase
import grocery.core.BuyListPlanner
import grocery.core.BuyLine
import grocery.core.ConversionRule
import grocery.core.Need
import grocery.core.Pack
import grocery.core.PriceAssessment
import grocery.core.PricePointData
import grocery.core.PriceVerdict
import grocery.core.PurchaseData
import grocery.core.Quantity
import grocery.core.RunOutProjector
import grocery.core.UnitResolver

/**
 * Bridges Room entities to the pure-Kotlin core. All decisions (verdicts,
 * buy list, run-out) are computed here from stored data, never stored.
 */
class Repository(private val db: GroceryDb) {

    private companion object {
        const val MS_PER_DAY = 86_400_000L
    }

    /** The shared entry flow: one purchase feeds price history and run-out projection. */
    suspend fun logPurchase(storeProductId: Long, price: Double, quantity: Int) {
        val now = System.currentTimeMillis()
        db.purchases().insert(Purchase(0, storeProductId, price, quantity, now))
        db.prices().insert(PricePoint(0, storeProductId, price, now, source = "manual"))
    }

    suspend fun assessPrice(categoryId: Long, candidateUnitPrice: Double): PriceAssessment {
        val products = db.products().byCategory(categoryId).associateBy { it.id }
        val history = db.prices().historyForCategory(categoryId).mapNotNull { pp ->
            val size = products[pp.storeProductId]?.packSize ?: return@mapNotNull null
            if (size <= 0) return@mapNotNull null
            PricePointData(pp.storeProductId, pp.price / size, pp.timestampMs)
        }
        val purchases = db.purchases().byCategory(categoryId).mapNotNull { pu ->
            val size = products[pu.storeProductId]?.packSize ?: return@mapNotNull null
            if (size <= 0) return@mapNotNull null
            PurchaseData(categoryId, pu.price / size, pu.timestampMs)
        }
        return PriceVerdict.assess(candidateUnitPrice, history, purchases)
    }

    /**
     * Daily cheapest unit price for a category (canonical unit), for the price
     * history chart. One point per day so a special counts once.
     */
    suspend fun categoryUnitPriceHistory(categoryId: Long): List<Pair<Long, Double>> {
        val products = db.products().byCategory(categoryId).associateBy { it.id }
        return db.prices().historyForCategory(categoryId).mapNotNull { pp ->
            val size = products[pp.storeProductId]?.packSize ?: return@mapNotNull null
            if (size <= 0) return@mapNotNull null
            pp.timestampMs to pp.price / size
        }.groupBy { it.first / MS_PER_DAY }
            .map { (day, points) -> day * MS_PER_DAY to points.minOf { it.second } }
            .sortedBy { it.first }
    }

    suspend fun projectedRunOutMs(categoryId: Long): Long? {
        val category = db.categories().byId(categoryId) ?: return null
        val timestamps = db.purchases().byCategory(categoryId).map { it.timestampMs }
        return RunOutProjector.projectRunOut(timestamps, category.fixedIntervalDays)
    }

    sealed interface PlanResult {
        data class Ok(val lines: List<BuyLine>) : PlanResult
        /** A recipe used a unit with no standard or declared conversion — prompt the user. */
        data class NeedsRule(val categoryId: Long, val categoryName: String, val unit: String) : PlanResult
    }

    /** Aggregate the current plan (recipes + ad-hoc items) into a buy list. */
    suspend fun buildBuyList(
        planRecipeIds: List<Long>,
        adhoc: List<Triple<Long, Double, String>>, // categoryId, amount, unit
    ): PlanResult {
        val needs = mutableListOf<Need>()
        val categoryIds = mutableSetOf<Long>()

        suspend fun resolveOrFail(categoryId: Long, amount: Double, unit: String, label: String): Need? {
            val category = db.categories().byId(categoryId) ?: return null
            val rules = db.rules().byCategory(categoryId).map {
                ConversionRule(it.categoryId, it.fromUnit, it.fromQty, it.toUnit, it.toQty)
            }
            return when (val r = UnitResolver.resolve(Quantity(amount, unit), category.canonicalUnit, rules)) {
                is UnitResolver.Result.Resolved -> Need(categoryId, label, r.amountCanonical)
                is UnitResolver.Result.Unresolved -> null
            }
        }

        for (recipeId in planRecipeIds) {
            for (ing in db.recipes().ingredients(recipeId)) {
                val category = db.categories().byId(ing.categoryId) ?: continue
                val need = resolveOrFail(ing.categoryId, ing.amount, ing.unit, "Recipe #$recipeId")
                    ?: return PlanResult.NeedsRule(ing.categoryId, category.name, ing.unit)
                needs += need
                categoryIds += ing.categoryId
            }
        }
        for ((categoryId, amount, unit) in adhoc) {
            val category = db.categories().byId(categoryId) ?: continue
            val need = resolveOrFail(categoryId, amount, unit, "Ad-hoc")
                ?: return PlanResult.NeedsRule(categoryId, category.name, unit)
            needs += need
            categoryIds += categoryId
        }

        val infos = categoryIds.mapNotNull { id ->
            db.categories().byId(id)?.let { BuyListPlanner.CategoryInfo(it.id, it.name, it.canonicalUnit) }
        }
        val packs = categoryIds.flatMap { id ->
            db.products().byCategory(id).map { sp ->
                Pack(sp.id, sp.categoryId, sp.store, sp.name, sp.packSize,
                    db.prices().latestForProduct(sp.id)?.price)
            }
        }
        val onHand = db.onHand().all().associate { it.categoryId to it.amountCanonical }
        return PlanResult.Ok(BuyListPlanner.plan(infos, needs, onHand, packs))
    }
}
