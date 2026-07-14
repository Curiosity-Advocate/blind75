package grocery.app

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Surface
import grocery.app.data.Repository
import grocery.app.data.db.GroceryDb
import grocery.app.notify.RunOutWorker
import grocery.app.scrape.ScrapeWorker
import grocery.app.ui.GroceryApp

class MainActivity : ComponentActivity() {
    private val notifPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db = GroceryDb.get(this)
        val repo = Repository(db)
        ScrapeWorker.schedule(this)
        RunOutWorker.schedule(this)
        if (Build.VERSION.SDK_INT >= 33) {
            notifPermission.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
        setContent {
            grocery.app.ui.GroceryTheme {
                Surface { GroceryApp(db, repo) }
            }
        }
    }
}
