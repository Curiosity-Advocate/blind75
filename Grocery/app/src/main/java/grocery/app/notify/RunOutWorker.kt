package grocery.app.notify

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import grocery.app.data.Repository
import grocery.app.data.db.GroceryDb
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

/**
 * Daily check: any category projected to run out within the warning window gets
 * a notification. Projection comes from purchase gaps (or the fixed interval),
 * so this only fires for items with enough purchase history.
 */
class RunOutWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val db = GroceryDb.get(applicationContext)
        val repo = Repository(db)
        val now = System.currentTimeMillis()

        val runningOut = db.categories().all().first().mapNotNull { category ->
            val runOut = repo.projectedRunOutMs(category.id) ?: return@mapNotNull null
            val daysLeft = (runOut - now) / MS_PER_DAY
            if (daysLeft <= WARN_DAYS) category.name to daysLeft else null
        }
        if (runningOut.isNotEmpty() && hasPermission()) notify(runningOut)
        return Result.success()
    }

    private fun hasPermission(): Boolean =
        android.os.Build.VERSION.SDK_INT < 33 ||
            applicationContext.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED

    private fun notify(items: List<Pair<String, Long>>) {
        val manager = applicationContext.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL, "Running low", NotificationManager.IMPORTANCE_DEFAULT)
        )
        val text = items.joinToString("\n") { (name, days) ->
            if (days <= 0) "$name — probably out" else "$name — about $days day(s) left"
        }
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL)
            .setSmallIcon(android.R.drawable.stat_notify_more)
            .setContentTitle("Running low on ${items.size} item(s)")
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(applicationContext).notify(1, notification)
    }

    companion object {
        private const val CHANNEL = "run-out"
        private const val MS_PER_DAY = 86_400_000L
        private const val WARN_DAYS = 7L

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<RunOutWorker>(1, TimeUnit.DAYS).build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "run-out-check", ExistingPeriodicWorkPolicy.UPDATE, request,
            )
        }
    }
}
