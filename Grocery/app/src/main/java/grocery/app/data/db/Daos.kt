package grocery.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Insert suspend fun insert(c: Category): Long
    @Query("SELECT * FROM Category ORDER BY name") fun all(): Flow<List<Category>>
    @Query("SELECT * FROM Category WHERE id = :id") suspend fun byId(id: Long): Category?
    @Query("SELECT * FROM Category") suspend fun allOnce(): List<Category>
    @Query("SELECT * FROM Category WHERE name = :name COLLATE NOCASE LIMIT 1")
    suspend fun byName(name: String): Category?
    @Query("UPDATE Category SET colorArgb = :color WHERE id = :id")
    suspend fun setColor(id: Long, color: Long?)

    /** Remove a category and everything hanging off it (accidental adds). */
    @Transaction
    suspend fun deleteCascade(id: Long) {
        deletePrices(id); deletePurchases(id); deleteProducts(id); deleteRules(id)
        deleteIngredients(id); deleteAdhoc(id); deleteOnHand(id); deleteConfigs(id); deleteOverride(id)
        deleteCategory(id)
    }

    @Query("DELETE FROM Category WHERE id = :id") suspend fun deleteCategory(id: Long)
    @Query("DELETE FROM PricePoint WHERE storeProductId IN (SELECT id FROM StoreProduct WHERE categoryId = :id)")
    suspend fun deletePrices(id: Long)
    @Query("DELETE FROM Purchase WHERE storeProductId IN (SELECT id FROM StoreProduct WHERE categoryId = :id)")
    suspend fun deletePurchases(id: Long)
    @Query("DELETE FROM StoreProduct WHERE categoryId = :id") suspend fun deleteProducts(id: Long)
    @Query("DELETE FROM ConversionRuleEntity WHERE categoryId = :id") suspend fun deleteRules(id: Long)
    @Query("DELETE FROM RecipeIngredient WHERE categoryId = :id") suspend fun deleteIngredients(id: Long)
    @Query("DELETE FROM PlanAdhocItem WHERE categoryId = :id") suspend fun deleteAdhoc(id: Long)
    @Query("DELETE FROM OnHand WHERE categoryId = :id") suspend fun deleteOnHand(id: Long)
    @Query("DELETE FROM ScrapeConfig WHERE categoryId = :id") suspend fun deleteConfigs(id: Long)
    @Query("DELETE FROM BuyOverride WHERE categoryId = :id") suspend fun deleteOverride(id: Long)
}

@Dao
interface ProductDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insert(p: StoreProduct): Long
    @Query("SELECT * FROM StoreProduct WHERE categoryId = :categoryId")
    suspend fun byCategory(categoryId: Long): List<StoreProduct>
    @Query("SELECT * FROM StoreProduct WHERE store = :store AND storeSku = :sku")
    suspend fun bySku(store: String, sku: String): StoreProduct?
    @Query("SELECT * FROM StoreProduct")
    suspend fun all(): List<StoreProduct>
}

@Dao
interface PriceDao {
    @Insert suspend fun insert(p: PricePoint)
    @Query(
        """SELECT pp.* FROM PricePoint pp
           JOIN StoreProduct sp ON sp.id = pp.storeProductId
           WHERE sp.categoryId = :categoryId ORDER BY pp.timestampMs"""
    )
    suspend fun historyForCategory(categoryId: Long): List<PricePoint>
    @Query(
        """SELECT pp.* FROM PricePoint pp
           WHERE pp.storeProductId = :productId
           ORDER BY pp.timestampMs DESC LIMIT 1"""
    )
    suspend fun latestForProduct(productId: Long): PricePoint?
}

@Dao
interface PurchaseDao {
    @Insert suspend fun insert(p: Purchase)
    @Query(
        """SELECT pu.* FROM Purchase pu
           JOIN StoreProduct sp ON sp.id = pu.storeProductId
           WHERE sp.categoryId = :categoryId ORDER BY pu.timestampMs"""
    )
    suspend fun byCategory(categoryId: Long): List<Purchase>
    @Query(
        """SELECT pu.* FROM Purchase pu
           JOIN StoreProduct sp ON sp.id = pu.storeProductId
           WHERE sp.categoryId = :categoryId ORDER BY pu.timestampMs DESC LIMIT 1"""
    )
    suspend fun lastForCategory(categoryId: Long): Purchase?
}

