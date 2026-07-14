package grocery.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import grocery.app.data.Repository
import grocery.app.data.db.BuyOverride
import grocery.app.data.db.ConversionRuleEntity
import grocery.app.data.db.GroceryDb
import grocery.app.data.db.OnHand
import grocery.app.data.db.PlanAdhocItem
import grocery.app.data.db.PlanRecipe
import grocery.app.data.db.StoreProduct
import grocery.app.scrape.PriceFinder
import grocery.core.BuyLine
import grocery.core.Verdict
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val DEFAULT_DOT = 0xFF9E9E9EL // grey until the user picks a colour
private val PALETTE = listOf(
    0xFFE53935L, 0xFFFB8C00L, 0xFFFDD835L, 0xFF43A047L,
    0xFF1E88E5L, 0xFF8E24AAL, 0xFF6D4C41L, 0xFF9E9E9EL,
)

/** Pick this week's recipes and ad-hoc non-recipe items (veggies, fruit...). */
@Composable
fun PlanScreen(db: GroceryDb) {
    val scope = rememberCoroutineScope()
    val recipes by db.recipes().all().collectAsState(initial = emptyList())
    val planned by db.plan().recipes().collectAsState(initial = emptyList())
    val adhoc by db.plan().adhocItems().collectAsState(initial = emptyList())
    val categories by db.categories().all().collectAsState(initial = emptyList())
    val plannedIds = planned.map { it.recipeId }.toSet()
    val categoryById = categories.associateBy { it.id }
    var selected by remember { mutableStateOf<grocery.app.data.db.Category?>(null) }
    var amount by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("This week's recipes", style = MaterialTheme.typography.titleLarge)
        LazyColumn(Modifier.weight(1f)) {
            items(recipes) { recipe ->
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(
                        checked = recipe.id in plannedIds,
                        onCheckedChange = { checked ->
                            scope.launch {
                                if (checked) db.plan().addRecipe(PlanRecipe(recipe.id))
                                else db.plan().removeRecipe(recipe.id)
                            }
                        },
                    )
                    Text(recipe.name)
                }
            }
            item {
                Column(Modifier.padding(top = 12.dp)) {
                    Text("Extra items (not from a recipe)", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Weekly one-offs like veggies and fruit. Pick a known item or create a new one right from the picker.",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
            items(adhoc) { item ->
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("${fmt(item.amount)} ${item.unit} ${categoryById[item.categoryId]?.name ?: "?"}")
                    TextButton(onClick = { scope.launch { db.plan().removeAdhoc(item.id) } }) { Text("Remove") }
                }
            }
        }
        CategoryPicker(categories, selected, { cat ->
            selected = cat
            if (unit.isBlank()) unit = cat.canonicalUnit
        }, placeholder = "Pick or create item…", db = db)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(amount, { amount = it }, label = { Text("Amount") }, modifier = Modifier.weight(1f))
            OutlinedTextField(unit, { unit = it }, label = { Text("Unit") }, modifier = Modifier.weight(1f))
        }
        Button(
            onClick = {
                val cat = selected ?: return@Button
                val amt = amount.toDoubleOrNull() ?: return@Button
                scope.launch {
                    db.plan().addAdhoc(PlanAdhocItem(categoryId = cat.id, amount = amt,
                        unit = unit.ifBlank { cat.canonicalUnit }))
                    amount = ""
                }
            },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        ) { Text("Add extra item") }
    }
}

/**
 * The main output: what to buy, with stock-up verdict chips. Tap a row for the
 * breakdown. Items with no known store product stay on the list flagged "Not
 * found". Rows can be overridden (+/− packs, remove); Reset restores computed values.
 */
