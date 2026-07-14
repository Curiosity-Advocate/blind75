"""Probe direct product-by-ID endpoints for weekly price refresh.

Woolworths: GET /apis/ui/product/detail/{stockcode}
Coles:      GET /_next/data/{buildId}/en/product/{slug}.json  (slug from search results)
"""
import json
import re
import sys

import requests

UA = ("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 "
      "(KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36")


def woolworths():
    print("=== Woolworths product detail ===")
    s = requests.Session()
    s.headers.update({"User-Agent": UA, "Accept": "application/json"})
    s.get("https://www.woolworths.com.au/", timeout=20)
    # Get a stockcode from search first, then fetch it directly.
    search = s.post("https://www.woolworths.com.au/apis/ui/Search/products",
                    json={"SearchTerm": "olive oil", "PageSize": 5, "PageNumber": 1, "SortType": "CUPAsc"},
                    headers={"Origin": "https://www.woolworths.com.au",
                             "Referer": "https://www.woolworths.com.au/shop/search/products?searchTerm=olive%20oil"},
                    timeout=20).json()
    stockcode = search["Products"][0]["Products"][0]["Stockcode"]
    print("stockcode from search:", stockcode)
    r = s.get(f"https://www.woolworths.com.au/apis/ui/product/detail/{stockcode}",
              headers={"Referer": "https://www.woolworths.com.au/"}, timeout=20)
    print("detail:", r.status_code, r.headers.get("content-type"))
    if "json" in (r.headers.get("content-type") or ""):
        d = r.json()
        print("top-level keys:", sorted(d.keys())[:15])
        p = d.get("Product") or d
        print(json.dumps({k: p.get(k) for k in
                          ["Stockcode", "DisplayName", "Price", "CupPrice", "CupMeasure",
                           "PackageSize", "IsAvailable", "IsOnSpecial"]}, indent=2))
    else:
        print(r.text[:200])


def coles():
    print("=== Coles product data route ===")
    s = requests.Session()
    s.headers.update({"User-Agent": UA,
                      "Accept": "text/html,application/xhtml+xml,application/json;q=0.9,*/*;q=0.8",
                      "Accept-Language": "en-AU,en;q=0.9"})
    home = s.get("https://www.coles.com.au/", timeout=30)
    build_id = re.search(r'"buildId"\s*:\s*"([^"]+)"', home.text).group(1)
    print("buildId:", build_id)
    search = s.get(f"https://www.coles.com.au/_next/data/{build_id}/en/search/products.json",
                   params={"q": "olive oil"},
                   headers={"Referer": "https://www.coles.com.au/search/products?q=olive%20oil"},
                   timeout=30).json()
    products = [r for r in search["pageProps"]["searchResults"]["results"] if r.get("_type") == "PRODUCT"]
    p = products[0]
    pid, name, brand = p["id"], p["name"], (p.get("brand") or "")
    print("product:", pid, brand, name)
    # Coles product URLs look like /product/{brand-name-size-id}; build the slug.
    slug_bits = f"{brand} {name} {p.get('size') or ''}".lower()
    slug = re.sub(r"[^a-z0-9]+", "-", slug_bits).strip("-") + f"-{pid}"
    print("trying slug:", slug)
    r = s.get(f"https://www.coles.com.au/_next/data/{build_id}/en/product/{slug}.json",
              headers={"Referer": f"https://www.coles.com.au/product/{slug}"}, timeout=30)
    print("product route:", r.status_code, r.headers.get("content-type"))
    if "json" in (r.headers.get("content-type") or ""):
        d = r.json()
        pp = d.get("pageProps", {})
        print("pageProps keys:", sorted(pp.keys())[:15])
        prod = pp.get("product") or {}
        if prod:
            print(json.dumps({"id": prod.get("id"), "name": prod.get("name"),
                              "size": prod.get("size"), "pricing": prod.get("pricing"),
                              "availability": prod.get("availability")}, indent=2))
    else:
        print(r.text[:200])


if __name__ == "__main__":
    which = sys.argv[1] if len(sys.argv) > 1 else "both"
    if which in ("both", "woolworths"):
        woolworths()
    if which in ("both", "coles"):
        coles()
