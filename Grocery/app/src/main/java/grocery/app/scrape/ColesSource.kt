package grocery.app.scrape

import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

/**
 * Fetches search results from the Next.js data route backing coles.com.au search.
 *
 * Verified against the live endpoint (2026-07-12, via tools/coles_probe.py):
 *  - A plain GET to the homepage sets the Incapsula cookies and its HTML embeds
 *    the Next.js "buildId" needed to construct the data URL.
 *  - GET /_next/data/{buildId}/en/search/products.json?q=... returns
 *    pageProps.searchResults.results[] with _type == "PRODUCT".
 *  - Results are NOT sorted by unit price (no server-side sort param on this
 *    route), so we sort client-side on pricing.unit.price.
 */
class ColesSource : PriceSource {

    override val store = "Coles"

    private val cookieStore = mutableMapOf<String, Cookie>()
    private val client = OkHttpClient.Builder()
        .cookieJar(object : CookieJar {
            override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
                cookies.forEach { cookieStore[it.name] = it }
            }
            override fun loadForRequest(url: HttpUrl): List<Cookie> = cookieStore.values.toList()
        })
        .build()

    private var buildId: String? = null

    /** Homepage GET: sets Incapsula cookies and yields the Next.js buildId. */
    private fun ensureBuildId(): String? {
        buildId?.let { return it }
        val request = Request.Builder()
            .url("https://www.coles.com.au/")
            .header("User-Agent", USER_AGENT)
            .header("Accept-Language", "en-AU,en;q=0.9")
            .build()
        client.newCall(request).execute().use { resp ->
            if (!resp.isSuccessful) return null
            val html = resp.body?.string() ?: return null
            buildId = Regex("\"buildId\"\\s*:\\s*\"([^\"]+)\"").find(html)?.groupValues?.get(1)
        }
        return buildId
    }

    override fun search(query: String, topN: Int): List<ScrapedProduct> {
        val id = ensureBuildId() ?: return emptyList()
        val url = "https://www.coles.com.au/_next/data/$id/en/search/products.json".toHttpUrl()
            .newBuilder().addQueryParameter("q", query).build()
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", USER_AGENT)
            .header("Accept", "application/json")
            .header("Referer", "https://www.coles.com.au/search/products?q=$query")
            .build()

        client.newCall(request).execute().use { resp ->
            if (!resp.isSuccessful) {
                buildId = null // build IDs rotate on deploys; refetch next run
                return emptyList()
            }
            val text = resp.body?.string() ?: return emptyList()
            if (!text.trimStart().startsWith("{")) return emptyList() // bot-blocked HTML
            val results = JSONObject(text)
                .optJSONObject("pageProps")
                ?.optJSONObject("searchResults")
                ?.optJSONArray("results") ?: return emptyList()

            val out = mutableListOf<ScrapedProduct>()
            for (i in 0 until results.length()) {
                val p = results.getJSONObject(i)
                if (p.optString("_type") != "PRODUCT") continue
                out += parseProduct(p) ?: continue
            }
            // This route has no unit-price sort; order client-side, unpriced last.
            return out.sortedBy { it.unitPrice ?: Double.MAX_VALUE }.take(topN)
        }
    }

    /**
     * Direct product refresh (verified via tools/product_probe.py, 2026-07-12):
     * GET /_next/data/{buildId}/en/product/{slug}.json -> pageProps.product,
     * same pricing shape as search. The slug ("brand-name-size-id") is captured
     * at scrape time as [ScrapedProduct.refreshKey]; if missing, rebuild is not
     * attempted here since name formatting can drift — the search path still covers it.
     */
    override fun fetchProduct(sku: String, refreshKey: String): ScrapedProduct? {
        if (refreshKey.isBlank()) return null
        val id = ensureBuildId() ?: return null
        val request = Request.Builder()
            .url("https://www.coles.com.au/_next/data/$id/en/product/$refreshKey.json")
            .header("User-Agent", USER_AGENT)
            .header("Accept", "application/json")
            .header("Referer", "https://www.coles.com.au/product/$refreshKey")
            .build()
        client.newCall(request).execute().use { resp ->
            if (!resp.isSuccessful) {
                buildId = null
                return null
            }
            val text = resp.body?.string() ?: return null
            if (!text.trimStart().startsWith("{")) return null
            val product = JSONObject(text).optJSONObject("pageProps")?.optJSONObject("product") ?: return null
            return parseProduct(product)
        }
    }

    /** Shared parser: search results and the product route use the same shape. */
    private fun parseProduct(p: JSONObject): ScrapedProduct? {
        if (!p.optBoolean("availability", true)) return null
        val pricing = p.optJSONObject("pricing") ?: return null
        val price = pricing.optDouble("now", Double.NaN)
        if (price.isNaN()) return null
        val unit = pricing.optJSONObject("unit")
        val measure = unit?.let {
            "${it.optInt("ofMeasureQuantity", 1)}${it.optString("ofMeasureUnits")}"
        } ?: ""
        val brand = p.optString("brand")
        val name = p.optString("name")
        val size = p.optString("size")
        val sku = p.optLong("id").toString()
        val slug = "$brand $name $size".lowercase()
            .replace(Regex("[^a-z0-9]+"), "-").trim('-') + "-$sku"
        return ScrapedProduct(
            sku = sku,
            name = listOf(brand, name).filter { it.isNotBlank() }.joinToString(" "),
            price = price,
            wasPrice = pricing.optDouble("was").takeIf { !it.isNaN() && it > 0 },
            isOnSpecial = pricing.optBoolean("onlineSpecial", false),
            packSizeText = size,
            unitPrice = unit?.optDouble("price")?.takeIf { !it.isNaN() },
            unitMeasure = measure,
            refreshKey = slug,
        )
    }

    private companion object {
        const val USER_AGENT =
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 " +
                "(KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36"
    }
}
