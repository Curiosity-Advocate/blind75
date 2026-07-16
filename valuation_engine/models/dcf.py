import numpy as np

class DiscountedCashFlowModel:
    @staticmethod
    def evaluate(historical_flows: list, required_return: float, current_price: float, 
                 asset_type: str, terminal_growth: float = 0.02) -> dict:
        """
        Executes a Multi-Period Valuation Matrix.
        Forks automatically based on asset classification profiles:
        - Stocks: Traditional Free Cash Flow (FCF) multi-period discounting.
        - ETFs: Net Asset Value (NAV) + Present Value of Expected Yield Distributions.
        """
        # --- PATHWAY A: INDEX TRACKING FUNDS & ETFS ---
        if asset_type == "ETF":
            if not historical_flows or max(historical_flows) <= 0:
                return {
                    "intrinsic_value": float(current_price),
                    "status": "Valid (ETF NAV Baseline Tracking)",
                    "note": "ETF contains no corporate cash retaining lines. Valuation set to direct underlying net assets."
                }
            
            # For an ETF, the intrinsic baseline is its Net Asset Value backing (proxied by current price)
            # plus the capitalized value of its average annual distribution yield.
            avg_annual_distribution = float(np.mean(historical_flows))
            
            # Capitalise the dividend pass-through yield stream using the CAPM hurdle rate premium
            pv_of_yield_stream = avg_annual_distribution / required_return
            
            # Blended valuation adjustment: asset liquidation value weighted with yield premium energy
            adjusted_intrinsic_value = (current_price * 0.85) + (pv_of_yield_stream * 0.15)
            
            return {
                "intrinsic_value": float(adjusted_intrinsic_value),
                "calculated_growth_baseline": 0.0, # ETFs possess no corporate internal growth
                "growth_margin_of_error": 0.0,
                "status": "Valid (ETF Adjusted Model)",
                "note": "Priced using Net Asset Value backing and capitalised pass-through distributions."
            }

        # --- PATHWAY B: STANDARD INDIVIDUAL CORPORATE STOCKS ---
        if not historical_flows or len(historical_flows) < 2 or max(historical_flows) <= 0:
            return {"intrinsic_value": 0.0, "status": "Invalid: Insufficient historical cash generation profiles."}

        growths = []
        for i in range(1, len(historical_flows)):
            if historical_flows[i-1] > 0:
                growths.append((historical_flows[i] - historical_flows[i-1]) / historical_flows[i-1])
        
        avg_growth = float(np.mean(growths)) if growths else 0.03
        growth_std_err = float(np.std(growths)) if len(growths) > 1 else 0.01

        if required_return <= terminal_growth:
            return {"intrinsic_value": 0.0, "status": "Unstable: Hurdle rate is lower than long term decay constants."}

        base_cash = historical_flows[-1]
        projected_flows = []
        discounted_flows = []
        
        for year in range(1, 6):
            future_cash = base_cash * ((1 + avg_growth) ** year)
            projected_flows.append(future_cash)
            discounted_flows.append(future_cash / ((1 + required_return) ** year))

        terminal_value = (projected_flows[-1] * (1 + terminal_growth)) / (required_return - terminal_growth)
        discounted_terminal_value = terminal_value / ((1 + required_return) ** 5)
        total_intrinsic_value = sum(discounted_flows) + discounted_terminal_value

        return {
            "intrinsic_value": float(total_intrinsic_value),
            "calculated_growth_baseline": avg_growth,
            "growth_margin_of_error": growth_std_err,
            "status": "Valid (Corporate Stock Model)",
            "five_year_projections": [float(x) for x in projected_flows]
        }
