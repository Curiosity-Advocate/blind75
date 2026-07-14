package grocery.app.data

import grocery.app.data.db.Category
import grocery.app.data.db.GroceryDb
import grocery.app.data.db.Recipe
import grocery.app.data.db.RecipeIngredient
import org.json.JSONArray
import org.json.JSONObject

/**
 * Recipe sharing between phones, without a server: the share text is human-
 * readable with a machine-readable JSON block at the end. Another phone running
 * this app pastes the whole message into Import; the JSON block is parsed and
 * unknown items are created automatically (using each item's base unit).
 */
object RecipeCodec {

    private const val MARKER = "#grocery-recipe:"

    fun toShareText(
        recipe: Recipe,
        ingredients: List<RecipeIngredient>,
        categoriesById: Map<Long, Category>,
    ): String {
        val lines = ingredients.joinToString("\n") { ing ->
            "• ${ing.amount} ${ing.unit} ${categoriesById[ing.categoryId]?.name ?: "?"}"
        }
        val json = JSONObject()
            .put("n", recipe.name)
            .put("c", recipe.country)
            .put("s", recipe.sourceUrl)
            .put("i", JSONArray().apply {
                ingredients.forEach { ing ->
                    val category = categoriesById[ing.categoryId] ?: return@forEach
                    put(
                        JSONObject()
                            .put("item", category.name)
                            .put("base", category.canonicalUnit)
                            .put("a", ing.amount)
                            .put("u", ing.unit)
                    )
                }
            })
        val meta = listOf(recipe.country, recipe.sourceUrl).filter { it.isNotBlank() }.joinToString(" · ")
        return buildString {
            appendLine(recipe.name)
            if (meta.isNotBlank()) appendLine(meta)
            appendLine(lines)
            appendLine()
            append(MARKER).append(json)
        }
    }

    /** Import from pasted share text. Returns the new recipe id, or null if no block found. */
    suspend fun import(db: GroceryDb, text: String): Long? {
        val start = text.indexOf(MARKER)
        if (start < 0) return null
        val json = try {
            JSONObject(text.substring(start + MARKER.length).trim())
        } catch (e: Exception) {
            return null
        }
        val recipeId = db.recipes().insert(
            Recipe(
                name = json.optString("n").ifBlank { return null },
                country = json.optString("c"),
                sourceUrl = json.optString("s"),
                createdAtMs = System.currentTimeMillis(),
            )
        )
        val items = json.optJSONArray("i") ?: JSONArray()
        for (k in 0 until items.length()) {
            val ing = items.getJSONObject(k)
            val itemName = ing.optString("item")
            if (itemName.isBlank()) continue
            val category = db.categories().byName(itemName)
                ?: db.categories().byId(
                    db.categories().insert(
                        Category(name = itemName, canonicalUnit = ing.optString("base").ifBlank { "each" })
                    )
                )!!
            db.recipes().insertIngredient(
                RecipeIngredient(
                    recipeId = recipeId,
                    categoryId = category.id,
                    amount = ing.optDouble("a", 1.0),
                    unit = ing.optString("u").ifBlank { category.canonicalUnit },
                )
            )
        }
        return recipeId
    }
}