@Dao
interface RuleDao {
    @Insert suspend fun insert(r: ConversionRuleEntity)
    /** Rules for this item plus global rules (categoryId = 0, apply to all items). */
    @Query("SELECT * FROM ConversionRuleEntity WHERE categoryId = :categoryId OR categoryId = 0")
    suspend fun byCategory(categoryId: Long): List<ConversionRuleEntity>
    @Query("SELECT * FROM ConversionRuleEntity") fun all(): Flow<List<ConversionRuleEntity>>
    @Query("DELETE FROM ConversionRuleEntity WHERE id = :id") suspend fun delete(id: Long)
}

@Dao
interface RecipeDao {
    @Insert suspend fun insert(r: Recipe): Long
    @Insert suspend fun insertIngredient(i: RecipeIngredient)
    @Query("SELECT * FROM Recipe ORDER BY name") fun all(): Flow<List<Recipe>>
    @Query("SELECT * FROM RecipeIngredient WHERE recipeId = :recipeId")
    suspend fun ingredients(recipeId: Long): List<RecipeIngredient>
    @Query("DELETE FROM RecipeIngredient WHERE id = :ingredientId")
    suspend fun deleteIngredient(ingredientId: Long)

    @Transaction
    suspend fun deleteRecipe(recipeId: Long) {
        deleteRecipeIngredients(recipeId); deleteFromPlan(recipeId); deleteRecipeRow(recipeId)
    }
    @Query("DELETE FROM RecipeIngredient WHERE recipeId = :recipeId")
    suspend fun deleteRecipeIngredients(recipeId: Long)
    @Query("DELETE FROM PlanRecipe WHERE recipeId = :recipeId") suspend fun deleteFromPlan(recipeId: Long)
    @Query("DELETE FROM Recipe WHERE id = :recipeId") suspend fun deleteRecipeRow(recipeId: Long)
}

@Dao
interface PlanDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun addRecipe(p: PlanRecipe)
    @Query("DELETE FROM PlanRecipe WHERE recipeId = :recipeId") suspend fun removeRecipe(recipeId: Long)
    @Query("SELECT * FROM PlanRecipe") fun recipes(): Flow<List<PlanRecipe>>
    @Insert suspend fun addAdhoc(i: PlanAdhocItem)
    @Query("DELETE FROM PlanAdhocItem WHERE id = :id") suspend fun removeAdhoc(id: Long)
    @Query("SELECT * FROM PlanAdhocItem") fun adhocItems(): Flow<List<PlanAdhocItem>>
    @Transaction
    suspend fun clearPlan() { clearPlanRecipes(); clearPlanAdhoc() }
    @Query("DELETE FROM PlanRecipe") suspend fun clearPlanRecipes()
    @Query("DELETE FROM PlanAdhocItem") suspend fun clearPlanAdhoc()
}

@Dao
interface OnHandDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(o: OnHand)
    @Query("SELECT * FROM OnHand") suspend fun all(): List<OnHand>
    @Query("SELECT * FROM OnHand") fun observe(): Flow<List<OnHand>>
}

@Dao
interface BuyOverrideDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(o: BuyOverride)
    @Query("SELECT * FROM BuyOverride") fun all(): Flow<List<BuyOverride>>
    @Query("DELETE FROM BuyOverride") suspend fun clear()
}

@Dao
interface ScrapeConfigDao {
    @Insert suspend fun insert(c: ScrapeConfig)
    @Query("SELECT * FROM ScrapeConfig WHERE enabled = 1") suspend fun enabled(): List<ScrapeConfig>
    @Query("SELECT * FROM ScrapeConfig") fun all(): Flow<List<ScrapeConfig>>
}
