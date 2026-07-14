package grocery.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RunOutProjectorTest {

    private val day = 86_400_000L

    @Test
    fun averagesLastThreeGaps() {
        // Purchases at days 0, 10, 24, 36, 50 -> last 3 gaps: 14, 12, 14 -> avg ~13.33.
        val ts = listOf(0L, 10L, 24L, 36L, 50L).map { it * day }
        val projected = RunOutProjector.projectRunOut(ts)!!
        assertEquals(50 * day + (40 * day / 3), projected)
    }

    @Test
    fun fixedIntervalIgnoresGaps() {
        val ts = listOf(0L, 3L, 100L).map { it * day }
        val projected = RunOutProjector.projectRunOut(ts, fixedIntervalDays = 180)
        assertEquals(100 * day + 180 * day, projected)
    }

    @Test
    fun singlePurchaseWithoutFixedIntervalIsNull() {
        assertNull(RunOutProjector.projectRunOut(listOf(5 * day)))
        assertNull(RunOutProjector.projectRunOut(emptyList()))
    }
}
