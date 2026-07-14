package grocery.core

import kotlin.math.ceil

/**
 * Aggregates needs across recipes and ad-hoc items, subtracts what's on hand,
 * and rounds up to whole purchasable packs — keeping per-source attribution
 * for the explain screen.
 */
object BuyListPlanner {

    data class CategoryInfo(val categoryId: Long, val name: String, val canonicalUnit: String)

    fun plan(
        categories: List<CategoryInfo>,
        needs: List<Need>,
        onHand: Map<Long, Double>,          // categoryId -> amount in canonical unit
        packs: List<Pack>,
    ): List<BuyLine> {
        val byCategory = needs.groupBy { it.categoryId }
        return byCategory.mapNotNull { (categoryId, catNeeds) ->
            val info = categories.find { it.categoryId == categoryId } ?: return@mapNotNull null
            val total = catNeeds.sumOf { it.amountCanonical }
            val have = onHand[categoryId] ?: 0.0
            val net = (total - have).coerceAtLeast(0.0)

            val pack = cheapestCovering(packs.filter { it.categoryId == categoryId }, net)
            val count = when {
                net <= 0.0 -> 0
                pack != null -> ceil(net / pack.packSize).toInt()
                else -> 0
            }
            BuyLine(
                categoryId = categoryId,
                categoryName = info.name,
                canonicalUnit = info.canonicalUnit,
                totalNeeded = total,
                onHand = have,
                netNeeded = net,
                chosenPack = pack,
                packsToBuy = count,
                breakdown = catNeeds,
            )
        }.sortedBy { it.categoryName }
    }

    /**
     * Cheapest way to cover [net] with whole packs of a single product.
     * (Mixing pack sizes of the same category is left for a later version.)
     */
    private fun cheapestCovering(candidates: List<Pack>, net: Double): Pack? {
        val priced = candidates.filter { it.currentPrice != null && it.packSize > 0 }
        if (priced.isEmpty()) return candidates.firstOrNull()
        if (net <= 0.0) return priced.minByOrNull { it.currentPrice!! / it.packSize }
        return priced.minByOrNull { ceil(net / it.packSize) * it.currentPrice!! }
    }
}
