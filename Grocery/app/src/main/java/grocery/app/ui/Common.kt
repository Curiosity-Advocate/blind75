package grocery.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import grocery.app.data.db.Category
import grocery.app.data.db.GroceryDb
import kotlinx.coroutines.launch

/**
 * Item picker: dropdown of all known items, plus "+ New item" so an item that
 * isn't in the list yet (you don't own it, never bought it) can be created on
 * the spot without a trip to the Items tab.
 */
@Composable
fun CategoryPicker(
    categories: List<Category>,
    selected: Category?,
    onSelect: (Category) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Pick item…",
    db: GroceryDb? = null, // enables inline "+ New item" when provided
) {
    val scope = rememberCoroutineScope()
    var open by remember { mutableStateOf(false) }
    var creating by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var newUnit by remember { mutableStateOf("") }

    Row(modifier) {
        OutlinedButton(onClick = { open = true }, modifier = Modifier.fillMaxWidth()) {
            Text(selected?.name ?: placeholder)
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            categories.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category.name) },
                    onClick = { onSelect(category); open = false },
                )
            }
            if (db != null) {
                DropdownMenuItem(
                    text = { Text("+ New item…") },
                    onClick = { creating = true; open = false },
                )
            }
        }
    }

    if (creating && db != null) {
        AlertDialog(
            onDismissRequest = { creating = false },
            title = { Text("New item") },
            text = {
                Column {
                    OutlinedTextField(newName, { newName = it }, label = { Text("Name (e.g. eggs)") })
                    OutlinedTextField(newUnit, { newUnit = it }, label = { Text("Base unit (g, ml, each…)") })
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newName.isBlank() || newUnit.isBlank()) return@TextButton
                    scope.launch {
                        val id = db.categories().insert(
                            Category(name = newName.trim(), canonicalUnit = newUnit.trim())
                        )
                        db.categories().byId(id)?.let(onSelect)
                        newName = ""; newUnit = ""
                        creating = false
                    }
                }) { Text("Create") }
            },
            dismissButton = { TextButton(onClick = { creating = false }) { Text("Cancel") } },
        )
    }
}

fun fmt(v: Double): String = if (v % 1.0 == 0.0) v.toInt().toString() else "%.1f".format(v)

/** Open the Android share sheet with plain text (WhatsApp, Messages, Keep, email…). */
fun shareText(context: android.content.Context, subject: String, text: String) {
    val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(android.content.Intent.EXTRA_SUBJECT, subject)
        putExtra(android.content.Intent.EXTRA_TEXT, text)
    }
    context.startActivity(android.content.Intent.createChooser(intent, subject))
}

/**
 * All countries with their flags, alphabetical. Generated from the platform's
 * ISO country list: a flag emoji is just the two country-code letters mapped to
 * Unicode regional-indicator symbols, so every country renders its real flag.
 */
val COUNTRIES: List<String> by lazy {
    java.util.Locale.getISOCountries()
        .map { code ->
            val flag = code.map { ch ->
                String(Character.toChars(0x1F1E6 + (ch - 'A')))
            }.joinToString("")
            "$flag " + java.util.Locale("", code).getDisplayCountry(java.util.Locale.ENGLISH)
        }
        .sortedBy { it.substringAfter(' ') } + "🌍 Other"
}

/** Searchable dialog over [COUNTRIES]; stores the flag+name string on the recipe. */
@Composable
fun CountryPicker(
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var open by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    OutlinedButton(onClick = { open = true; query = "" }, modifier = modifier.fillMaxWidth()) {
        Text(selected.ifBlank { "Country…" })
    }
    if (open) {
        AlertDialog(
            onDismissRequest = { open = false },
            title = { Text("Country") },
            text = {
                Column {
                    OutlinedTextField(
                        query, { query = it },
                        label = { Text("Search") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    val matches = COUNTRIES.filter { it.contains(query.trim(), ignoreCase = true) }
                    LazyColumn(
                        Modifier.fillMaxWidth().heightIn(max = 400.dp),
                    ) {
                        items(matches.size) { i ->
                            Text(
                                matches[i],
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelect(matches[i]); open = false }
                                    .padding(vertical = 10.dp),
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { open = false }) { Text("Cancel") } },
        )
    }
}
