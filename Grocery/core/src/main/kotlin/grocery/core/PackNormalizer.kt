package grocery.core

/**
 * Normalizes a scraped store product into a pack size expressed in the
 * category's canonical unit, so unit prices are comparable across stores
 * regardless of each store's own unit-price basis ("100ML" vs "ml" vs "1M").
 *
 * Resolution order:
 *  1. Parse the pack-size text ("460mL", "1.4kg", "24 pack") and convert to
 *     the canonical unit via [UnitResolver] (standard + declared rules).
 *  2. If the text is missing/unparseable, derive from the store's unit price:
 *     packSize = price / unitPrice * measureQuantity, in the measure's unit
 *     ("$66.96 at $0.04/1M" -> 1674 M), then convert to canonical the same way.
 */
object PackNormalizer {

    private val packPattern = Regex(
        """([\d.]+)\s*(kg|g|mg|l|ml|litre[s]?|liter[s]?|pack|pk|each|ea|roll[s]?|sheet[s]?|piece[s]?)\b""",
        RegexOption.IGNORE_CASE,
    )

    // Store measure strings: "100ML", "100G", "1M", "ea", "100ea", "1kg".
    private val measurePattern = Regex("""^([\d.]*)\s*([a-zA-Z]+)$""")

    /** Pack-size text -> quantity in whatever unit the text uses, or null. */
    fun parsePackText(text: String): Quantity? {
        val m = packPattern.find(text) ?: return null
        val amount = m.groupValues[1].toDoubleOrNull() ?: return null
        val unit = when (m.groupValues[2].lowercase().trimEnd('s')) {
            "pack", "pk", "ea", "piece" -> "each"
            "litre", "liter" -> "l"
            else -> m.groupValues[2].lowercase()
        }
        return Quantity(amount, unit)
    }

    /** Measure string like "100ML" or "ea" -> Quantity(100.0, "ml") / Quantity(1.0, "each"). */
    fun parseMeasure(measure: String): Quantity? {
        val m = measurePattern.find(measure.trim()) ?: return null
        val amount = m.groupValues[1].toDoubleOrNull() ?: 1.0
        val unit = when (val u = m.groupValues[2].lowercase()) {
            "ea" -> "each"
            else -> u
        }
        return Quantity(amount, unit)
    }

    /**
     * Best-effort pack size in [canonicalUnit], or null if neither path resolves.
     * [rules] are the category's declared conversions (e.g. "1 roll = 30 m").
     */
    fun canonicalPackSize(
        packSizeText: String,
        price: Double,
        unitPrice: Double?,
        unitMeasure: String,
        canonicalUnit: String,
        rules: List<ConversionRule> = emptyList(),
    ): Double? {
        parsePackText(packSizeText)?.let { qty ->
            val r = UnitResolver.resolve(qty, canonicalUnit, rules)
            if (r is UnitResolver.Result.Resolved) return r.amountCanonical
        }
        if (unitPrice != null && unitPrice > 0 && price > 0) {
            val measure = parseMeasure(unitMeasure) ?: return null
            val amountInMeasureUnit = price / unitPrice * measure.amount
            val r = UnitResolver.resolve(Quantity(amountInMeasureUnit, measure.unit), canonicalUnit, rules)
            if (r is UnitResolver.Result.Resolved) return r.amountCanonical
        }
        return null
    }
}
