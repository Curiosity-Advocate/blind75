"""Probe the unofficial Woolworths search endpoint and dump the response shape."""
import json
import sys

import requests

UA = ("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 "
      "(KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36")

session = requests.Session()
session.headers.update({"User-Agent": UA, "Accept": "application/json"})

# Woolworths gates the API behind cookies set on the main site — visit it first.
home = session.get("https://www.woolworths.com.au/", timeout=20)
print(f"homepage: {home.status_code}, cookies: {sorted(session.cookies.keys())[:8]}")

payload = {
    "SearchTerm": "toilet paper",
    "PageSize": 36,
    "PageNumber": 1,
    "SortType": "CUPAsc",
    "Location": "/shop/search/products?searchTerm=toilet%20paper",
}
resp = session.post(
    "https://www.woolworths.com.au/apis/ui/Search/products",
    json=payload,
    headers={"Origin": "https://www.woolworths.com.au",
             "Referer": "https://www.woolworths.com.au/shop/search/products?searchTerm=toilet%20paper"},
    timeout=20,
)
print(f"search: {resp.status_code}, content-type: {resp.headers.get('content-type')}")
if "json" not in (resp.headers.get("content-type") or ""):
    print(resp.text[:500])
    sys.exit(1)

data = resp.json()
print("top-level keys:", sorted(data.keys()))
products = data.get("Products") or []
print("product groups:", len(products))
if products:
    inner = products[0].get("Products") or []
    if inner:
        p = inner[0]
        keep = ["Stockcode", "DisplayName", "Price", "WasPrice", "CupPrice",
                "CupMeasure", "CupString", "PackageSize", "Unit", "IsAvailable",
                "IsOnSpecial", "Brand"]
        print(json.dumps({k: p.get(k) for k in keep}, indent=2))
        print("all product fields:", len(p), "keys")
# First 5 by unit price, as the app would track them
rows = []
for grp in products:
    for p in (grp.get("Products") or []):
        if p.get("Price") is not None:
            rows.append((p.get("CupPrice"), p.get("Price"), p.get("PackageSize"), p.get("DisplayName")))
for r in rows[:5]:
    print(r)
