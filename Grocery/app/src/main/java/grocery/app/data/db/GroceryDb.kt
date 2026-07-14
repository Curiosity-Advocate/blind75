package grocery.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// v1→v2: category label colours.
private val MIGRATION_1_2 = Migration(1, 2) { db: SupportSQLiteDatabase ->
    db.execSQL("ALTER TABLE Category ADD COLUMN colorArgb INTEGER")
}

// v2→v3: recipe country / source / created date.
private val MIGRATION_2_3 = Migration(2, 3) { db: SupportSQLiteDatabase ->
    db.execSQL("ALTER TABLE Recipe ADD COLUMN country TEXT NOT NULL DEFAULT ''")
    db.execSQL("ALTER TABLE Recipe ADD COLUMN sourceUrl TEXT NOT NULL DEFAULT ''")
    db.execSQL("ALTER TABLE Recipe ADD COLUMN createdAtMs INTEGER NOT NULL DEFAULT 0")
}

// v3→v4: shopping-list notes.
private val MIGRATION_3_4 = Migration(3, 4) { db: SupportSQLiteDatabase ->
    db.execSQL("ALTER TABLE BuyOverride ADD COLUMN note TEXT NOT NULL DEFAULT ''")
}

@Database(
    entities = [
        Category::class, StoreProduct::class, PricePoint::class, Purchase::class,
        ConversionRuleEntity::class, Recipe::class, RecipeIngredient::class,
        PlanRecipe::class, PlanAdhocItem::class, OnHand::class, ScrapeConfig::class,
        BuyOverride::class,
    ],
    version = 4,
    exportSchema = true,
)
abstract class GroceryDb : RoomDatabase() {
    abstract fun categories(): CategoryDao
    abstract fun products(): ProductDao
    abstract fun prices(): PriceDao
    abstract fun purchases(): PurchaseDao
    abstract fun rules(): RuleDao
    abstract fun recipes(): RecipeDao
    abstract fun plan(): PlanDao
    abstract fun onHand(): OnHandDao
    abstract fun scrapeConfigs(): ScrapeConfigDao
    abstract fun buyOverrides(): BuyOverrideDao

    companion object {
        @Volatile private var instance: GroceryDb? = null
        fun get(context: Context): GroceryDb = instance ?: synchronized(this) {
            // No destructive fallback: user data must survive app updates. Any
            // future schema change MUST bump the version AND ship a Migration
            // (or an @AutoMigration against the exported schemas/ JSON files) —
            // otherwise the app crashes at open rather than silently wiping data.
            instance ?: Room.databaseBuilder(context.applicationContext, GroceryDb::class.java, "grocery.db")
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                .build().also { instance = it }
        }
    }
}
