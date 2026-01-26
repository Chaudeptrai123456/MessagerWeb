
import React from 'react';
import { 
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, 
  PieChart, Pie, Cell, RadarChart, PolarGrid, PolarAngleAxis, PolarRadiusAxis, Radar 
} from 'recharts';
import { UserProfile } from '../../../../type';

export const BehavioralRadar: React.FC<{ behavior: UserProfile['behavior'] }> = ({ behavior }) => {
  const data = [
    { subject: 'Frequency', A: behavior.purchase_frequency_score * 100, fullMark: 100 },
    { subject: 'Loyalty', A: behavior.loyalty_score * 100, fullMark: 100 },
    { subject: 'Churn Risk (Inv)', A: (1 - behavior.churn_risk) * 100, fullMark: 100 },
  ];

  return (
    <div className="h-[250px] w-full">
      <ResponsiveContainer width="100%" height="100%">
        <RadarChart cx="50%" cy="50%" outerRadius="80%" data={data}>
          <PolarGrid />
          <PolarAngleAxis dataKey="subject" />
          <PolarRadiusAxis angle={30} domain={[0, 100]} />
          <Radar name="Behavior" dataKey="A" stroke="#4f46e5" fill="#4f46e5" fillOpacity={0.6} />
        </RadarChart>
      </ResponsiveContainer>
    </div>
  );
};

export const OrderPie: React.FC<{ orders: UserProfile['orders'] }> = ({ orders }) => {
  const data = [
    { name: 'Confirmed', value: orders.confirmed_orders },
    { name: 'Cancelled', value: orders.cancelled_orders },
  ];
  const COLORS = ['#10b981', '#ef4444'];

  return (
    <div className="h-[200px] w-full">
      <ResponsiveContainer width="100%" height="100%">
        <PieChart>
          <Pie
            data={data}
            cx="50%"
            cy="50%"
            innerRadius={60}
            outerRadius={80}
            paddingAngle={5}
            dataKey="value"
          >
            {data.map((entry, index) => (
              <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
            ))}
          </Pie>
          <Tooltip />
        </PieChart>
      </ResponsiveContainer>
    </div>
  );
};
