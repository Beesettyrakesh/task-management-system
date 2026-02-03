import React from "react";
import { TaskStatistics } from "../types";

interface StatisticsOverviewProps {
  statistics: TaskStatistics;
}

const StatisticsOverview: React.FC<StatisticsOverviewProps> = ({
  statistics,
}) => {
  const stats = [
    {
      label: "Total Tasks",
      value: statistics.totalTasks,
      icon: "📋",
    },
    {
      label: "Completed",
      value: statistics.completedTasks,
      icon: "✅",
    },
    {
      label: "In Progress",
      value: statistics.inProgressTasks,
      icon: "🔄",
    },
    {
      label: "Overdue",
      value: statistics.overdueTasks,
      icon: "⚠️",
    },
  ];

  return (
    <div className="bg-white rounded-lg shadow-md p-6">
      <h3 className="text-lg font-semibold mb-4 text-gray-900">
        Tasks Overview
      </h3>
      <div className="space-y-3">
        {stats.map((stat, index) => (
          <div key={index} className="flex items-center justify-between">
            <div className="flex items-center">
              <span className="text-lg mr-3">{stat.icon}</span>
              <span className="text-sm font-medium text-gray-700">
                {stat.label}
              </span>
            </div>
            <span className="text-md font-bold text-gray-900">
              {stat.value}
            </span>
          </div>
        ))}
      </div>

      {statistics.totalTasks === 0 && (
        <div className="text-center text-gray-500 py-4 mt-4">
          <div className="text-3xl mb-2">📊</div>
          <p className="text-sm">No tasks yet</p>
        </div>
      )}
    </div>
  );
};

export default StatisticsOverview;
