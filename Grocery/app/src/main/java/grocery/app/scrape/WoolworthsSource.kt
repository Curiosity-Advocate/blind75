package grocery.app.scrape

import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

/**
 * Fetches search results from the JSON endpoint backing woolworths.com.au search.
 *
 * Verified against the live endpoint (2026-07-12, via python probe):
 *  - The POST is rejected without the Akamai cookies (_abck, bm_*) set by a plain
 *    GET to the homepage, so [warmUp] must run once per session before searching.
 *  - SortType "CUPAsc" sorts by unit price ascending.
 *  - PackageSize can be empty (bulk/carton items); CupMeasure varies per product
 *    ("100ML", "100G", "1M"), so unit prices only compare within the same measure.
 *
 * Unofficial and may change without notice — every call site must tolerate failure,
 * and manual price entry remains the reliable path.
 */
class WoolworthsSource : PriceSource {

    override val store = "Woolworths"

    private val cookieStore = mutableMapOf<String, Cookie>()
    private val client = OkHttpClient.Builder()
        .cookieJar(object : CookieJar {
            override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
                cookies.forEach { cookieStore[it.name] = it }
            }
            override fun loadForRequest(url: HttpUrl): List<Cookie> = cookieStore.values.toList()
        })
        .build()

    private var warmedUp = false

    private fun warmUp() {
        if (warmedUp) return
        val request = Request.Builder()
            .url("https://www.woolworths.com.au/")
            .header("User-Agent", USER_AGENT)
            .build()
        client.newCall(request).execute().use { warmedUp = it.isSuccessful }
    }

    override fun search(query: String, topN: Int): List<ScrapedProduct> {
        warmUp()
        val body = JSONObject()
            .put("SearchTerm", query)
            .put("PageSize", 36)
            .put("PageNumber", 1)
            .put("SortType", "CUPAsc") // unit price, low to high
            .toString()
            .toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("https://www.woolworths.com.au/apis/ui/Search/products")
            .header("User-Agent", USER_AGENT)
            .header("Accept", "application/json")
            .header("Origin", "https://www.woolworths.com.au")
            .header("Referer", "https://www.woolworths.com.au/shop/search/products?searchTerm=$query")
            .post(body)
            .build()

        client.newCall(request).execute().use { resp ->
            if (!resp.isSuccessful) return emptyList()
            val text = resp.body?.string() ?: return emptyList()
            if (!text.trimStart().startsWith("{")) return emptyList() // bot-blocked HTML
            val root = JSONObject(text)
            val out = mutableListOf<ScrapedProduct>()
            val groups = root.optJSONArray("Products") ?: return emptyList()
            for (i in 0 until groups.length()) {
                val products = groups.getJSONObject(i).optJSONArray("Products") ?: continue
                for (j in 0 until products.length()) {
                    val p = products.getJSONObject(j)
                    val price = p.optDouble("Price", Double.NaN)
                    if (price.isNaN() || !p.optBoolean("IsAvailable", true)) continue
                    out += ScrapedProduct(
                        sku = p.optLong("Stockcode").toString(),
                        name = p.optString("DisplayName"),
                        price = price,
                        wasPrice = p.optDouble("WasPrice").takeIf { !it.isNaN() },
                        isOnSpecial = p.optBoolean("IsOnSpecial", false),
                        packSizeText = p.optString("PackageSize"),
                        unitPrice = p.optDouble("CupPrice").takeIf { !it.isNaN() },
                        unitMeasure = p.optString("CupMeasure"),
                    )
                    if (out.size >= topN) return out
                }
            }
            return out
        }
    }

    /**
     * Direct product detail (verified via tools/product_probe.py, 2026-07-12):
     * GET /apis/ui/product/detail/{stockcode} returns {"Product": {...same fields...}}.
     */
    override fun fetchProduct(sku: String, refreshKey: String): ScrapedProduct? {
        warmUp()
        val request = Request.Builder()
            .url("https://www.woolworths.com.au/apis/ui/product/detail/$sku")
            .header("User-Agent", USER_AGENT)
            .header("Accept", "application/json")
            .header("Referer", "https://www.woolworths.com.au/")
            .build()
        client.newCall(request).execute().use { resp ->
            if (!resp.isSuccessful) return null
            val text = resp.body?.string() ?: return null
            if (!text.trimStart().startsWith("{")) return null
            val p = JSONObject(text).optJSONObject("Product") ?: return null
            val price = p.optDouble("Price", Double.NaN)
            if (price.isNaN()) return null
            return ScrapedProduct(
                sku = p.optLong("Stockcode").toString(),
                name = p.optString("DisplayName"),
                price = price,
                wasPrice = p.optDouble("WasPrice").takeIf { !it.isNaN() },
                isOnSpecial = p.optBoolean("IsOnSpecial", false),
                packSizeText = p.optString("PackageSize"),
                unitPrice = p.optDouble("CupPrice").takeIf { !it.isNaN() },
                unitMeasure = p.optString("CupMeasure"),
            )
        }
    }

    private companion object {
        const val USER_AGENT =
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 " +
                "(KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36"
    }
}
