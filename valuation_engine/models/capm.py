import numpy as np
from scipy import stats
from core.error_bounds import ErrorBoundsEngine

class CAPMModel:
    @staticmethod
    def evaluate(stock_returns: list, market_returns: list, rf: float, market_expectation: float) -> dict:
        s_arr = np.array(stock_returns)
        m_arr = np.array(market_returns)
        
        # Calculate ordinary least squares regression
        slope, intercept, r_val, p_val, std_err = stats.linregress(m_arr, s_arr)
        
        beta = slope
        beta_se = std_err  # The localized margin of error for beta
        
        mrp = market_expectation - rf
        beta_low, beta_high = ErrorBoundsEngine.get_95_confidence_interval(beta, beta_se)
        
        return {
            "beta_baseline": float(beta),
            "beta_standard_error": float(beta_se),
            "required_return_baseline": float(rf + (beta * mrp)),
            "required_return_lower_bound": float(rf + (beta_low * mrp)),
            "required_return_upper_bound": float(rf + (beta_high * mrp))
        }
