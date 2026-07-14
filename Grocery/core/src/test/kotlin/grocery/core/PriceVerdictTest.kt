package grocery.core

import kotlin.test.Test
import kotlin.test.assertEquals

class PriceVerdictTest {

    private val day = 86_400_000L

    private fun history(vararg unitPrices: Double): List<PricePointData> =
        unitPrices.mapIndexed { i, p -> PricePointData(1L, p, i * day) }

    private fun purchases(vararg unitPrices: Double): List<PurchaseData> =
        unitPrices.mapIndexed { i, p -> PurchaseData(1L, p, i * day) }

    @Test
    fun noDataVerdict() {
        val a = PriceVerdict.assess(5.0, emptyList(), emptyList())
        assertEquals(Verdict.NO_DATA, a.verdict)
    }

    @Test
    fun deepHistoryPercentileStockUp() {
        val h = history(4.0, 4.2, 4.5, 4.5, 4.8, 5.0, 5.0, 5.2, 5.5, 6.0)
        val a = PriceVerdict.assess(4.1, h, emptyList())
        assertEquals(Verdict.STOCK_UP, a.verdict)
        assertEquals(0.1, a.percentile!!, 1e-9)
    }

    @Test
    fun deepHistoryPercentileWait() {
        val h = history(4.0, 4.2, 4.5, 4.5, 4.8, 5.0, 5.0, 5.2, 5.5, 6.0)
        val a = PriceVerdict.assess(5.9, h, emptyList())
        assertEquals(Verdict.WAIT, a.verdict)
    }

    @Test
    fun sparseHistoryFallsBackToLastKPurchases() {
        // Only 3 history days (< 10) but candidate beats all 3 recent buys.
        val a = PriceVerdict.assess(3.5, history(4.0, 4.5, 5.0), purchases(4.0, 4.2, 4.8))
        assertEquals(Verdict.STOCK_UP, a.verdict)
        assertEquals(3, a.cheaperThanLastK)
    }

    @Test
    fun beatsNoRecentBuysIsWait() {
        val a = PriceVerdict.assess(6.0, emptyList(), purchases(4.0, 4.2, 4.8))
        assertEquals(Verdict.WAIT, a.verdict)
        assertEquals(0, a.cheaperThanLastK)
    }

    @Test
    fun sameDayPricesCollapseToDailyCheapest() {
        // 20 points across 2 days must not satisfy the 10-day minimum.
        val h = (0 until 20).map { PricePointData(1L, 5.0 + it, (it % 2) * day) }
        val a = PriceVerdict.assess(5.0, h, emptyList())
        assertEquals(null, a.percentile)
    }
}
