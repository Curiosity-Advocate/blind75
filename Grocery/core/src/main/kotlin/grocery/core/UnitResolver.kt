package grocery.core

/**
 * Resolves human-written quantities to a category's canonical unit.
 *
 * Resolution order:
 *  1. Standard unit conversions (lb->g, cup->ml, ...), built in.
 *  2. User-declared [ConversionRule]s for the category ("6 fillet = 1000 g").
 *  3. Failure -> [Unresolved]; the UI prompts the user to declare a rule once.
 */
object UnitResolver {

    sealed interface Result {
        data class Resolved(val amountCanonical: Double) : Result
        data class Unresolved(val unit: String) : Result
    }

    // Canonical units per family: g, ml, each.
    private val standard: Map<String, Pair<UnitKind, Double>> = mapOf(
        "g" to (UnitKind.MASS to 1.0),
        "kg" to (UnitKind.MASS to 1000.0),
        "mg" to (UnitKind.MASS to 0.001),
        "lb" to (UnitKind.MASS to 453.592),
        "oz" to (UnitKind.MASS to 28.3495),
        "ml" to (UnitKind.VOLUME to 1.0),
        "l" to (UnitKind.VOLUME to 1000.0),
        "cup" to (UnitKind.VOLUME to 250.0),   // AU metric cup
        "tbsp" to (UnitKind.VOLUME to 20.0),   // AU tablespoon
        "tsp" to (UnitKind.VOLUME to 5.0),
        "each" to (UnitKind.COUNT to 1.0),
        "dozen" to (UnitKind.COUNT to 12.0),
    )

    /** Read-only view of the built-in units, for display in a UI. */
    fun builtInUnits(): Map<String, Pair<UnitKind, Double>> = standard

    private fun norm(unit: String) = unit.trim().lowercase().removeSuffix(".").let {
        when (it) {
            "grams", "gram" -> "g"
            "kilogram", "kilograms", "kgs" -> "kg"
            "pound", "pounds", "lbs" -> "lb"
            "ounce", "ounces" -> "oz"
            "litre", "liter", "litres", "liters" -> "l"
            "millilitre", "milliliter", "mls" -> "ml"
            "cups" -> "cup"
            "tablespoon", "tablespoons" -> "tbsp"
            "teaspoon", "teaspoons" -> "tsp"
            "unit", "units", "piece", "pieces", "whole" -> "each"
            // Count-like retail units: a "24 pack" of toilet paper is 24 rolls.
            "roll", "rolls", "pack", "pk", "ea" -> "each"
            else -> it
        }
    }

    fun resolve(
        qty: Quantity,
        canonicalUnit: String,
        rules: List<ConversionRule>,
    ): Result {
        val from = norm(qty.unit)
        val canonical = norm(canonicalUnit)
        if (from == canonical) return Result.Resolved(qty.amount)

        // 1. Standard conversion within the same unit family.
        val fromStd = standard[from]
        val canonStd = standard[canonical]
        if (fromStd != null && canonStd != null && fromStd.first == canonStd.first) {
            return Result.Resolved(qty.amount * fromStd.second / canonStd.second)
        }

        // 2. Declared rule, possibly chained through one standard conversion
        //    (e.g. rule targets "g" but canonical is "kg").
        for (rule in rules) {
            if (norm(rule.fromUnit) != from) continue
            val inRuleTarget = qty.amount / rule.fromQty * rule.toQty
            when (val r = resolve(Quantity(inRuleTarget, rule.toUnit), canonicalUnit, emptyList())) {
                is Result.Resolved -> return r
                is Result.Unresolved -> {}
            }
        }

        return Result.Unresolved(qty.unit)
    }
}
