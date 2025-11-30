import API from "../services/api";
import { Task } from "@/types";
import React, { useEffect, useState } from "react";
import TaskCard from "./TaskCard";

interface TaskListProps {
  onSuccess?: () => void
}

const TaskList: React.FC<TaskListProps> = ({ onSuccess }) => {
  const [tasks, setTasks] = useState<Task[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchTasks = async () => {
      try {
        setError(null);
        const response = await API.get("/tasks");
        setTasks(response.data);
      } catch (error: any) {
        console.error("Error fetching tasks:", error);
        setError(error.response?.data?.message || "Failed to fetch tasks");
      } finally {
        setLoading(false);
      }
    };
    fetchTasks();
  }, []);

  // Loading state with spinner
  if (loading) {
    return (
      <div className="flex justify-center items-center py-12">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto mb-4"></div>
          <p className="text-gray-600">Loading your tasks...</p>
        </div>
      </div>
    );
  }

  // Error state
  if (error) {
    return (
      <div className="text-center py-12">
        <div className="bg-red-50 border border-red-200 rounded-lg p-6 max-w-md mx-auto">
          <svg
            className="w-12 h-12 text-red-500 mx-auto mb-4"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
            />
          </svg>
          <h3 className="text-lg font-semibold text-red-800 mb-2">
            Unable to Load Tasks
          </h3>
          <p className="text-red-600 text-sm mb-4">{error}</p>
          <button
            onClick={() => window.location.reload()}
            className="bg-red-600 text-white px-4 py-2 rounded-lg hover:bg-red-700 transition-colors"
          >
            Try Again
          </button>
        </div>
      </div>
    );
  }

  // Empty state
  if (tasks.length === 0) {
    return (
      <div className="text-center py-12">
        <div className="max-w-md mx-auto">
          <svg
            className="w-20 h-20 text-gray-400 mx-auto mb-6"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={1}
              d="M9 5H7a2 2 0 00-2 2v10a2 2 0 002 2h8a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-6 9l2 2 4-4"
            />
          </svg>
          <h3 className="text-xl font-semibold text-gray-900 mb-3">
            No tasks found
          </h3>
          <p className="text-gray-600 mb-6">
            You haven't created any tasks yet. Create your first task to get started!
          </p>
        </div>
      </div>
    );
  }

  // Tasks display with responsive grid
  return (
    <div className="space-y-4">
      {/* Tasks summary */}
      <div className="flex justify-between items-center text-sm text-gray-600 pb-2">
        <span className="font-medium">
          {tasks.length} task{tasks.length !== 1 ? 's' : ''} total
        </span>
        <div className="flex items-center space-x-4">
          <span>
            {tasks.filter(task => task.status === 'DONE').length} completed
          </span>
          <span>•</span>
          <span>
            {tasks.filter(task => task.status !== 'DONE').length} remaining
          </span>
        </div>
      </div>

      {/* Tasks grid - responsive layout */}
      <div className="grid grid-cols-1 gap-4">
        {tasks.map((task) => (
          <TaskCard key={task.id} task={task} refreshDashboard={onSuccess}/>
        ))}
      </div>
    </div>
  );
};

export default TaskList;
