
export interface UserProfile {
  user_id: string;
  email: string;
  orders: {
    total_orders: number;
    confirmed_orders: number;
    cancelled_orders: number;
    confirmation_rate: number;
    repeat_purchase_rate: number;
    avg_days_between_orders: number;
  };
  revenue: {
    total_revenue: number;
    avg_order_value: number;
    avg_revenue_per_confirmed_order: number;
    customer_lifetime_value: number;
  };
  profit: {
    avg_profit_per_order: number;
    total_estimated_profit: number;
    profit_margin: number;
  };
  price_behavior: {
    price_sensitivity: number;
    discount_usage_rate: number;
    avg_discount_rate_used: number;
  };
  behavior: {
    purchase_frequency_score: number;
    churn_risk: number;
    loyalty_score: number;
  };
  preferences: {
    favorite_categories: string[];
    top_products: string[];
    embedding?: number[];
  };
  segment: string;
  last_updated: string;
}

export interface AIAnalysis {
  summary: string;
  recommendations: string[];
  strategic_score: number;
}
