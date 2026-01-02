import { TaskStatistics } from "@/types";
import React from "react";

interface StatisticsCardProps {
  statistics: TaskStatistics;
}

const StatisticsCard: React.FC<StatisticsCardProps> = ({ statistics }) => {
  const cards = [
    {
      title: "Total Tasks",
      value: statistics.totalTasks,
      icon: "📋",
      color: "bg-blue-500",
    },
    {
      title: "Completed",
      value: statistics.completedTasks,
      icon: "✅",
      color: "bg-green-500",
    },
    {
      title: "In Progress",
      value: statistics.inProgressTasks,
      icon: "🔄",
      color: "bg-yellow-500",
    },
    {
      title: "Overdue",
      value: statistics.overdueTasks,
      icon: "⚠️",
      color: "bg-red-500",
    },
  ];

  return (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
      {cards.map((card, index) => (
        <div key={index} className="bg-white rounded-lg shadow-md p-6">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-gray-600 text-sm font-medium">{card.title}</p>
              <p className="text-3xl font-bold text-gray-900">{card.value}</p>
            </div>
            <div
              className={`${card.color} text-white p-3 rounded-full text-2xl`}
            >
              {card.icon}
            </div>
          </div>
        </div>
      ))}
    </div>
  );
};

export default StatisticsCard;
