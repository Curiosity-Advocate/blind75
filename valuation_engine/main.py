from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from core.data_provider import data_store
from core.error_bounds import ErrorBoundsEngine
from models.capm import CAPMModel
from models.gordon_growth import GordonGrowthModel
from models.monte_carlo import MonteCarloModel
from models.dcf import DiscountedCashFlowModel

app = FastAPI(title="Mathematical Equity Valuation Engine", version="1.1.0")

class ValuationRequest(BaseModel):
    ticker: str
    market_ticker: str = "^AXJO"
    risk_free_rate: float = 0.041
    market_return_expectation: float = 0.09
    dividend_growth_rate: float = 0.025
    terminal_growth_rate: float = 0.020

@app.post("/api/v1/evaluate")
async def evaluate_asset_pricing(payload: ValuationRequest):
    try:
        data = data_store.get_market_series(payload.ticker, payload.market_ticker)
        vol_metrics = ErrorBoundsEngine.calculate_rolling_volatility_error(data["stock_returns"])
        
        # 1. Run CAPM to establish our risk-adjusted discount hurdle rate
        capm_output = CAPMModel.evaluate(
            data["stock_returns"], data["market_returns"], 
            payload.risk_free_rate, payload.market_return_expectation
        )
        
        # 2. Run Gordon Growth
        gordon_output = GordonGrowthModel.evaluate(
            data["latest_dividend"], 
            capm_output["required_return_baseline"], 
            payload.dividend_growth_rate
        )
        
        # 3. Run our new Multi-Period Cash Flow DCF Engine
        dcf_output = DiscountedCashFlowModel.evaluate(
            data["historical_cash_flows"],
            capm_output["required_return_baseline"],
            data["current_price"],
            data["asset_type"],
            payload.terminal_growth_rate
        )
        
        # 4. Run Monte Carlo simulations
        monte_carlo_output = MonteCarloModel.evaluate(
            data["current_price"], data["stock_returns"]
        )
        
        return {
            "ticker": payload.ticker,
            "current_market_price": data["current_price"],
            "statistical_errors": {
                "beta_standard_error": capm_output["beta_standard_error"],
                "historical_volatility": vol_metrics["baseline_volatility"],
                "volatility_margin_of_error": vol_metrics["volatility_margin_of_error"]
            },
            "valuations": {
                "capm_analysis": capm_output,
                "gordon_growth_analysis": gordon_output,
                "dcf_analysis": dcf_output,
                "monte_carlo_1y_forecast": monte_carlo_output
            }
        }
        
    except Exception as e:
        raise HTTPException(status_code=400, detail=str(e))

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main.py", host="127.0.0.1", port=8000, reload=True)
