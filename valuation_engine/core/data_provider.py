import sqlite3
import pandas as pd
import yfinance as yf
from datetime import datetime, timedelta
import os

class SQLiteDataProvider:
    def __init__(self, db_path: str = "valuation_engine.db"):
        self.db_path = db_path
        self._initialize_db()

    def _initialize_db(self):
        """Creates the local database tables matching native Android SQLite configurations."""
        with sqlite3.connect(self.db_path) as conn:
            cursor = conn.cursor()
            # Table for daily price series points
            cursor.execute("""
                CREATE TABLE IF NOT EXISTS historical_prices (
                    ticker TEXT,
                    date TEXT,
                    close_price REAL,
                    PRIMARY KEY (ticker, date)
                )
            """)
            conn.commit()

    def _get_local_date_bounds(self, ticker: str) -> tuple:
        """Returns the earliest and latest available dates stored in the local DB for a ticker."""
        with sqlite3.connect(self.db_path) as conn:
            cursor = conn.cursor()
            cursor.execute(
                "SELECT MIN(date), MAX(date) FROM historical_prices WHERE ticker = ?", 
                (ticker,)
            )
            row = cursor.fetchone()
            if row and row[0] and row[1]:
                return datetime.strptime(row[0], "%Y-%m-%d"), datetime.strptime(row[1], "%Y-%m-%d")
        return None, None

    def _load_local_series(self, ticker: str, start_date: str, end_date: str) -> pd.Series:
        """Retrieves historical prices directly from the local SQLite database."""
        with sqlite3.connect(self.db_path) as conn:
            query = """
                SELECT date, close_price FROM historical_prices 
                WHERE ticker = ? AND date BETWEEN ? AND ?
                ORDER BY date ASC
            """
            df = pd.read_sql_query(query, conn, params=(ticker, start_date, end_date))
            if not df.empty:
                df['date'] = pd.to_datetime(df['date'])
                return df.set_index('date')['close_price']
        return pd.Series(dtype='float64')

    def _save_to_local_db(self, ticker: str, df: pd.DataFrame):
        """Saves newly downloaded external API data points into the local database."""
        if df.empty:
            return
        
        # Flatten columns if Multi-Index
        if isinstance(df.columns, pd.MultiIndex):
            df.columns = df.columns.get_level_values(0)
            
        col = 'Adj Close' if 'Adj Close' in df.columns else 'Close'
        
        with sqlite3.connect(self.db_path) as conn:
            cursor = conn.cursor()
            for date, row in df.iterrows():
                date_str = date.strftime("%Y-%m-%d")
                price = float(row[col])
                if pd.notna(price):
                    # INSERT OR IGNORE avoids throwing errors on overlapping historic dates
                    cursor.execute("""
                        INSERT OR IGNORE INTO historical_prices (ticker, date, close_price)
                        VALUES (?, ?, ?)
                    """, (ticker, date_str, price))
            conn.commit()

    def get_market_series(self, ticker: str, market_ticker: str = "^AXJO", period_years: int = 3) -> dict:
        """
        Coordinates historical data acquisition. 
        Checks local database first; fetches from Yahoo Finance and updates the database only if missing data.
        """
        end_dt = datetime.now()
        start_dt = end_dt - timedelta(days=period_years * 365)
        
        start_str = start_dt.strftime("%Y-%m-%d")
        end_str = end_dt.strftime("%Y-%m-%d")

        # 1. Check local database history bounds
        stock_min, stock_max = self._get_local_date_bounds(ticker)
        market_min, market_max = self._get_local_date_bounds(market_ticker)

        # Determine if we need to pull fresh data from the internet
        needs_stock_download = stock_min is None or stock_max is None or (stock_max < (end_dt - timedelta(days=3)))
        needs_market_download = market_min is None or market_max is None or (market_max < (end_dt - timedelta(days=3)))

        # 2. Make external API calls only when data is missing locally
        if needs_stock_download:
            print(f"[API CALL] Downloading missing data for {ticker}...")
            stock_download = yf.download(ticker, period=f"{period_years}y", interval="1d", auto_adjust=False, progress=False)
            self._save_to_local_db(ticker, stock_download)

        if needs_market_download:
            print(f"[API CALL] Downloading missing data for {market_ticker}...")
            market_download = yf.download(market_ticker, period=f"{period_years}y", interval="1d", auto_adjust=False, progress=False)
            self._save_to_local_db(market_ticker, market_download)

        # 3. Pull historical records directly out of our clean local SQLite DB
        stock_series = self._load_local_series(ticker, start_str, end_str)
        market_series = self._load_local_series(market_ticker, start_str, end_str)

        if stock_series.empty or market_series.empty:
            raise ValueError("Failed to retrieve valid pricing series from local or external databases.")

        # 4. Align historical arrays and compute percentage changes
        stock_rets = stock_series.pct_change().dropna()
        market_rets = market_series.pct_change().dropna()
        
        combined = pd.DataFrame({'stock': stock_rets, 'market': market_rets}).dropna()

        # Fetch secondary metadata parameters
        ticker_obj = yf.Ticker(ticker)
        info = ticker_obj.info
        
        quote_type = info.get('quoteType', 'EQUITY')
        asset_type = "ETF" if quote_type == "ETF" else "STOCK"

        div_history = ticker_obj.dividends
        historical_cash_flows = [0.0]
        if not div_history.empty:
            annual_divs = div_history.resample('YE').sum()
            historical_cash_flows = annual_divs.tail(4).values.flatten().tolist()

        return {
            "asset_type": asset_type,
            "stock_returns": combined['stock'].values.flatten().tolist(),
            "market_returns": combined['market'].values.flatten().tolist(),
            "current_price": float(info.get('currentPrice', info.get('regularMarketPrice', 0.0))),
            "latest_dividend": float(info.get('dividendRate', 0.0)) if info.get('dividendRate') is not None else 0.0,
            "historical_cash_flows": historical_cash_flows,
            "shares_outstanding": float(info.get('sharesOutstanding', 1.0))
        }

data_store = SQLiteDataProvider()
