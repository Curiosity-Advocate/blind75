package grocery.app.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** In-app guide: what each tab does, with one running example. */
@Composable
fun HelpScreen() {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
    ) {
        Text("How Grocery Tracker works", style = MaterialTheme.typography.titleLarge)
        Text(
            "The running example below: you cook a salmon bowl this week, buy veggies " +
                "every week, and want to stock up on toilet paper when it's cheap.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(vertical = 8.dp),
        )

        Section(
            "1 · Items — everything the app knows",
            "Every item you ever cook with or buy lives here once, with its base unit. " +
                "Being on this list does NOT mean you own it — 'on hand' tracks that.\n\n" +
                "Example: 'salmon' (g), 'cooking oil' (ml), 'toilet paper' (roll), " +
                "'eggs' (each).\n\n" +
                "• Set on hand — what's actually at home: oil 300 ml. Shown on the card; " +
                "the buy list subtracts it.\n" +
                "• Log buy — record a real purchase ('750 ml bottle at Coles, $6'). One " +
                "entry does two jobs: a price observation AND the run-out prediction " +
                "(from gaps between buys). The card shows your last buy as proof.\n" +
                "• Tap the colour dot to colour-code or delete an item.\n" +
                "• Units (top right) — see built-in conversions (lb→g, cup→ml, dozen…) " +
                "and add your own, per item ('6 fillet = 1000 g' for salmon) or global " +
                "('1 bunch = 6 each' for everything).",
        )
        Section(
            "2 · Recipes — what a dish needs",
            "A recipe has a name, a country (picked from a flag list) and a source " +
                "website — all searchable from the box up top. Tap a recipe to see and " +
                "edit its ingredients; ✕ removes one; Delete removes the whole recipe " +
                "(items and price history are untouched).\n\n" +
                "Ingredients are picked from the Items list, or created on the spot with " +
                "'+ New item…'. Amount and unit are free-form — '2 fillet', '1 dozen', " +
                "'1 lb' all work. Unknown units trigger a one-time question on the Buy " +
                "list ('how many g is 1 fillet?'), remembered forever.\n\n" +
                "• Share — sends the recipe as a message (WhatsApp, Keep, email…).\n" +
                "• Import — paste a shared recipe message on another phone running this " +
                "app; missing items are created automatically.",
        )
        Section(
            "3 · Plan — pick this week",
            "Tick the recipes you'll cook, then add extra non-recipe items — the weekly " +
                "veggies and fruit. Extras are picked from Items or created inline.\n\n" +
                "Example: tick 'Salmon bowl', add 5 each apples and 1 bunch green onions.",
        )
        Section(
            "4 · Buy list — what to actually buy",
            "Adds up every ingredient across ticked recipes plus extras, subtracts " +
                "what's on hand, rounds up to whole packs.\n\n" +
                "Example: you need 5 apples and have 2 → buy 3. Oil needs 30 ml and you " +
                "have 300 → not on the list.\n\n" +
                "• Find prices — searches Woolworths and Coles live, shows the cheapest " +
                "offer (price, brand, store) per item, and starts tracking the 5 " +
                "cheapest products per store; tracked prices then refresh weekly.\n" +
                "• Stock up / Wait chips — 'Stock up' = historically cheap (bottom 20%) " +
                "or beats your recent buys; 'Wait' = expensive right now.\n" +
                "• Not found — still listed; price it at the shelf.\n" +
                "• +/−, Remove, Note — overrule any line or attach a note ('get the " +
                "green one'); Reset edits restores the computed list.\n" +
                "• Share — sends the list as a ☐ checklist with notes to any app.\n" +
                "• Tap a line — the breakdown (which recipe needs what), the price " +
                "history chart with min/now/max, and the predicted run-out date.",
        )
        Section(
            "Behind the scenes",
            "• Weekly, every tracked product's price refreshes automatically (by " +
                "search and by direct product lookup).\n" +
                "• Daily, run-out dates are checked; you get a notification when " +
                "something is about a week from running out.\n" +
                "• All data stays on this phone and survives app updates.",
        )
    }
}

@Composable
private fun Section(title: String, body: String) {
    Card(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Column(Modifier.padding(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(body, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 6.dp))
        }
    }
}
