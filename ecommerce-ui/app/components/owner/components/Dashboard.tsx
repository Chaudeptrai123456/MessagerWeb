import React, { useContext, useEffect, useState } from "react";
import {
  User,
  ShoppingCart,
  TrendingUp,
  DollarSign,
  Percent,
  RefreshCcw,
  AlertTriangle,
  Star,
  Zap,
  BrainCircuit,
  Loader2,
  ChevronRight,
  ArrowUpRight,
  ArrowDownRight,
  Activity,
  Terminal,
  Code2,
  Server,
} from "lucide-react";
import { UserProfile, AIAnalysis } from "../../../../type";
import { API_PATHS } from "@/utils/apiPaths";
import { BehavioralRadar, OrderPie } from "./charts";
import { apiClient } from "@/utils/axios.client";
import { UserContext } from "@/context/UserContext";
const backend = apiClient("BACKEND");
const KPICard: React.FC<{
  title: string;
  value: string | number;
  subValue?: string;
  icon: React.ReactNode;
  trend?: "up" | "down";
  color?: string;
}> = ({ title, value, subValue, icon, trend, color = "blue" }) => (
  <div className="bg-white p-6 rounded-2xl shadow-sm border border-slate-100 flex flex-col justify-between h-full hover:shadow-md transition-all">
    <div className="flex justify-between items-start mb-4">
      <div className={`p-3 rounded-xl bg-${color}-50 text-${color}-600`}>
        {icon}
      </div>
      {trend && (
        <span
          className={`flex items-center text-xs font-semibold px-2 py-1 rounded-full ${
            trend === "up"
              ? "bg-emerald-50 text-emerald-600"
              : "bg-rose-50 text-rose-600"
          }`}
        >
          {trend === "up" ? (
            <ArrowUpRight className="w-3 h-3 mr-1" />
          ) : (
            <ArrowDownRight className="w-3 h-3 mr-1" />
          )}
          {trend === "up" ? "Positive" : "Action Needed"}
        </span>
      )}
    </div>
    <div>
      <h3 className="text-slate-500 text-sm font-medium mb-1">{title}</h3>
      <div className="flex items-baseline gap-2">
        <span className="text-2xl font-bold text-slate-900">{value}</span>
      </div>
      {subValue && <p className="text-xs text-slate-400 mt-1">{subValue}</p>}
    </div>
  </div>
);

