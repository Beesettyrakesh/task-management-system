import { Task } from "../types";
import React, { useEffect, useState } from "react";
import API from "../services/api";
import TaskCard from "./TaskCard";
import { TaskFilters } from "./FilterControls";
import { LoadingPage } from "./LoadingSpinner";

interface TaskListProps {
  onSuccess?: () => void;
  filters?: TaskFilters;
  refreshTrigger?: number;
}

const TaskList: React.FC<TaskListProps> = ({ onSuccess, filters, refreshTrigger }) => {
  const [tasks, setTasks] = useState<Task[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [activeTab, setActiveTab] = useState<'active' | 'completed'>('active');

  useEffect(() => {
    const fetchTasks = async () => {
      try {
        setError(null);
        const params = new URLSearchParams();
        if(filters?.status) params.append('status', filters.status);
        if(filters?.priority) params.append('priority', filters.priority);
        if(filters?.sortBy) params.append('sortBy', filters.sortBy);
        if(filters?.sortDirection) params.append('sortDirection', filters.sortDirection);
        if(filters?.tagName) params.append('tagName', filters.tagName);

        const queryString = params.toString();
        const url = queryString ? `/tasks?${queryString}` : '/tasks';

        const response = await API.get(url);
        setTasks(response.data);
      } catch (error: any) {
        console.error("Error fetching tasks:", error);
        setError(error.response?.data?.message || "Failed to fetch tasks");
      } finally {
        setLoading(false);
      }
    };
    fetchTasks();
  }, [filters, refreshTrigger]);

  if (loading) {
    return <LoadingPage text="Loading your tasks..." />;
  }

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

  // Separate active and completed tasks
  const activeTasks = tasks.filter(task => task.status !== 'DONE');
  const completedTasks = tasks.filter(task => task.status === 'DONE');

  // Determine if tabs should be visible
  const isStatusFilterApplied = filters?.status;
  const shouldShowTabs = !isStatusFilterApplied;

  if (tasks.length === 0) {
    const hasActiveFilters = filters?.status || filters?.priority || filters?.tagName;
    
    return (
      <div className="text-center py-12">
        <div className="max-w-md mx-auto">
          {hasActiveFilters ? (
            <>
              <svg
                className="w-20 h-20 text-gray-400 mx-auto mb-6"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={1.5}
                  d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
                />
              </svg>
              <h3 className="text-xl font-semibold text-gray-900 mb-3">
                No tasks match your filters
              </h3>
              <p className="text-gray-600 mb-6">
                Try adjusting your search or filter criteria to find what you're looking for.
              </p>
            </>
          ) : (
            <>
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
                No tasks yet
              </h3>
              <p className="text-gray-600 mb-6">
                Get started by creating your first task!
              </p>
            </>
          )}
        </div>
      </div>
    );
  }

  // Determine which tasks to show based on tab and filters
  const tasksToDisplay = shouldShowTabs 
    ? (activeTab === 'active' ? activeTasks : completedTasks)
    : tasks;

  return (
    <div className="space-y-6">
      {/* Tabs - Only show if no status filter */}
      {shouldShowTabs && (
        <div className="border-b border-gray-200 pt-2">
          <div className="flex space-x-8">
            <button
              onClick={() => setActiveTab('active')}
              className={`pb-3 px-1 border-b-2 font-medium text-sm transition-colors ${
                activeTab === 'active'
                  ? 'border-blue-600 text-blue-600'
                  : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
              }`}
            >
              Active
              <span className={`ml-2 py-0.5 px-2 rounded-full text-xs ${
                activeTab === 'active' 
                  ? 'bg-blue-100 text-blue-600' 
                  : 'bg-gray-100 text-gray-600'
              }`}>
                {activeTasks.length}
              </span>
            </button>
            
            <button
              onClick={() => setActiveTab('completed')}
              className={`pb-3 px-1 border-b-2 font-medium text-sm transition-colors ${
                activeTab === 'completed'
                  ? 'border-blue-600 text-blue-600'
                  : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
              }`}
            >
              Completed
              <span className={`ml-2 py-0.5 px-2 rounded-full text-xs ${
                activeTab === 'completed' 
                  ? 'bg-blue-100 text-blue-600' 
                  : 'bg-gray-100 text-gray-600'
              }`}>
                {completedTasks.length}
              </span>
            </button>
          </div>
        </div>
      )}

      {/* Task Stats */}
      <div className="flex justify-between items-center text-sm text-gray-600">
        <span className="font-medium">
          {tasksToDisplay.length} task{tasksToDisplay.length !== 1 ? "s" : ""}
          {shouldShowTabs && ` ${activeTab === 'active' ? 'active' : 'completed'}`}
        </span>
        {!shouldShowTabs && (
          <div className="flex items-center space-x-4">
            <span>{completedTasks.length} completed</span>
            <span>•</span>
            <span>{activeTasks.length} remaining</span>
          </div>
        )}
      </div>

      {/* Task List */}
      {tasksToDisplay.length > 0 ? (
        <div className={`grid grid-cols-1 gap-4 ${activeTab === 'completed' ? 'opacity-75' : ''}`}>
          {tasksToDisplay.map((task) => (
            <TaskCard key={task.id} task={task} refreshDashboard={onSuccess} />
          ))}
        </div>
      ) : (
        <div className="text-center py-12">
          <div className="max-w-md mx-auto">
            {activeTab === 'active' ? (
              <>
                <svg
                  className="w-16 h-16 text-gray-400 mx-auto mb-4"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={1.5}
                    d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"
                  />
                </svg>
                <h3 className="text-lg font-semibold text-gray-900 mb-2">
                  No active tasks
                </h3>
                <p className="text-gray-600">
                  All tasks are completed! Great job! 🎉
                </p>
              </>
            ) : (
              <>
                <svg
                  className="w-16 h-16 text-gray-400 mx-auto mb-4"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={1.5}
                    d="M9 5H7a2 2 0 00-2 2v10a2 2 0 002 2h8a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"
                  />
                </svg>
                <h3 className="text-lg font-semibold text-gray-900 mb-2">
                  No completed tasks yet
                </h3>
                <p className="text-gray-600">
                  Complete some tasks to see them here
                </p>
              </>
            )}
          </div>
        </div>
      )}
    </div>
  );
};

export default TaskList;