@Composable
fun BuyListScreen(db: GroceryDb, repo: Repository, onExplain: (Long) -> Unit) {
    val scope = rememberCoroutineScope()
    val planned by db.plan().recipes().collectAsState(initial = emptyList())
    val adhoc by db.plan().adhocItems().collectAsState(initial = emptyList())
    val overrides by db.buyOverrides().all().collectAsState(initial = emptyList())
    var lines by remember { mutableStateOf<List<BuyLine>>(emptyList()) }
    var needsRule by remember { mutableStateOf<Repository.PlanResult.NeedsRule?>(null) }
    var verdicts by remember { mutableStateOf<Map<Long, Verdict>>(emptyMap()) }
    var bestOffers by remember { mutableStateOf<Map<Long, PriceFinder.BestOffer>>(emptyMap()) }
    var finding by remember { mutableStateOf(false) }
    var refresh by remember { mutableStateOf(0) }
    var noteFor by remember { mutableStateOf<BuyLine?>(null) }
    val context = androidx.compose.ui.platform.LocalContext.current
    val overrideMap = overrides.associateBy { it.categoryId }

    noteFor?.let { line ->
        NoteDialog(
            initial = overrideMap[line.categoryId]?.note ?: "",
            title = line.categoryName,
            onSave = { text ->
                scope.launch {
                    val prev = overrideMap[line.categoryId]
                    db.buyOverrides().upsert(
                        BuyOverride(line.categoryId, prev?.packsOverride, prev?.hidden ?: false, text)
                    )
                    noteFor = null
                }
            },
            onDismiss = { noteFor = null },
        )
    }

    LaunchedEffect(planned, adhoc, refresh) {
        when (val r = repo.buildBuyList(
            planned.map { it.recipeId },
            adhoc.map { Triple(it.categoryId, it.amount, it.unit) },
        )) {
            is Repository.PlanResult.Ok -> {
                lines = r.lines
                needsRule = null
                verdicts = r.lines.mapNotNull { line ->
                    val pack = line.chosenPack ?: return@mapNotNull null
                    val price = pack.currentPrice ?: return@mapNotNull null
                    if (pack.packSize <= 0) return@mapNotNull null
                    line.categoryId to repo.assessPrice(line.categoryId, price / pack.packSize).verdict
                }.toMap()
            }
            is Repository.PlanResult.NeedsRule -> needsRule = r
        }
    }

    // One-time declaration prompt: "how many <canonical> is 1 <unit>?" — the rule
    // is saved and applies to every future recipe using that unit.
    needsRule?.let { nr ->
        RuleDialog(db = db, nr = nr, onDone = { needsRule = null; refresh++ })
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Buy list", style = MaterialTheme.typography.titleLarge)
            Row {
                if (overrides.isNotEmpty()) {
                    TextButton(onClick = { scope.launch { db.buyOverrides().clear() } }) { Text("Reset edits") }
                }
                // The on-demand mechanism: search Woolworths + Coles for every
                // category in the plan, report the cheapest offer, and add the
                // top-5 per store to the price-tracking-list.
                TextButton(
                    enabled = !finding && lines.isNotEmpty(),
                    onClick = {
                        scope.launch {
                            finding = true
                            val offers = PriceFinder(db).findAndTrack(lines.map { it.categoryId })
                            bestOffers = offers.associateBy { it.categoryId }
                            finding = false
                            refresh++ // recompute the list with the fresh prices
                        }
                    },
                ) { Text(if (finding) "Finding…" else "Find prices") }
                TextButton(
                    enabled = lines.isNotEmpty(),
                    onClick = {
                        val visible = lines.filter {
                            (it.packsToBuy > 0 || it.notFound) && overrideMap[it.categoryId]?.hidden != true
                        }
                        val body = visible.joinToString("\n") { line ->
                            val packs = overrideMap[line.categoryId]?.packsOverride ?: line.packsToBuy
                            val note = overrideMap[line.categoryId]?.note
                                ?.takeIf { it.isNotBlank() }?.let { " — $it" } ?: ""
                            if (line.notFound) {
                                "☐ ${line.categoryName} (need ${fmt(line.netNeeded)} ${line.canonicalUnit}, price at shelf)$note"
                            } else {
                                val pack = line.chosenPack!!
                                val cost = pack.currentPrice?.let { " \$${fmt(it * packs)}" } ?: ""
                                "☐ $packs× ${pack.name} @ ${pack.store}$cost$note"
                            }
                        }
                        shareText(context, "Shopping list", "Shopping list\n$body")
                    },
                ) { Text("Share") }
            }
        }
        needsRule?.let {
            Text(
                "'${it.unit}' of ${it.categoryName} needs a conversion — see prompt.",
                color = MaterialTheme.colorScheme.error,
            )
        }
        // Not-found items are as much a part of the list as priced ones.
        val visible = lines.filter { (it.packsToBuy > 0 || it.notFound) && overrideMap[it.categoryId]?.hidden != true }
        LazyColumn {
            items(visible) { line ->
                val packs = overrideMap[line.categoryId]?.packsOverride ?: line.packsToBuy
                val edited = overrideMap[line.categoryId]?.packsOverride != null
                Card(
                    onClick = { onExplain(line.categoryId) },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                ) {
                    Column(Modifier.fillMaxWidth().padding(12.dp)) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    if (line.notFound) line.categoryName
                                    else "$packs× ${line.chosenPack?.name ?: line.categoryName}" + if (edited) " (edited)" else "",
                                )
                                Text(
                                    "need ${fmt(line.netNeeded)} ${line.canonicalUnit}" +
                                        (line.chosenPack?.takeIf { !line.notFound }
                                            ?.let { p -> (p.currentPrice?.let { " · \$${fmt(it * packs)}" } ?: "") + " @ ${p.store}" }
                                            ?: ""),
                                    style = MaterialTheme.typography.bodySmall,
                                )
                                bestOffers[line.categoryId]?.let { offer ->
                                    Text(
                                        "Cheapest: \$${fmt(offer.price)} — ${offer.productName} @ ${offer.store}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                }
                                overrideMap[line.categoryId]?.note?.takeIf { it.isNotBlank() }?.let {
                                    Text("📝 $it", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                            if (line.notFound) {
                                AssistChip(onClick = {}, label = { Text("Not found") })
                            } else when (verdicts[line.categoryId]) {
                                Verdict.STOCK_UP -> AssistChip(onClick = {}, label = { Text("Stock up") })
                                Verdict.WAIT -> AssistChip(onClick = {}, label = { Text("Wait") })
                                else -> {}
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            if (!line.notFound) {
                                TextButton(onClick = {
                                    scope.launch {
                                        val prev = overrideMap[line.categoryId]
                                        db.buyOverrides().upsert(
                                            BuyOverride(line.categoryId, (packs - 1).coerceAtLeast(0),
                                                note = prev?.note ?: "")
                                        )
                                    }
                                }) { Text("−") }
                                TextButton(onClick = {
                                    scope.launch {
                                        val prev = overrideMap[line.categoryId]
                                        db.buyOverrides().upsert(
                                            BuyOverride(line.categoryId, packs + 1, note = prev?.note ?: "")
                                        )
                                    }
                                }) { Text("+") }
                            }
                            TextButton(onClick = { noteFor = line }) { Text("Note") }
                            TextButton(onClick = {
                                scope.launch {
                                    val prev = overrideMap[line.categoryId]
                                    db.buyOverrides().upsert(
                                        BuyOverride(line.categoryId, prev?.packsOverride, hidden = true, prev?.note ?: "")
                                    )
                                }
                            }) { Text("Remove") }
                        }
                    }
                }
            }
        }
    }
}

/** The attribution screen: where each amount comes from, and the run-out projection. */
@Composable
fun ExplainScreen(db: GroceryDb, repo: Repository, categoryId: Long) {
    val planned by db.plan().recipes().collectAsState(initial = emptyList())
    val adhoc by db.plan().adhocItems().collectAsState(initial = emptyList())
    var line by remember { mutableStateOf<BuyLine?>(null) }
    var runOut by remember { mutableStateOf<Long?>(null) }
    var history by remember { mutableStateOf<List<Pair<Long, Double>>>(emptyList()) }

    LaunchedEffect(planned, adhoc, categoryId) {
        val r = repo.buildBuyList(
            planned.map { it.recipeId },
            adhoc.map { Triple(it.categoryId, it.amount, it.unit) },
        )
        if (r is Repository.PlanResult.Ok) line = r.lines.find { it.categoryId == categoryId }
        runOut = repo.projectedRunOutMs(categoryId)
        history = repo.categoryUnitPriceHistory(categoryId)
    }

    val l = line ?: return
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(l.categoryName, style = MaterialTheme.typography.titleLarge)
        Text("Buying ${l.packsToBuy}× ${l.chosenPack?.name ?: ""} to cover ${fmt(l.netNeeded)} ${l.canonicalUnit}")
        HorizontalDivider()
        l.breakdown.forEach { need ->
            Text("• ${fmt(need.amountCanonical)} ${l.canonicalUnit} — ${need.sourceLabel}")
        }
        if (l.onHand > 0) Text("• minus ${fmt(l.onHand)} ${l.canonicalUnit} on hand")
        runOut?.let {
            HorizontalDivider()
            val date = SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(Date(it))
            Text("Projected to run out around $date (from recent purchase gaps)")
        }
        if (history.size >= 2) {
            HorizontalDivider()
            Text("Cheapest unit price over time (\$/${l.canonicalUnit})",
                style = MaterialTheme.typography.titleSmall)
            PriceHistoryChart(history)
            Text(
                "min \$${"%.2f".format(history.minOf { it.second })} · " +
                    "now \$${"%.2f".format(history.last().second)} · " +
                    "max \$${"%.2f".format(history.maxOf { it.second })}",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

/** Minimal line chart: daily cheapest unit price across all tracked products. */
@Composable
private fun PriceHistoryChart(points: List<Pair<Long, Double>>) {
    val color = MaterialTheme.colorScheme.primary
    Canvas(Modifier.fillMaxWidth().height(140.dp).padding(vertical = 8.dp)) {
        val minT = points.first().first
        val maxT = points.last().first
        val minP = points.minOf { it.second }
        val maxP = points.maxOf { it.second }
        val spanT = (maxT - minT).coerceAtLeast(1L).toFloat()
        val spanP = (maxP - minP).coerceAtLeast(1e-9).toFloat()
        val pad = 6.dp.toPx()
        fun x(t: Long) = pad + (t - minT) / spanT * (size.width - 2 * pad)
        fun y(p: Double) = size.height - pad - ((p - minP) / spanP * (size.height - 2 * pad)).toFloat()

        val path = Path()
        points.forEachIndexed { i, (t, p) ->
            if (i == 0) path.moveTo(x(t), y(p)) else path.lineTo(x(t), y(p))
        }
        drawPath(path, color, style = Stroke(width = 2.dp.toPx()))
        points.forEach { (t, p) ->
            drawCircle(color, radius = 3.dp.toPx(), center = Offset(x(t), y(p)))
        }
    }
}

/** Pantry: categories, on-hand amounts, purchase logging (the shared entry flow). */
@Composable
fun PantryScreen(db: GroceryDb, repo: Repository, onUnits: () -> Unit = {}) {
    val scope = rememberCoroutineScope()
    val categories by db.categories().all().collectAsState(initial = emptyList())
    val onHandAll by db.onHand().observe().collectAsState(initial = emptyList())
    var newName by remember { mutableStateOf("") }
    var newUnit by remember { mutableStateOf("") }
    var logFor by remember { mutableStateOf<grocery.app.data.db.Category?>(null) }
    var onHandFor by remember { mutableStateOf<grocery.app.data.db.Category?>(null) }
    var editFor by remember { mutableStateOf<grocery.app.data.db.Category?>(null) }
    var buyReload by remember { mutableStateOf(0) }
    val onHandMap = onHandAll.associate { it.categoryId to it.amountCanonical }

    logFor?.let { PurchaseDialog(db, repo, it, onDone = { logFor = null; buyReload++ }) }
    onHandFor?.let { OnHandDialog(db, it, onDone = { onHandFor = null }) }
    editFor?.let { CategoryEditDialog(db, it, onDone = { editFor = null }) }

    // "Log buy" visible feedback: each card shows the latest recorded purchase.
    var lastBuys by remember { mutableStateOf<Map<Long, String>>(emptyMap()) }
    LaunchedEffect(categories, buyReload) {
        val dateFmt = SimpleDateFormat("d MMM", Locale.getDefault())
        lastBuys = categories.mapNotNull { category ->
            val last = db.purchases().lastForCategory(category.id) ?: return@mapNotNull null
            category.id to "last buy \$${fmt(last.price)} · ${dateFmt.format(Date(last.timestampMs))}"
        }.toMap()
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Items", style = MaterialTheme.typography.titleLarge)
            TextButton(onClick = onUnits) { Text("Units") }
        }
        Text(
            "Everything the app knows about — owned or not. 'On hand' is what you actually have.",
            style = MaterialTheme.typography.bodySmall,
        )
        LazyColumn(Modifier.weight(1f)) {
            items(categories) { category ->
                Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Column(Modifier.fillMaxWidth().padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Colour dot: tap to change colour or remove the item.
                            Box(
                                Modifier
                                    .padding(end = 10.dp)
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(Color(category.colorArgb ?: DEFAULT_DOT))
                                    .clickable { editFor = category },
                            )
                            Column(Modifier.weight(1f)) {
                                Text(category.name, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    onHandMap[category.id]
                                        ?.let { "On hand: ${fmt(it)} ${category.canonicalUnit}" }
                                        ?: "On hand: not set (${category.canonicalUnit})",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                                lastBuys[category.id]?.let {
                                    Text(it, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                        Row(
                            Modifier.fillMaxWidth().padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            FilledTonalButton(
                                onClick = { onHandFor = category },
                                modifier = Modifier.weight(1f),
                            ) { Text("Set on hand") }
                            FilledTonalButton(
                                onClick = { logFor = category },
                                modifier = Modifier.weight(1f),
                            ) { Text("Log buy") }
                        }
                    }
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(newName, { newName = it }, label = { Text("New item") }, modifier = Modifier.weight(2f))
            OutlinedTextField(newUnit, { newUnit = it }, label = { Text("Unit") }, modifier = Modifier.weight(1f))
        }
        Button(
            onClick = {
                if (newName.isNotBlank() && newUnit.isNotBlank()) scope.launch {
                    db.categories().insert(grocery.app.data.db.Category(name = newName.trim(), canonicalUnit = newUnit.trim()))
                    newName = ""; newUnit = ""
                }
            },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        ) { Text("Add item") }
    }
}

/**
 * The one-time conversion declaration ("Recipe says 'fillet' — how many g is 1
 * fillet of salmon?"). Saved as a ConversionRule; applies forever after.
 */
@Composable
fun RuleDialog(db: GroceryDb, nr: Repository.PlanResult.NeedsRule, onDone: () -> Unit) {
    val scope = rememberCoroutineScope()
    var canonicalUnit by remember { mutableStateOf("") }
    var value by remember { mutableStateOf("") }
    LaunchedEffect(nr.categoryId) {
        canonicalUnit = db.categories().byId(nr.categoryId)?.canonicalUnit ?: ""
    }

    AlertDialog(
        onDismissRequest = onDone,
        title = { Text("Unknown unit: ${nr.unit}") },
        text = {
            Column {
                Text("How many $canonicalUnit is 1 ${nr.unit} of ${nr.categoryName}?")
                OutlinedTextField(value, { value = it }, label = { Text(canonicalUnit) })
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val v = value.toDoubleOrNull() ?: return@TextButton
                scope.launch {
                    db.rules().insert(
                        ConversionRuleEntity(
                            categoryId = nr.categoryId,
                            fromUnit = nr.unit, fromQty = 1.0,
                            toUnit = canonicalUnit, toQty = v,
                        )
                    )
                    onDone()
                }
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDone) { Text("Later") } },
    )
}

/** Free-text note attached to a shopping-list line ("get the green one"). */
@Composable
fun NoteDialog(initial: String, title: String, onSave: (String) -> Unit, onDismiss: () -> Unit) {
    var text by remember { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Note: $title") },
        text = { OutlinedTextField(text, { text = it }, label = { Text("Note") }) },
        confirmButton = { TextButton(onClick = { onSave(text.trim()) }) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

/** Change an item's label colour, or remove it (and all its data) entirely. */
@Composable
fun CategoryEditDialog(db: GroceryDb, category: grocery.app.data.db.Category, onDone: () -> Unit) {
    val scope = rememberCoroutineScope()
    var confirmDelete by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDone,
        title = { Text(category.name) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Label colour")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PALETTE.forEach { argb ->
                        Box(
                            Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(argb))
                                .clickable {
                                    scope.launch {
                                        db.categories().setColor(category.id, argb)
                                        onDone()
                                    }
                                },
                        )
                    }
                }
                if (confirmDelete) {
                    Text(
                        "Really remove '${category.name}'? Its price history, purchases and recipes' uses of it are deleted too.",
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (!confirmDelete) confirmDelete = true
                else scope.launch {
                    db.categories().deleteCascade(category.id)
                    onDone()
                }
            }) { Text(if (confirmDelete) "Yes, remove" else "Remove item", color = MaterialTheme.colorScheme.error) }
        },
        dismissButton = { TextButton(onClick = onDone) { Text("Close") } },
    )
}

/** Record roughly how much of an item is at home (in its canonical unit). */
@Composable
fun OnHandDialog(db: GroceryDb, category: grocery.app.data.db.Category, onDone: () -> Unit) {
    val scope = rememberCoroutineScope()
    var value by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDone,
        title = { Text("On hand: ${category.name}") },
        text = { OutlinedTextField(value, { value = it }, label = { Text(category.canonicalUnit) }) },
        confirmButton = {
            TextButton(onClick = {
                val v = value.toDoubleOrNull() ?: return@TextButton
                scope.launch {
                    db.onHand().upsert(OnHand(category.id, v))
                    onDone()
                }
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDone) { Text("Cancel") } },
    )
}

/**
 * The shared entry flow: one purchase updates price history AND run-out
 * projection. Pick an existing tracked product or describe a new one inline.
 */
@Composable
fun PurchaseDialog(db: GroceryDb, repo: Repository, category: grocery.app.data.db.Category, onDone: () -> Unit) {
    val scope = rememberCoroutineScope()
    var products by remember { mutableStateOf<List<StoreProduct>>(emptyList()) }
    var selected by remember { mutableStateOf<StoreProduct?>(null) }
    var newProduct by remember { mutableStateOf(false) }
    var store by remember { mutableStateOf("Woolworths") }
    var name by remember { mutableStateOf("") }
    var packSize by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var qty by remember { mutableStateOf("1") }
    var pickerOpen by remember { mutableStateOf(false) }
    LaunchedEffect(category.id) {
        products = db.products().byCategory(category.id)
        newProduct = products.isEmpty()
    }

    AlertDialog(
        onDismissRequest = onDone,
        title = { Text("Log buy: ${category.name}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                if (!newProduct) {
                    OutlinedButton(onClick = { pickerOpen = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(selected?.let { "${it.name} @ ${it.store}" } ?: "Pick product…")
                    }
                    DropdownMenu(expanded = pickerOpen, onDismissRequest = { pickerOpen = false }) {
                        products.forEach { p ->
                            DropdownMenuItem(
                                text = { Text("${p.name} @ ${p.store}") },
                                onClick = { selected = p; pickerOpen = false },
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("+ New product") },
                            onClick = { newProduct = true; pickerOpen = false },
                        )
                    }
                } else {
                    OutlinedTextField(store, { store = it }, label = { Text("Store") })
                    OutlinedTextField(name, { name = it }, label = { Text("Product name") })
                    OutlinedTextField(packSize, { packSize = it },
                        label = { Text("Pack size (${category.canonicalUnit})") })
                }
                OutlinedTextField(price, { price = it }, label = { Text("Price $") })
                OutlinedTextField(qty, { qty = it }, label = { Text("Quantity") })
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val pr = price.toDoubleOrNull() ?: return@TextButton
                val q = qty.toIntOrNull() ?: 1
                scope.launch {
                    val productId = if (newProduct) {
                        val size = packSize.toDoubleOrNull() ?: return@launch
                        if (name.isBlank()) return@launch
                        db.products().insert(
                            StoreProduct(
                                categoryId = category.id, store = store.trim(),
                                storeSku = "manual-${System.currentTimeMillis()}",
                                name = name.trim(), packSize = size,
                            )
                        )
                    } else selected?.id ?: return@launch
                    repo.logPurchase(productId, pr, q)
                    onDone()
                }
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDone) { Text("Cancel") } },
    )
}
