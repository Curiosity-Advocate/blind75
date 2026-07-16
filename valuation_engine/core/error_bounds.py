import numpy as np

class ErrorBoundsEngine:
    @staticmethod
    def calculate_rolling_volatility_error(returns: list) -> dict:
        """Computes short vs long window variance to establish an error boundary for volatility."""
        arr = np.array(returns)
        # Look at last 30 trading days vs total history
        short_vol = np.std(arr[-30:]) * np.sqrt(252)
        long_vol = np.std(arr) * np.sqrt(252)
        
        return {
            "baseline_volatility": float(long_vol),
            "volatility_margin_of_error": float(abs(long_vol - short_vol))
        }

    @staticmethod
    def get_95_confidence_interval(value: float, standard_error: float) -> tuple:
        """Applies a two-tailed Z-score structural adjustment boundary."""
        lower = value - (1.96 * standard_error)
        upper = value + (1.96 * standard_error)
        return float(lower), float(upper)
