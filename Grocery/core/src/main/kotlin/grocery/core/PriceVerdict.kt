package grocery.core

/**
 * "Is this a good week to stock up?" — compares a candidate unit price against
 * (a) the category's historical cheapest-available unit prices, and
 * (b) the user's own last k purchases.
 *
 * With sparse history the percentile is unreliable, so the verdict leans on the
 * last-k comparison first and only trusts percentiles once history is deep enough.
 */
object PriceVerdict {

    const val DEFAULT_K = 5
    private const val MIN_HISTORY_FOR_PERCENTILE = 10
    private const val STOCK_UP_PERCENTILE = 0.20
    private const val WAIT_PERCENTILE = 0.70

    fun assess(
        candidateUnitPrice: Double,
        history: List<PricePointData>,      // category-wide, any product
        purchases: List<PurchaseData>,      // this category, user's own buys
        k: Int = DEFAULT_K,
    ): PriceAssessment {
        val lastK = purchases.sortedByDescending { it.timestampMs }.take(k)
        val beats = lastK.count { candidateUnitPrice < it.unitPrice }
        val cheaperThanLastK = if (lastK.isEmpty()) null else beats

        // Percentile over the cheapest available price per day, so a week with
        // one deep special counts once rather than being drowned by full-price SKUs.
        val dailyCheapest = history
            .groupBy { it.timestampMs / MS_PER_DAY }
            .map { (_, points) -> points.minOf { it.unitPrice } }
        val percentile = if (dailyCheapest.size >= MIN_HISTORY_FOR_PERCENTILE) {
            dailyCheapest.count { it < candidateUnitPrice }.toDouble() / dailyCheapest.size
        } else null

        val verdict = when {
            percentile != null && percentile <= STOCK_UP_PERCENTILE -> Verdict.STOCK_UP
            percentile != null && percentile >= WAIT_PERCENTILE -> Verdict.WAIT
            percentile != null -> Verdict.NORMAL
            lastK.size >= 3 && beats == lastK.size -> Verdict.STOCK_UP
            lastK.size >= 3 && beats == 0 -> Verdict.WAIT
            lastK.isNotEmpty() -> Verdict.NORMAL
            else -> Verdict.NO_DATA
        }
        return PriceAssessment(verdict, percentile, cheaperThanLastK, k)
    }

    private const val MS_PER_DAY = 86_400_000L
}
