import numpy as np

class MonteCarloModel:
    @staticmethod
    def evaluate(current_price: float, stock_returns: list, days: int = 252, simulations: int = 5000) -> dict:
        arr = np.array(stock_returns)
        
        # Calculate drift parameters
        daily_mean = np.mean(arr)
        daily_var = np.var(arr)
        drift = daily_mean - (0.5 * daily_var)
        daily_vol = np.std(arr)
        
        # Vectorized generation across the step parameters
        shocks = np.random.normal(0, 1, (days, simulations))
        price_paths = np.exp(drift + daily_vol * shocks)
        terminal_prices = current_price * np.prod(price_paths, axis=0)
        
        return {
            "expected_median_price": float(np.percentile(terminal_prices, 50)),
            "downside_risk_5th_percentile": float(np.percentile(terminal_prices, 5)),
            "upside_potential_95th_percentile": float(np.percentile(terminal_prices, 95))
        }
