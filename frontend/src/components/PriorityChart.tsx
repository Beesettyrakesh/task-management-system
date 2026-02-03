import React from "react";
import { Priority } from "../types";

interface PriorityChartProps {
  priorityData: Record<Priority, number>;
}

const PriorityChart: React.FC<PriorityChartProps> = ({ priorityData }) => {
  const maxValue = Math.max(...Object.values(priorityData));

  const getPriorityColor = (priority: Priority): string => {
    switch (priority) {
      case Priority.HIGH:
        return "bg-red-500";
      case Priority.MEDIUM:
        return "bg-yellow-500";
      case Priority.LOW:
        return "bg-green-500";
      default:
        return "bg-gray-500";
    }
  };

  const getPriorityLabel = (priority: Priority): string => {
    switch (priority) {
      case Priority.HIGH:
        return "High Priority";
      case Priority.MEDIUM:
        return "Medium Priority";
      case Priority.LOW:
        return "Low Priority";
      default:
        return priority;
    }
  };

  return (
    <div className="bg-white rounded-lg shadow-md p-6">
      <h3 className="text-lg font-semibold mb-4 text-gray-900">
        Tasks by Priority
      </h3>
      <div className="space-y-4">
        {Object.entries(priorityData).map(([priority, count]) => (
          <div key={priority} className="flex items-center">
            <span className="w-24 text-sm font-medium text-gray-700">
              {getPriorityLabel(priority as Priority)}
            </span>
            <div className="flex-1 bg-gray-200 rounded-full h-4 ml-4 relative">
              <div
                className={`h-4 rounded-full transition-all duration-500 ${getPriorityColor(priority as Priority)}`}
                style={{
                  width: `${maxValue > 0 ? (count / maxValue) * 100 : 0}%`,
                }}
              />
            </div>
            <span className="ml-4 text-sm font-bold text-gray-900 min-w-[2rem] text-right">
              {count}
            </span>
          </div>
        ))}
      </div>

      {maxValue === 0 && (
        <div className="text-center text-gray-500 py-8">
          <div className="text-4xl mb-2">📊</div>
          <p className="text-sm">No tasks to display</p>
        </div>
      )}
    </div>
  );
};

export default PriorityChart;
