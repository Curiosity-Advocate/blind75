package grocery.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class BuyListPlannerTest {

    private val oil = BuyListPlanner.CategoryInfo(1L, "Cooking oil", "ml")
    private val bottle750 = Pack(10L, 1L, "Woolworths", "Oil 750ml", 750.0, 6.0)
    private val bottle2L = Pack(11L, 1L, "Woolworths", "Oil 2L", 2000.0, 13.0)

    @Test
    fun aggregatesAcrossRecipesSubtractsOnHandRoundsUp() {
        // 200ml recipe 1 + 500ml recipe 2, 100ml at home -> net 600ml -> 1 bottle of 750ml.
        val needs = listOf(
            Need(1L, "Recipe 1", 200.0),
            Need(1L, "Recipe 2", 500.0),
        )
        val lines = BuyListPlanner.plan(listOf(oil), needs, mapOf(1L to 100.0), listOf(bottle750))
        val line = lines.single()
        assertEquals(700.0, line.totalNeeded, 1e-9)
        assertEquals(600.0, line.netNeeded, 1e-9)
        assertEquals(1, line.packsToBuy)
        assertEquals(2, line.breakdown.size)
    }

    @Test
    fun needsExceedingOnePackBuyTwo() {
        val needs = listOf(Need(1L, "Recipe 1", 800.0))
        val line = BuyListPlanner.plan(listOf(oil), needs, emptyMap(), listOf(bottle750)).single()
        assertEquals(2, line.packsToBuy)
    }

    @Test
    fun picksCheaperCoveringPack() {
        // Need 1500ml: 2x750ml = $12 beats 1x2L = $13.
        val needs = listOf(Need(1L, "Recipe 1", 1500.0))
        val line = BuyListPlanner.plan(listOf(oil), needs, emptyMap(), listOf(bottle750, bottle2L)).single()
        assertEquals(10L, assertNotNull(line.chosenPack).storeProductId)
        assertEquals(2, line.packsToBuy)
        // Need 1600ml: 1x2L = $13 beats 3x750ml = $18.
        val line2 = BuyListPlanner.plan(
            listOf(oil), listOf(Need(1L, "R", 1600.0)), emptyMap(), listOf(bottle750, bottle2L),
        ).single()
        assertEquals(11L, assertNotNull(line2.chosenPack).storeProductId)
        assertEquals(1, line2.packsToBuy)
    }

    @Test
    fun fullyCoveredByPantryBuysNothing() {
        val needs = listOf(Need(1L, "Recipe 1", 300.0))
        val line = BuyListPlanner.plan(listOf(oil), needs, mapOf(1L to 500.0), listOf(bottle750)).single()
        assertEquals(0, line.packsToBuy)
        assertEquals(0.0, line.netNeeded, 1e-9)
    }
}
