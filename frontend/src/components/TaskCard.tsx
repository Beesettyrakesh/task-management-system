import React, { useEffect, useRef, useState } from "react";
import { Controller, SubmitHandler, useForm } from "react-hook-form";
import toast from "react-hot-toast";
import Select from "react-select";
import API from "../services/api";
import { SelectOption, Task, TaskFormData, TaskStatus } from "../types";
import { customSelectStyles } from "../utils/selectStyles";
import {
  formatDueDate,
  formatStatusText,
  getDueDateStyle,
  getPriorityBorderColor,
  getPriorityTextColor,
  getStatusBadgeColor,
} from "../utils/taskUtils";
import Modal from "./Modal";
import TaskForm from "./TaskForm";

interface TaskCardProps {
  task: Task;
  refreshDashboard?: () => void;
  taskDefaultValues?: TaskFormData;
}

const TaskCard: React.FC<TaskCardProps> = ({ task, refreshDashboard, taskDefaultValues }) => {
  const [isStatusDropdownOpen, setIsStatusDropdownOpen] = useState(false);
  const [isUpdating, setIsUpdating] = useState(false);
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [isDeleting, setIsDeleting] = useState(false);
  const dropdownRef = useRef<HTMLDivElement>(null);

  const currentStatusOption: SelectOption<TaskStatus> = {
    value: task.status,
    label: task.status
      .replace("_", " ")
      .toLowerCase()
      .replace(/\b\w/g, (l) => l.toUpperCase()),
  };

  const taskStatusOptions: SelectOption<TaskStatus>[] = Object.values(
    TaskStatus
  ).map((status) => ({
    value: status,
    label: status
      .replace("_", " ")
      .toLowerCase()
      .replace(/\b\w/g, (l) => l.toUpperCase()),
  }));

  const handleTaskUpdated = () => {
    setIsCreateModalOpen(false);
    refreshDashboard?.();
  };

  const handleDeleteTask = async () => {
    setIsDeleting(true);
    try {
      await API.delete(`/tasks/${task.id}`);
      toast.success("Task deleted successfully!");
      setIsDeleteModalOpen(false);
      refreshDashboard?.();
    } catch (error: any) {
      console.error("Error deleting task:", error);
      toast.error(error.response?.data?.message || "Failed to delete task");
    } finally {
      setIsDeleting(false);
    }
  };

  const { handleSubmit, control } = useForm<{
    status: SelectOption<TaskStatus>;
  }>({
    defaultValues: {
      status: currentStatusOption,
    },
  });

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (
        dropdownRef.current &&
        !dropdownRef.current.contains(event.target as Node)
      ) {
        setIsStatusDropdownOpen(false);
      }
    };

    if (isStatusDropdownOpen) {
      document.addEventListener("mousedown", handleClickOutside);
      return () =>
        document.removeEventListener("mousedown", handleClickOutside);
    }
  }, [isStatusDropdownOpen]);

  const onsubmit: SubmitHandler<{ status: SelectOption<TaskStatus> }> = async (
    data
  ) => {
    setIsUpdating(true);

    try {
      const updatedTask = {
        title: task.title,
        description: task.description,
        dueDate: task.dueDate,
        priority: task.priority,
        status: data.status?.value, // Only this changes
      };

      await API.put(`/tasks/${task.id}`, updatedTask);
      refreshDashboard?.();
      setIsStatusDropdownOpen(false);
      toast.success("Task updated successfully!");
    } catch (error: any) {
      toast.error("Failed to update task");
    } finally {
      setIsUpdating(false);
    }
  };

  useEffect(() => {
    const handleEscape = (event: KeyboardEvent) => {
      if (event.key === "Escape") {
        setIsStatusDropdownOpen(false);
      }
    };

    if (isStatusDropdownOpen) {
      document.addEventListener("keydown", handleEscape);
      return () => document.removeEventListener("keydown", handleEscape);
    }
  }, [isStatusDropdownOpen]);

  return (
    <div
      className={`
        bg-white rounded-lg shadow-md hover:shadow-lg transition-shadow duration-200
        border border-gray-200 p-6
        ${getPriorityBorderColor(task.priority)}
      `}
    >
      <div className="flex justify-between items-start mb-3">
        <h3 className="text-lg font-semibold text-gray-900 flex-1 mr-3 line-clamp-2">
          {task.title}
        </h3>
        {isStatusDropdownOpen ? (
          <form onSubmit={handleSubmit(onsubmit)}>
            <div ref={dropdownRef} className="relative">
              <Controller
                name="status"
                control={control}
                render={({ field }) => (
                  <Select
                    {...field}
                    options={taskStatusOptions}
                    placeholder="Change status..."
                    autoFocus
                    menuPlacement="auto"
                    className="react-select-container"
                    classNamePrefix="react-select"
                    styles={customSelectStyles}
                    isDisabled={isUpdating}
                    onChange={(newValue) => {
                      field.onChange(newValue);
                      handleSubmit(onsubmit)();
                    }}
                  />
                )}
              />
            </div>
          </form>
        ) : (
          <button
            onClick={() => setIsStatusDropdownOpen(true)}
            className={`
            group flex items-center space-x-1 px-3 py-1 rounded-full text-xs font-medium
            ${getStatusBadgeColor(task.status)}
            hover:shadow-md hover:scale-105 transition-all duration-200 cursor-pointer
            border border-transparent hover:border-white/20
          `}
          >
            <span>{formatStatusText(task.status)}</span>
            <svg
              className="w-3 h-3 group-hover:rotate-180 transition-transform duration-200"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M19 9l-7 7-7-7"
              />
            </svg>
          </button>
        )}
      </div>

      {task.description && (
        <p className="text-gray-600 text-sm mb-4 line-clamp-3">
          {task.description}
        </p>
      )}

      <div className="flex justify-between items-center text-sm">
        <div className="flex items-center space-x-2">
          <span className="text-gray-500">Priority:</span>
          <span
            className={`font-medium ${getPriorityTextColor(task.priority)}`}
          >
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

      <div className="mt-3 pt-3 border-t border-gray-100">
        <div className="flex justify-between items-center">
          <p className="text-xs text-gray-400">
            Created{" "}
            {new Date(task.createdAt).toLocaleDateString("en-US", {
              month: "short",
              day: "numeric",
              year: "numeric",
              hour: "2-digit",
              minute: "2-digit",
            })}
          </p>
          
          <div className="flex space-x-2">
            <button
              onClick={() => setIsCreateModalOpen(true)}
              className="p-1 text-gray-400 hover:text-blue-600 hover:bg-blue-50 rounded transition-colors"
              title="Edit task"
            >
              <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
              </svg>
            </button>
            
            <button
              onClick={() => setIsDeleteModalOpen(true)}
              className="p-1 text-gray-400 hover:text-red-600 hover:bg-red-50 rounded transition-colors"
              title="Delete task"
            >
              <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
              </svg>
            </button>
          </div>
        </div>
      </div>
      
      <Modal
        isOpen={isCreateModalOpen}
        onClose={() => setIsCreateModalOpen(false)}
        size="xl"
      >
        <TaskForm
          taskToEdit={task}
          onSuccess={handleTaskUpdated}
          onCancel={() => setIsCreateModalOpen(false)}
        />
      </Modal>

      {/* Delete Confirmation Modal */}
      <Modal
        isOpen={isDeleteModalOpen}
        onClose={() => setIsDeleteModalOpen(false)}
        size="md"
      >
        <div className="bg-white p-6 rounded-lg">
          <div className="flex items-center mb-4">
            <div className="mx-auto flex-shrink-0 flex items-center justify-center h-12 w-12 rounded-full bg-red-100">
              <svg className="h-6 w-6 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L3.732 16c-.77.833.192 2.5 1.732 2.5z" />
              </svg>
            </div>
          </div>
          
          <div className="text-center">
            <h3 className="text-lg font-medium text-gray-900 mb-2">Delete Task</h3>
            <p className="text-sm text-gray-500 mb-6">
              Are you sure you want to delete "<strong>{task.title}</strong>"? This action cannot be undone.
            </p>
          </div>

          <div className="flex space-x-4">
            <button
              onClick={() => setIsDeleteModalOpen(false)}
              disabled={isDeleting}
              className="flex-1 bg-gray-300 text-gray-700 py-3 px-4 rounded-lg hover:bg-gray-400 focus:outline-none focus:ring-2 focus:ring-gray-500 focus:ring-offset-2 transition-colors font-medium disabled:opacity-50"
            >
              Cancel
            </button>
            <button
              onClick={handleDeleteTask}
              disabled={isDeleting}
              className="flex-1 bg-red-600 text-white py-3 px-4 rounded-lg hover:bg-red-700 focus:outline-none focus:ring-2 focus:ring-red-500 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed transition-colors font-medium"
            >
              {isDeleting ? (
                <>
                  <svg
                    className="animate-spin -ml-1 mr-3 h-5 w-5 text-white inline"
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
                  Deleting...
                </>
              ) : (
                "Delete Task"
              )}
            </button>
          </div>
        </div>
      </Modal>
    </div>
  );
};

export default TaskCard;
