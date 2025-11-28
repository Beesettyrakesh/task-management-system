import { Task } from "@/types";
import React from "react";
import {
  getStatusBadgeColor,
  getPriorityBorderColor,
  getPriorityTextColor,
  formatDueDate,
  getDueDateStyle,
  formatStatusText,
} from "../utils/taskUtils";

interface TaskCardProps {
  task: Task;
}

const TaskCard: React.FC<TaskCardProps> = ({ task }) => {
  return (
    <div
      className={`
        bg-white rounded-lg shadow-md hover:shadow-lg transition-shadow duration-200
        border border-gray-200 p-6
        ${getPriorityBorderColor(task.priority)}
      `}
    >
      {/* Header with Title and Status */}
      <div className="flex justify-between items-start mb-3">
        <h3 className="text-lg font-semibold text-gray-900 flex-1 mr-3 line-clamp-2">
          {task.title}
        </h3>
        <span
          className={`
            px-3 py-1 rounded-full text-xs font-medium whitespace-nowrap
            ${getStatusBadgeColor(task.status)}
          `}
        >
          {formatStatusText(task.status)}
        </span>
      </div>

      {/* Description */}
      {task.description && (
        <p className="text-gray-600 text-sm mb-4 line-clamp-3">
          {task.description}
        </p>
      )}

      {/* Footer with Priority and Due Date */}
      <div className="flex justify-between items-center text-sm">
        <div className="flex items-center space-x-2">
          <span className="text-gray-500">Priority:</span>
          <span className={`font-medium ${getPriorityTextColor(task.priority)}`}>
            {task.priority}
          </span>
        </div>

        {task.dueDate && (
          <div className="flex items-center space-x-1">
            <svg
              className="w-4 h-4 text-gray-400"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"
              />
            </svg>
            <span className={getDueDateStyle(task.dueDate)}>
              {formatDueDate(task.dueDate)}
            </span>
          </div>
        )}
      </div>

      {/* Created Date */}
      <div className="mt-3 pt-3 border-t border-gray-100">
        <p className="text-xs text-gray-400">
          Created {new Date(task.createdAt).toLocaleDateString('en-US', {
            month: 'short',
            day: 'numeric',
            year: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
          })}
        </p>
      </div>
    </div>
  );
};

export default TaskCard;
