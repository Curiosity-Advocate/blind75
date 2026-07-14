package grocery.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** Cases taken from live Woolworths/Coles probe data (tools/*_probe.py, 2026-07-12). */
class PackNormalizerTest {

    @Test
    fun colesOliveOilFromPackText() {
        // Coles: "460mL" at $10, category canonical ml.
        val size = PackNormalizer.canonicalPackSize("460mL", 10.0, 2.17, "ml", "ml")
        assertEquals(460.0, size!!, 1e-9)
    }

    @Test
    fun woolworthsBigBottleToMl() {
        // Woolworths: "3.78L" -> 3780 ml.
        val size = PackNormalizer.canonicalPackSize("3.78L", 40.0, 1.06, "100ML", "ml")
        assertEquals(3780.0, size!!, 1e-9)
    }

    @Test
    fun toiletPaperPackToRolls() {
        // Coles: "20 Pack" toilet paper, canonical unit "roll" (count family).
        val size = PackNormalizer.canonicalPackSize("20 Pack", 11.0, 0.31, "100ea", "roll")
        assertEquals(20.0, size!!, 1e-9)
    }

    @Test
    fun emptyPackTextDerivedFromUnitPrice() {
        // Woolworths Essentials 1L had PackageSize "1L", but the Kleenex carton had
        // none: $66.96 at $0.04/1M -> 1674 metres, for a category canonical in "m".
        val size = PackNormalizer.canonicalPackSize("", 66.96, 0.04, "1M", "m")
        assertEquals(1674.0, size!!, 1e-9)
    }

    @Test
    fun derivedPathConvertsFamilies() {
        // No pack text; $8 at $0.56/ea for canonical "roll" -> ~14.3 rolls.
        val size = PackNormalizer.canonicalPackSize("", 8.0, 0.56, "ea", "roll")
        assertEquals(8.0 / 0.56, size!!, 1e-9)
    }

    @Test
    fun measureIn100GramsToCanonicalKg() {
        // $4.50 at $0.90/100G -> 500 g -> 0.5 kg.
        val size = PackNormalizer.canonicalPackSize("", 4.5, 0.9, "100G", "kg")
        assertEquals(0.5, size!!, 1e-9)
    }

    @Test
    fun unresolvableCrossFamilyIsNull() {
        // Metre-based measure can't reach a mass canonical without a rule.
        assertNull(PackNormalizer.canonicalPackSize("", 66.96, 0.04, "1M", "g"))
        // No pack text, no unit price.
        assertNull(PackNormalizer.canonicalPackSize("", 5.0, null, "", "ml"))
    }

    @Test
    fun declaredRuleBridgesFamilies() {
        // User declares 1 roll = 30 m; a metre-priced carton then resolves to rolls.
        val rule = ConversionRule(1L, "m", 30.0, "roll", 1.0)
        val size = PackNormalizer.canonicalPackSize("", 66.96, 0.04, "1M", "roll", listOf(rule))
        assertEquals(1674.0 / 30.0, size!!, 1e-9)
    }
}
