package grocery.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import grocery.app.data.RecipeCodec
import grocery.app.data.db.Category
import grocery.app.data.db.GroceryDb
import grocery.app.data.db.Recipe
import grocery.app.data.db.RecipeIngredient
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Recipe entry: name + country of origin + source website, searchable across
 * all three. Tap a recipe to expand its ingredients; ingredients can be picked
 * from known items or created on the spot.
 */
@Composable
fun RecipesScreen(db: GroceryDb) {
    val scope = rememberCoroutineScope()
    val recipes by db.recipes().all().collectAsState(initial = emptyList())
    var expanded by remember { mutableStateOf<Long?>(null) }
    var search by remember { mutableStateOf("") }
    var newName by remember { mutableStateOf("") }
    var newCountry by remember { mutableStateOf("") }
    var newSource by remember { mutableStateOf("") }
    var adding by remember { mutableStateOf(false) }
    var importing by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf<Recipe?>(null) }
    val context = androidx.compose.ui.platform.LocalContext.current

    val visible = recipes.filter { r ->
        val q = search.trim()
        q.isBlank() || listOf(r.name, r.country, r.sourceUrl).any { it.contains(q, ignoreCase = true) }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Recipes", style = MaterialTheme.typography.titleLarge)
            TextButton(onClick = { importing = true }) { Text("Import") }
        }
        OutlinedTextField(
            search, { search = it },
            label = { Text("Search name, country or site") },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        )
        LazyColumn(Modifier.weight(1f)) {
            items(visible) { recipe ->
                Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Column(Modifier.padding(12.dp)) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(
                                Modifier
                                    .weight(1f)
                                    .clickable { expanded = if (expanded == recipe.id) null else recipe.id },
                            ) {
                                Text(recipe.name, style = MaterialTheme.typography.titleMedium)
                                val meta = listOfNotNull(
                                    recipe.country.ifBlank { null },
                                    recipe.sourceUrl.ifBlank { null },
                                    recipe.createdAtMs.takeIf { it > 0 }?.let {
                                        "added " + SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(Date(it))
                                    },
                                ).joinToString(" · ")
                                if (meta.isNotBlank()) Text(meta, style = MaterialTheme.typography.bodySmall)
                            }
                            TextButton(onClick = {
                                scope.launch {
                                    val ings = db.recipes().ingredients(recipe.id)
                                    val cats = db.categories().allOnce().associateBy { it.id }
                                    shareText(context, "Recipe: ${recipe.name}",
                                        RecipeCodec.toShareText(recipe, ings, cats))
                                }
                            }) { Text("Share") }
                            TextButton(onClick = { confirmDelete = recipe }) { Text("Delete") }
                        }
                        if (expanded == recipe.id) IngredientEditor(db, recipe)
                    }
                }
            }
        }
        if (adding) {
            OutlinedTextField(newName, { newName = it }, label = { Text("Recipe name") }, modifier = Modifier.fillMaxWidth())
            CountryPicker(newCountry, { newCountry = it }, Modifier.padding(top = 4.dp))
            OutlinedTextField(newSource, { newSource = it }, label = { Text("Source site") }, modifier = Modifier.fillMaxWidth())
        }
        Button(
            onClick = {
                if (!adding) { adding = true; return@Button }
                if (newName.isNotBlank()) scope.launch {
                    expanded = db.recipes().insert(
                        Recipe(
                            name = newName.trim(), country = newCountry.trim(),
                            sourceUrl = newSource.trim(), createdAtMs = System.currentTimeMillis(),
                        )
                    )
                    newName = ""; newCountry = ""; newSource = ""
                    adding = false
                }
            },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        ) { Text(if (adding) "Save recipe" else "New recipe") }
    }

    if (importing) {
        var pasted by remember { mutableStateOf("") }
        var failed by remember { mutableStateOf(false) }
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { importing = false },
            title = { Text("Import recipe") },
            text = {
                Column {
                    Text("Paste a shared recipe message (it ends with a #grocery-recipe block).")
                    OutlinedTextField(pasted, { pasted = it; failed = false }, label = { Text("Paste here") })
                    if (failed) Text("Couldn't find a recipe in that text.", color = MaterialTheme.colorScheme.error)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        val id = RecipeCodec.import(db, pasted)
                        if (id == null) failed = true else { expanded = id; importing = false }
                    }
                }) { Text("Import") }
            },
            dismissButton = { TextButton(onClick = { importing = false }) { Text("Cancel") } },
        )
    }

    confirmDelete?.let { recipe ->
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { confirmDelete = null },
            title = { Text("Delete '${recipe.name}'?") },
            text = { Text("Its ingredient list is deleted and it's removed from this week's plan. Items and price history are untouched.") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch { db.recipes().deleteRecipe(recipe.id); confirmDelete = null }
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = null }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun IngredientEditor(db: GroceryDb, recipe: Recipe) {
    val scope = rememberCoroutineScope()
    val categories by db.categories().all().collectAsState(initial = emptyList())
    var ingredients by remember(recipe.id) { mutableStateOf<List<RecipeIngredient>>(emptyList()) }
    var reload by remember { mutableStateOf(0) }
    var selected by remember { mutableStateOf<Category?>(null) }
    var amount by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("") }

    LaunchedEffect(recipe.id, reload) { ingredients = db.recipes().ingredients(recipe.id) }
    val byId = categories.associateBy { it.id }

    Column(Modifier.padding(top = 8.dp)) {
        ingredients.forEach { ing ->
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "• ${fmt(ing.amount)} ${ing.unit} ${byId[ing.categoryId]?.name ?: "?"}",
                    style = MaterialTheme.typography.bodyMedium,
                )
                TextButton(onClick = {
                    scope.launch { db.recipes().deleteIngredient(ing.id); reload++ }
                }) { Text("✕") }
            }
        }
        CategoryPicker(
            categories, selected, { selected = it },
            Modifier.padding(top = 8.dp),
            placeholder = "Pick or create item…",
            db = db,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(amount, { amount = it }, label = { Text("Amount") }, modifier = Modifier.weight(1f))
            OutlinedTextField(unit, { unit = it }, label = { Text("Unit (g, ml, fillet…)") }, modifier = Modifier.weight(1f))
        }
        Button(
            onClick = {
                val cat = selected ?: return@Button
                val amt = amount.toDoubleOrNull() ?: return@Button
                scope.launch {
                    db.recipes().insertIngredient(
                        RecipeIngredient(recipeId = recipe.id, categoryId = cat.id, amount = amt,
                            unit = unit.ifBlank { cat.canonicalUnit })
                    )
                    amount = ""; unit = ""
                    reload++
                }
            },
            modifier = Modifier.padding(top = 4.dp),
        ) { Text("Add ingredient") }
    }
}
