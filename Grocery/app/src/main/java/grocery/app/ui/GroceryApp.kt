package grocery.app.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import grocery.app.data.Repository
import grocery.app.data.db.GroceryDb

private data class Tab(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

@Composable
fun GroceryApp(db: GroceryDb, repo: Repository) {
    val nav = rememberNavController()
    val tabs = listOf(
        Tab("recipes", "Recipes", Icons.Filled.Menu),
        Tab("plan", "Plan", Icons.Filled.List),
        Tab("buy", "Buy list", Icons.Filled.ShoppingCart),
        Tab("pantry", "Items", Icons.Filled.Check),
        Tab("help", "Help", Icons.Filled.Info),
    )
    val backStack by nav.currentBackStackEntryAsState()

    Scaffold(
        bottomBar = {
            NavigationBar {
                tabs.forEach { tab ->
                    NavigationBarItem(
                        selected = backStack?.destination?.route == tab.route,
                        onClick = { nav.navigate(tab.route) { launchSingleTop = true } },
                        icon = { Icon(tab.icon, tab.label) },
                        label = { Text(tab.label) },
                    )
                }
            }
        }
    ) { padding ->
        NavHost(nav, startDestination = "plan", modifier = Modifier.padding(padding)) {
            composable("recipes") { RecipesScreen(db) }
            composable("plan") { PlanScreen(db) }
            composable("buy") { BuyListScreen(db, repo, onExplain = { nav.navigate("explain/$it") }) }
            composable("explain/{categoryId}") { entry ->
                ExplainScreen(db, repo, entry.arguments?.getString("categoryId")?.toLongOrNull() ?: 0L)
            }
            composable("pantry") { PantryScreen(db, repo, onUnits = { nav.navigate("units") }) }
            composable("units") { UnitsScreen(db) }
            composable("help") { HelpScreen() }
        }
    }
}
