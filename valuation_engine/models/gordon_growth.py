class GordonGrowthModel:
    @staticmethod
    def evaluate(latest_dividend: float, required_return: float, growth_rate: float = 0.025) -> dict:
        if latest_dividend <= 0:
            return {"intrinsic_value": 0.0, "status": "Invalid: No dividends paid."}
            
        next_dividend = latest_dividend * (1 + growth_rate)
        
        if required_return <= growth_rate:
            return {"intrinsic_value": 0.0, "status": "Unstable: Growth exceeds required return."}
            
        val = next_dividend / (required_return - growth_rate)
        return {"intrinsic_value": float(val), "status": "Valid"}
