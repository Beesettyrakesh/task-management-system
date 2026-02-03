import React, { useEffect, useState } from "react";
import API from "../services/api";
import { Task } from "../types";
import { TaskFilters } from "./FilterControls";
import { LoadingPage } from "./LoadingSpinner";
import TaskCard from "./TaskCard";

interface TaskListProps {
  onSuccess?: () => void;
  filters?: TaskFilters;
  refreshTrigger?: number;
}

const TaskList: React.FC<TaskListProps> = ({
  onSuccess,
  filters,
  refreshTrigger,
}) => {
  const [tasks, setTasks] = useState<Task[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [activeTab, setActiveTab] = useState<"active" | "completed">("active");
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);
  const [isLoadingMore, setIsLoadingMore] = useState(false);

  const fetchTasks = async (pageNum: number = 0, append: boolean = false) => {
    try {
      setError(null);
      if (append) {
        setIsLoadingMore(true);
      } else {
        setLoading(true);
      }

      const params = new URLSearchParams();
      if (filters?.status) params.append("status", filters.status);
      if (filters?.priority) params.append("priority", filters.priority);
      if (filters?.sortBy) params.append("sortBy", filters.sortBy);
      if (filters?.sortDirection)
        params.append("sortDirection", filters.sortDirection);
      if (filters?.tagName) params.append("tagName", filters.tagName);

      params.append("page", pageNum.toString());
      params.append("size", "20");

      const queryString = params.toString();
      const url = queryString ? `/tasks?${queryString}` : "/tasks";

      const response = await API.get(url);

      if (response.data.content) {
        if (append) {
          setTasks((prev) => [...prev, ...response.data.content]);
        } else {
          setTasks(response.data.content);
        }
        setHasMore(response.data.number < response.data.totalPages - 1);
      } else {
        setTasks(response.data);
        setHasMore(false);
      }
    } catch (error: any) {
      console.error("Error fetching tasks:", error);
      setError(error.response?.data?.message || "Failed to fetch tasks");
    } finally {
      setLoading(false);
      setIsLoadingMore(false);
    }
  };

  const handleLoadMore = () => {
    const nextPage = page + 1;
    setPage(nextPage);
    fetchTasks(nextPage, true);
  };

  useEffect(() => {
    setPage(0);
    fetchTasks(0, false);
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

  const activeTasks = tasks.filter((task) => task.status !== "DONE");
  const completedTasks = tasks.filter((task) => task.status === "DONE");

  const isStatusFilterApplied = filters?.status;
  const shouldShowTabs = !isStatusFilterApplied;

  if (tasks.length === 0) {
    const hasActiveFilters =
      filters?.status || filters?.priority || filters?.tagName;

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
                Try adjusting your search or filter criteria to find what you're
                looking for.
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

  const tasksToDisplay = shouldShowTabs
    ? activeTab === "active"
      ? activeTasks
      : completedTasks
    : tasks;

  return (
    <div className="space-y-6">
      {shouldShowTabs && (
        <div className="border-b border-gray-200 pt-2">
          <div className="flex space-x-8">
            <button
              onClick={() => setActiveTab("active")}
              className={`pb-3 px-1 border-b-2 font-medium text-sm transition-colors ${
                activeTab === "active"
                  ? "border-blue-600 text-blue-600"
                  : "border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300"
              }`}
            >
              Active
              <span
                className={`ml-2 py-0.5 px-2 rounded-full text-xs ${
                  activeTab === "active"
                    ? "bg-blue-100 text-blue-600"
                    : "bg-gray-100 text-gray-600"
                }`}
              >
                {activeTasks.length}
              </span>
            </button>

            <button
              onClick={() => setActiveTab("completed")}
              className={`pb-3 px-1 border-b-2 font-medium text-sm transition-colors ${
                activeTab === "completed"
                  ? "border-blue-600 text-blue-600"
                  : "border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300"
              }`}
            >
              Completed
              <span
                className={`ml-2 py-0.5 px-2 rounded-full text-xs ${
                  activeTab === "completed"
                    ? "bg-blue-100 text-blue-600"
                    : "bg-gray-100 text-gray-600"
                }`}
              >
                {completedTasks.length}
              </span>
            </button>
          </div>
        </div>
      )}

      <div className="flex justify-between items-center text-sm text-gray-600">
        <span className="font-medium">
          {tasksToDisplay.length} task{tasksToDisplay.length !== 1 ? "s" : ""}
          {shouldShowTabs &&
            ` ${activeTab === "active" ? "active" : "completed"}`}
        </span>
        {!shouldShowTabs && (
          <div className="flex items-center space-x-4">
            <span>{completedTasks.length} completed</span>
            <span>•</span>
            <span>{activeTasks.length} remaining</span>
          </div>
        )}
      </div>

      {tasksToDisplay.length > 0 ? (
        <div
          className={`grid grid-cols-1 gap-4 ${activeTab === "completed" ? "opacity-75" : ""}`}
        >
          {tasksToDisplay.map((task) => (
            <TaskCard key={task.id} task={task} refreshDashboard={onSuccess} />
          ))}
        </div>
      ) : (
        <div className="text-center py-12">
          <div className="max-w-md mx-auto">
            {activeTab === "active" ? (
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

      {hasMore && tasks.length > 0 && !loading && (
        <div className="mt-6 text-center">
          <button
            onClick={handleLoadMore}
            disabled={isLoadingMore}
            className="px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 
                 disabled:bg-gray-400 disabled:cursor-not-allowed 
                 transition-colors duration-200 font-medium shadow-md 
                 hover:shadow-lg"
          >
            {isLoadingMore ? (
              <span className="flex items-center justify-center">
                <svg
                  className="animate-spin -ml-1 mr-3 h-5 w-5 text-white"
                  xmlns="http://www.w3.org/2000/svg"
                  fill="none"
                  viewBox="0 0 24 24"
                >
                  <circle
                    className="opacity-25"
                    cx="12"
                    cy="12"
                    r="10"
                    stroke="currentColor"
                    strokeWidth="4"
                  ></circle>
                  <path
                    className="opacity-75"
                    fill="currentColor"
                    d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
                  ></path>
                </svg>
                Loading more...
              </span>
            ) : (
              "Load More Tasks"
            )}
          </button>
        </div>
      )}
    </div>
  );
};

export default TaskList;
