package grocery.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UnitResolverTest {

    private fun resolved(r: UnitResolver.Result): Double =
        (r as UnitResolver.Result.Resolved).amountCanonical

    @Test
    fun sameUnitPassesThrough() {
        val r = UnitResolver.resolve(Quantity(200.0, "g"), "g", emptyList())
        assertEquals(200.0, resolved(r), 1e-9)
    }

    @Test
    fun standardMassConversion() {
        val r = UnitResolver.resolve(Quantity(2.0, "lb"), "g", emptyList())
        assertEquals(907.184, resolved(r), 0.01)
    }

    @Test
    fun standardVolumeWithAliasesAndCanonicalKg() {
        val r = UnitResolver.resolve(Quantity(2.0, "Litres"), "ml", emptyList())
        assertEquals(2000.0, resolved(r), 1e-9)
        val kg = UnitResolver.resolve(Quantity(500.0, "grams"), "kg", emptyList())
        assertEquals(0.5, resolved(kg), 1e-9)
    }

    @Test
    fun declaredRuleSalmonFillets() {
        val rule = ConversionRule(1L, "fillet", 6.0, "g", 1000.0)
        val r = UnitResolver.resolve(Quantity(4.0, "fillet"), "g", listOf(rule))
        assertEquals(666.67, resolved(r), 0.01)
    }

    @Test
    fun declaredRuleChainsThroughStandardConversion() {
        // Rule declared in g, category canonical in kg.
        val rule = ConversionRule(1L, "fillet", 6.0, "g", 1000.0)
        val r = UnitResolver.resolve(Quantity(6.0, "fillet"), "kg", listOf(rule))
        assertEquals(1.0, resolved(r), 1e-9)
    }

    @Test
    fun unknownUnitIsUnresolved() {
        val r = UnitResolver.resolve(Quantity(3.0, "fillet"), "g", emptyList())
        assertTrue(r is UnitResolver.Result.Unresolved)
    }

    @Test
    fun crossFamilyWithoutRuleIsUnresolved() {
        val r = UnitResolver.resolve(Quantity(1.0, "cup"), "g", emptyList())
        assertTrue(r is UnitResolver.Result.Unresolved)
    }
}
