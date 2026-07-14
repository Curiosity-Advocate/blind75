package grocery.app.ui

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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import grocery.app.data.db.Category
import grocery.app.data.db.ConversionRuleEntity
import grocery.app.data.db.GroceryDb
import grocery.core.UnitKind
import grocery.core.UnitResolver
import kotlinx.coroutines.launch

/**
 * Unit conversions: user-declared rules (add/delete; per item or global) and
 * the read-only built-in table. The same rules the buy-list prompt creates
 * lazily can be managed here directly.
 */
@Composable
fun UnitsScreen(db: GroceryDb) {
    val scope = rememberCoroutineScope()
    val rules by db.rules().all().collectAsState(initial = emptyList())
    val categories by db.categories().all().collectAsState(initial = emptyList())
    val categoryNames = categories.associate { it.id to it.name }

    var global by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf<Category?>(null) }
    var fromQty by remember { mutableStateOf("1") }
    var fromUnit by remember { mutableStateOf("") }
    var toQty by remember { mutableStateOf("") }
    var toUnit by remember { mutableStateOf("") }

    LazyColumn(Modifier.fillMaxSize().padding(16.dp)) {
        item {
            Text("Unit conversions", style = MaterialTheme.typography.titleLarge)
            Text(
                "Your rules", style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 12.dp),
            )
            if (rules.isEmpty()) Text("None yet.", style = MaterialTheme.typography.bodySmall)
        }
        items(rules) { rule ->
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "${fmt(rule.fromQty)} ${rule.fromUnit} = ${fmt(rule.toQty)} ${rule.toUnit}" +
                        " · " + (categoryNames[rule.categoryId] ?: "all items"),
                )
                TextButton(onClick = { scope.launch { db.rules().delete(rule.id) } }) { Text("✕") }
            }
        }
        item {
            Card(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Add a rule", style = MaterialTheme.typography.titleSmall)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = global, onCheckedChange = { global = it })
                        Text("Applies to all items (e.g. 1 dozen = 12 each)")
                    }
                    if (!global) {
                        CategoryPicker(categories, selected, { selected = it },
                            placeholder = "Which item is this for?", db = db)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(fromQty, { fromQty = it }, label = { Text("Qty") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(fromUnit, { fromUnit = it }, label = { Text("Unit (fillet…)") }, modifier = Modifier.weight(2f))
                    }
                    Text("=", modifier = Modifier.padding(start = 8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(toQty, { toQty = it }, label = { Text("Qty") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(toUnit, { toUnit = it }, label = { Text("Unit (g, ml…)") }, modifier = Modifier.weight(2f))
                    }
                    Button(
                        onClick = {
                            val fq = fromQty.toDoubleOrNull() ?: return@Button
                            val tq = toQty.toDoubleOrNull() ?: return@Button
                            if (fromUnit.isBlank() || toUnit.isBlank()) return@Button
                            val categoryId = if (global) 0L else selected?.id ?: return@Button
                            scope.launch {
                                db.rules().insert(
                                    ConversionRuleEntity(
                                        categoryId = categoryId,
                                        fromUnit = fromUnit.trim(), fromQty = fq,
                                        toUnit = toUnit.trim(), toQty = tq,
                                    )
                                )
                                fromQty = "1"; fromUnit = ""; toQty = ""; toUnit = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text("Save rule") }
                }
            }
        }
        item {
            Text("Built-in units", style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 8.dp))
            Text(
                "Always available; conversions within the same family are automatic.",
                style = MaterialTheme.typography.bodySmall,
            )
            val builtIn = UnitResolver.builtInUnits().entries.groupBy({ it.value.first }, { it })
            listOf(
                UnitKind.MASS to "Weight (base: g)",
                UnitKind.VOLUME to "Volume (base: ml)",
                UnitKind.COUNT to "Count (base: each)",
            ).forEach { (kind, title) ->
                Text(title, style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 8.dp))
                builtIn[kind]?.sortedBy { it.value.second }?.forEach { (unit, def) ->
                    Text("1 $unit = ${fmt(def.second)}", style = MaterialTheme.typography.bodyMedium)
                }
            }
            Text(
                "Aliases like grams/kgs/lbs/litres/pieces are recognised too.",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}
