"""Probe the unofficial Coles search endpoint (Next.js data route) and dump the shape.

Flow: GET homepage -> extract Next.js buildId -> GET /_next/data/{buildId}/en/search/products.json
"""
import json
import re
import sys

import requests

UA = ("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 "
      "(KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36")

session = requests.Session()
session.headers.update({
    "User-Agent": UA,
    "Accept": "text/html,application/xhtml+xml,application/json;q=0.9,*/*;q=0.8",
    "Accept-Language": "en-AU,en;q=0.9",
})

home = session.get("https://www.coles.com.au/", timeout=30)
print(f"homepage: {home.status_code}, cookies: {sorted(session.cookies.keys())[:8]}")
m = re.search(r'"buildId"\s*:\s*"([^"]+)"', home.text)
if not m:
    print("no buildId found; first 300 chars:")
    print(home.text[:300])
    sys.exit(1)
build_id = m.group(1)
print("buildId:", build_id)

query = sys.argv[1] if len(sys.argv) > 1 else "toilet paper"
resp = session.get(
    f"https://www.coles.com.au/_next/data/{build_id}/en/search/products.json",
    params={"q": query},
    headers={"Referer": f"https://www.coles.com.au/search/products?q={query}"},
    timeout=30,
)
print(f"search: {resp.status_code}, content-type: {resp.headers.get('content-type')}")
if "json" not in (resp.headers.get("content-type") or ""):
    print(resp.text[:300])
    sys.exit(1)

data = resp.json()
results = data["pageProps"]["searchResults"]
print("searchResults keys:", sorted(results.keys()))
products = [r for r in results.get("results", []) if r.get("_type") == "PRODUCT"]
print("products:", len(products), "of", results.get("noOfResults"))
if products:
    p = products[0]
    print("product keys:", sorted(p.keys()))
    print(json.dumps({
        "id": p.get("id"),
        "name": p.get("name"),
        "brand": p.get("brand"),
        "size": p.get("size"),
        "pricing": p.get("pricing"),
        "availability": p.get("availability"),
    }, indent=2))
for p in products[1:4]:
    pr = p.get("pricing") or {}
    unit = pr.get("unit") or {}
    print((pr.get("now"), unit.get("price"), unit.get("ofMeasureUnits"), p.get("size"), p.get("name")))
