package grocery.core

/**
 * Projects when an item runs out from the gaps between its recent purchases
 * (last 2–3 gaps averaged). Fixed-interval items (toothbrush every 6 months)
 * pass a fixedIntervalDays instead and skip the gap math.
 */
object RunOutProjector {

    private const val MS_PER_DAY = 86_400_000L
    private const val MAX_GAPS = 3

    /** Returns projected run-out timestamp in ms, or null if not enough data. */
    fun projectRunOut(
        purchaseTimestampsMs: List<Long>,
        fixedIntervalDays: Int? = null,
    ): Long? {
        val sorted = purchaseTimestampsMs.sorted()
        val last = sorted.lastOrNull() ?: return null

        if (fixedIntervalDays != null) return last + fixedIntervalDays * MS_PER_DAY

        if (sorted.size < 2) return null
        val gaps = sorted.zipWithNext { a, b -> b - a }.takeLast(MAX_GAPS)
        return last + gaps.average().toLong()
    }
}
