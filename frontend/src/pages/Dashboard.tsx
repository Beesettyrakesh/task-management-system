import React, { useEffect, useState } from "react";
import FilterControls, { TaskFilters } from "../components/FilterControls";
import Layout from "../components/Layout";
import Modal from "../components/Modal";
import TaskForm from "../components/TaskForm";
import TaskList from "../components/TaskList";
import StatisticsOverview from "../components/StatisticsOverview";
import PriorityChart from "../components/PriorityChart";
import TagOverview from "../components/TagOverview";
import SearchBar from "../components/SearchBar";
import { useAuth } from "../hooks/useAuth";
import {
  Priority,
  Task,
  TaskFormData,
  TaskStatistics,
  TaskStatus,
} from "../types";
import { getRecentTasks, getTaskStatistics } from "@/services/api";
import toast from "react-hot-toast";

const Dashboard: React.FC = () => {
  const { user } = useAuth();
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [refreshTrigger, setRefreshTrigger] = useState(0);
  const [statistics, setStatistics] = useState<TaskStatistics | null>(null);
  const [recentTasks, setRecentTasks] = useState<Task[]>([]);
  const [loading, setLoading] = useState(true);
  const [filters, setFilters] = useState<TaskFilters>({
    sortDirection: "asc",
  });

  useEffect(() => {
    const fetchDashboardData = async () => {
      try {
        const [statsData, recentData] = await Promise.all([
          getTaskStatistics(),
          getRecentTasks(),
        ]);
        setStatistics(statsData);
        setRecentTasks(recentData);
      } catch (error) {
        toast.error("Failed to load dashboard data");
      } finally {
        setLoading(false);
      }
    };

    fetchDashboardData();
  }, []);

  const handleCreateTask = () => {
    setIsCreateModalOpen(true);
  };

  const handleTaskCreated = () => {
    setIsCreateModalOpen(false);
    handleRefresh();
  };

  const handleStatusUpdated = () => {
    handleRefresh();
  };

  const handleFiltersChange = (newFilters: TaskFilters) => {
    setFilters(newFilters);
  };

  const handleClearFilters = () => {
    setFilters({ sortDirection: "asc" });
  };

  const handleSearchChange = (search: string) => {
    setFilters({ ...filters, search });
  };

  const handleRefresh = async () => {
    console.log('🔄 Dashboard refresh triggered at:', new Date().toLocaleTimeString());
    console.trace('Refresh call stack:'); // This will show what called handleRefresh
    
    setRefreshTrigger((prev) => prev + 1);
    
    try {
      const [statsData, recentData] = await Promise.all([
        getTaskStatistics(),
        getRecentTasks(),
      ]);
      setStatistics(statsData);
      setRecentTasks(recentData);
      console.log('✅ Dashboard data refreshed successfully');
    } catch (error) {
      console.error('❌ Failed to refresh dashboard data:', error);
    }
  };

  const currentDate = new Date().toLocaleDateString("en-US", {
    weekday: "long",
    year: "numeric",
    month: "long",
    day: "numeric",
  });

  const currentTime = new Date().toLocaleTimeString("en-US", {
    hour: "2-digit",
    minute: "2-digit",
  });

  const taskDefaultValues: TaskFormData = {
    title: "",
    description: "",
    dueDate: new Date(),
    priority: {
      value: Priority.MEDIUM,
      label: "",
    },
    status: {
      value: TaskStatus.TODO,
      label: "",
    },
    tags: [],
  };

  return (
    <Layout>
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          <div className="lg:col-span-2 space-y-6">
            <div className="bg-white shadow-lg rounded-lg border border-gray-200">
              <div className="px-6 py-4 border-b border-gray-200 flex justify-between items-center">
                <h2 className="text-xl font-semibold text-gray-900">
                  Your Tasks
                </h2>
                <button
                  onClick={handleCreateTask}
                  className="text-blue-600 hover:text-blue-800 font-medium text-sm flex items-center space-x-1 transition-colors"
                >
                  <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
                  </svg>
                  <span>Create Task</span>
                </button>
              </div>
              <div className="p-6">
                <FilterControls
                  filters={filters}
                  onFiltersChange={handleFiltersChange}
                  onClearFilters={handleClearFilters}
                />

                <TaskList
                  onSuccess={handleStatusUpdated}
                  filters={filters}
                />
              </div>
            </div>
          </div>

          <div className="space-y-6">
            <SearchBar
              searchValue={filters.search || ""}
              onSearchChange={handleSearchChange}
              placeholder="Search tasks..."
            />

            <TagOverview onTagsChange={handleRefresh} />

            {statistics ? (
              <StatisticsOverview statistics={statistics} />
            ) : (
              <div className="bg-white rounded-lg shadow-md p-6 animate-pulse">
                <div className="h-6 bg-gray-200 rounded mb-4"></div>
                <div className="space-y-3">
                  {[1, 2, 3, 4].map((i) => (
                    <div key={i} className="flex items-center justify-between">
                      <div className="flex items-center">
                        <div className="w-4 h-4 bg-gray-200 rounded mr-3"></div>
                        <div className="h-4 bg-gray-200 rounded w-20"></div>
                      </div>
                      <div className="h-5 bg-gray-200 rounded w-8"></div>
                    </div>
                  ))}
                </div>
              </div>
            )}

            {statistics && (
              <PriorityChart priorityData={statistics.tasksByPriority} />
            )}
          </div>
        </div>
      </div>

      <Modal
        isOpen={isCreateModalOpen}
        onClose={() => setIsCreateModalOpen(false)}
        size="xl"
      >
        <TaskForm
          taskDefaultValues={taskDefaultValues}
          onSuccess={handleTaskCreated}
          onCancel={() => setIsCreateModalOpen(false)}
        />
      </Modal>
    </Layout>
  );
};

export default Dashboard;