const App: React.FC = () => {
  const [aiAnalysis, setAiAnalysis] = useState<AIAnalysis | null>(null);
  const [isAnalyzing, setIsAnalyzing] = useState(false);
  const [view, setView] = useState<"dashboard" | "backend">("dashboard");
  const ctx = useContext(UserContext);
  if (!ctx) return null;
  const { user, loading } = ctx;
  const fetchWarehouses = async () => {
    try {
      const token = user.token;
      const res = await backend.get(API_PATHS.WAREHOUSE.ECOMMERCE, {
        headers: { Authorization: token ? `Bearer ${token}` : "" },
      });
      console.log("Fetched warehouses:", res.data);
    } catch (err) {
      console.error("Error fetching warehouses:", err);
    }
  };
  useEffect(() => {
    fetchWarehouses();
  }, [user.token]);
  const handleAIAnalysis = async () => {
    setIsAnalyzing(true);
    try {
    } catch (err) {
      console.error(err);
    } finally {
      setIsAnalyzing(false);
    }
  };

  const formatCurrency = (val: number) =>
    new Intl.NumberFormat("vi-VN", {
      style: "currency",
      currency: "VND",
    }).format(val);

  return (
    <div className="min-h-screen bg-slate-50 pb-12">
      {/* Navbar */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 pt-8">
        {view === "dashboard" ? (
          <>
            {/* Header Profile Section */}
            <div className="bg-gradient-to-r from-indigo-700 to-indigo-900 rounded-3xl p-8 mb-8 text-white flex flex-col md:flex-row justify-between items-start md:items-center gap-6 shadow-xl">
              <div className="flex items-center gap-5">
                <div className="w-20 h-20 bg-white/20 backdrop-blur-md rounded-2xl flex items-center justify-center border border-white/30">
                  <User className="w-10 h-10" />
                </div>
                <div>
                  <h2 className="text-2xl font-bold">Email</h2>
                  <div className="flex items-center gap-3 mt-1 opacity-90">
                    <span className="px-3 py-1 bg-white/10 rounded-full text-xs font-semibold tracking-wide border border-white/20 uppercase">
                      id
                    </span>
                    <span className="px-3 py-1 bg-amber-400 text-amber-950 rounded-full text-xs font-bold tracking-wide uppercase">
                      profile
                    </span>
                  </div>
                </div>
              </div>
              <div className="flex gap-4">
                <div className="text-right">
                  <p className="text-indigo-100 text-sm opacity-80">
                    Lifetime Value (CLV)
                  </p>
                  <p className="text-2xl font-bold">test</p>
                </div>
              </div>
            </div>

            {/* AI Insight Box */}
            {aiAnalysis && (
              <div className="bg-white rounded-3xl border-2 border-indigo-100 shadow-xl p-8 mb-8 relative overflow-hidden">
                <div className="absolute top-0 right-0 p-4 opacity-10 pointer-events-none">
                  <BrainCircuit className="w-32 h-32" />
                </div>
                <div className="flex items-center gap-3 mb-6">
                  <div className="p-2 bg-indigo-100 text-indigo-600 rounded-lg">
                    <Zap className="w-5 h-5 fill-current" />
                  </div>
                  <h3 className="text-xl font-bold text-slate-900">
                    AI Strategic Insights
                  </h3>
                  <div className="ml-auto bg-indigo-50 text-indigo-700 px-4 py-1 rounded-full text-sm font-bold border border-indigo-200">
                    Strategic Score: {aiAnalysis.strategic_score}/100
                  </div>
                </div>
                <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
                  <div>
                    <h4 className="text-sm font-semibold text-slate-400 uppercase tracking-wider mb-2">
                      Executive Summary
                    </h4>
                    <p className="text-slate-700 leading-relaxed text-lg italic">
                      "{aiAnalysis.summary}"
                    </p>
                  </div>
                  <div>
                    <h4 className="text-sm font-semibold text-slate-400 uppercase tracking-wider mb-4">
                      Actionable Recommendations
                    </h4>
                    <ul className="space-y-3">
                      {aiAnalysis.recommendations.map((rec, i) => (
                        <li
                          key={i}
                          className="flex items-start gap-3 bg-slate-50 p-4 rounded-xl border border-slate-100 text-slate-700 shadow-sm"
                        >
                          <div className="mt-1 flex-shrink-0 w-5 h-5 bg-indigo-600 text-white rounded-full flex items-center justify-center text-[10px] font-bold">
                            {i + 1}
                          </div>
                          <span className="text-sm font-medium">{rec}</span>
                        </li>
                      ))}
                    </ul>
                  </div>
                </div>
              </div>
            )}

            {/* KPI Grid */}
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
              <KPICard
                title="Total Revenue"
                value={10}
                icon={<DollarSign />}
                color="indigo"
              />
              <KPICard
                title="Order Frequency"
                value={10}
                subValue="subvalue"
                icon={<ShoppingCart />}
                trend="up"
                color="emerald"
              />
              <KPICard
                title="Churn Risky"
                value={10}
                subValue="subvalue"
                icon={<ShoppingCart />}
                trend="up"
                color="emerald"
              />{" "}
              <KPICard
                title="Profit Margin"
                value={10}
                subValue="subvalue"
                icon={<ShoppingCart />}
                trend="up"
                color="emerald"
              />
              {/* <KPICard
                title="Churn Risk"
                value={`${(profile.behavior.churn_risk * 100).toFixed(1)}%`}
                icon={<AlertTriangle />}
                trend={profile.behavior.churn_risk > 0.3 ? "down" : "up"}
                color="rose"
              />
              <KPICard
                title="Profit Margin"
                value={`${(profile.profit.profit_margin * 100).toFixed(1)}%`}
                subValue={`${formatCurrency(
                  profile.profit.avg_profit_per_order
                )} / order`}
                icon={<TrendingUp />}
                color="blue" */}
              {/* /> */}
            </div>

            {/* Charts Section */}
            <div className="grid grid-cols-1 lg:grid-cols-3 gap-8 mb-8">
              <div className="bg-white p-6 rounded-3xl shadow-sm border border-slate-100">
                <h3 className="text-lg font-bold text-slate-900 mb-6 flex items-center gap-2">
                  <RefreshCcw className="w-5 h-5 text-indigo-600" />
                  Order Health
                </h3>
              </div>
              <div className="bg-white p-6 rounded-3xl shadow-sm border border-slate-100">
                <h3 className="text-lg font-bold text-slate-900 mb-6 flex items-center gap-2">
                  <Activity className="w-5 h-5 text-indigo-600" />
                  Behavioral Metrics
                </h3>
              </div>
              <div className="bg-white p-6 rounded-3xl shadow-sm border border-slate-100">
                <h3 className="text-lg font-bold text-slate-900 mb-6 flex items-center gap-2">
                  <Percent className="w-5 h-5 text-indigo-600" />
                  Pricing Sensitivity
                </h3>
              </div>
            </div>
          </>
        ) : (
          <div className="bg-slate-900 rounded-3xl overflow-hidden shadow-2xl border border-slate-800">
            <div className="p-6 bg-slate-800 border-b border-slate-700 flex items-center justify-between">
              <div className="flex items-center gap-3">
                <div className="flex gap-1.5">
                  <div className="w-3 h-3 rounded-full bg-rose-500" />
                  <div className="w-3 h-3 rounded-full bg-amber-500" />
                  <div className="w-3 h-3 rounded-full bg-emerald-500" />
                </div>
                <div className="flex items-center gap-2 text-slate-300 font-mono text-sm ml-4">
                  <Server className="w-4 h-4" />
                  main.py
                </div>
              </div>
              <div className="text-xs text-slate-400 font-mono">
                FastAPI + Uvicorn Server
              </div>
            </div>
            <div className="p-8">
              <div className="mb-8">
                <h3 className="text-white text-lg font-semibold mb-4 flex items-center gap-2">
                  <Terminal className="w-5 h-5 text-indigo-400" />
                  1. Aggregation Strategy
                </h3>
                <p className="text-slate-400 text-sm mb-4">
                  Server sẽ quét collection <code>orders</code> trong Qdrant để
                  lọc ra các đơn hàng của user, sau đó áp dụng các công thức:
                </p>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div className="bg-black/30 p-4 rounded-xl border border-slate-700">
                    <span className="text-emerald-400 font-mono text-xs">
                      # Confirmation Rate
                    </span>
                    <p className="text-white text-sm">
                      confirmed_orders / total_orders
                    </p>
                  </div>
                  <div className="bg-black/30 p-4 rounded-xl border border-slate-700">
                    <span className="text-indigo-400 font-mono text-xs">
                      # Churn Risk
                    </span>
                    <p className="text-white text-sm">
                      1.0 - (total_orders / target_freq)
                    </p>
                  </div>
                </div>
              </div>

              <div className="mb-8">
                <h3 className="text-white text-lg font-semibold mb-4 flex items-center gap-2">
                  <Code2 className="w-5 h-5 text-emerald-400" />
                  2. Core logic: calculate_user_profile
                </h3>
                <div className="bg-black/50 p-6 rounded-xl font-mono text-slate-300 text-xs overflow-x-auto border border-slate-800 leading-relaxed">
                  <pre>{`def calculate_user_profile(email: str):
    # Lấy orders từ Qdrant
    res = client.scroll(collection_name="orders", 
                        scroll_filter=Filter(must=[FieldCondition(key="email", match=MatchValue(value=email))]))
    orders = [p.payload for p in res[0]]
    
    # Tính các metrics
    total = len(orders)
    confirmed = sum(1 for o in orders if o['status'] == 'confirmed')
    revenue = sum(o['total_price'] for o in orders if o['status'] == 'confirmed')
    
    # Phân loại Segment tự động
    segment = "high_value" if revenue > 10000000 else "regular"
    
    return {
        "user_id": f"u-{hash(email)}",
        "orders": { "total_orders": total, "confirmation_rate": confirmed/total ... },
        "revenue": { "total_revenue": revenue, ... },
        "segment": segment
    }`}</pre>
                </div>
              </div>

              <div>
                <h3 className="text-white text-lg font-semibold mb-4 flex items-center gap-2">
                  <Activity className="w-5 h-5 text-amber-400" />
                  3. New Endpoint
                </h3>
                <div className="bg-black/50 p-4 rounded-xl font-mono text-emerald-400 text-sm border border-slate-800 flex justify-between items-center">
                  <span>GET /api/user_profile/{"{email}"}</span>
                  <span className="text-slate-500 text-xs">
                    Returns full UserProfileResponse
                  </span>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* Footer info */}
        <div className="mt-8 text-center text-slate-400 text-sm">
          Built for Customer Analytics by Châu. Powered by Gemini Flash 3.
        </div>
      </main>
    </div>
  );
};

export default App;
