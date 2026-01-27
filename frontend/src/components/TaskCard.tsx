import React, { useEffect, useRef, useState } from "react";
import { Controller, SubmitHandler, useForm } from "react-hook-form";
import Select from "react-select";
import API from "../services/api";
import { SelectOption, Task, TaskFormData, TaskStatus } from "../types";
import { customSelectStyles } from "../utils/selectStyles";
import { showSuccessToast, showErrorToast } from "../config/toastConfig";
import { ConfirmationModal } from "./ConfirmationModal";
import {
  formatDueDate,
  formatStatusText,
  getDueDateStyle,
  getPriorityBorderColor,
  getPriorityTextColor,
  getStatusBadgeColor,
} from "../utils/taskUtils";
import Modal from "./Modal";
import TagBadge from "./TagBadge";
import TaskForm from "./TaskForm";
import TaskDetailModal from "./TaskDetailModal";

interface TaskCardProps {
  task: Task;
  refreshDashboard?: () => void;
  taskDefaultValues?: TaskFormData;
}

const TaskCard: React.FC<TaskCardProps> = ({
  task,
  refreshDashboard,
  taskDefaultValues,
}) => {
  const [localTask, setLocalTask] = useState<Task>(task);
  const [isStatusDropdownOpen, setIsStatusDropdownOpen] = useState(false);
  const [isUpdating, setIsUpdating] = useState(false);
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [isDeleting, setIsDeleting] = useState(false);
  const [isDetailModalOpen, setIsDetailModalOpen] = useState(false);
  const dropdownRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    setLocalTask(task);
  }, [task]);

  const currentStatusOption: SelectOption<TaskStatus> = {
    value: localTask.status,
    label: localTask.status
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

  const handleTaskUpdated = (updatedTask?: Task) => {
    setIsCreateModalOpen(false);
    if (updatedTask) {
      setLocalTask(updatedTask);
    }
    refreshDashboard?.();
  };
  const handleDetailTaskUpdate = (updatedTask?: Task) => {
    if (updatedTask) {
      setLocalTask(updatedTask);
    }
    refreshDashboard?.();
  };

  const handleDeleteTask = async () => {
    setIsDeleting(true);
    try {
      await API.delete(`/tasks/${task.id}`);
      showSuccessToast("Task deleted successfully!");
      setIsDeleteModalOpen(false);
      refreshDashboard?.();
    } catch (error: any) {
      console.error("Error deleting task:", error);
      showErrorToast(error.response?.data?.message || "Failed to delete task");
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
    if (!data.status?.value) return;

    setIsUpdating(true);
    
    const previousStatus = localTask.status;
    setLocalTask(prev => ({ ...prev, status: data.status.value }));

    try {
      const updatedTask = {
        title: localTask.title,
        description: localTask.description,
        dueDate: localTask.dueDate,
        priority: localTask.priority,
        status: data.status.value,
      };

      await API.put(`/tasks/${task.id}`, updatedTask);
      refreshDashboard?.();
      setIsStatusDropdownOpen(false);
      showSuccessToast("Task status updated successfully!");
    } catch (error: any) {
      setLocalTask(prev => ({ ...prev, status: previousStatus }));
      showErrorToast("Failed to update task status");
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
        border border-gray-200 p-4
        ${getPriorityBorderColor(task.priority)}
      `}
    >
      <div className="flex justify-between items-center">
        <div className="flex-1 min-w-0 mr-4">
          <h3 className="text-lg font-semibold text-gray-900 truncate mb-1">
            {localTask.title}
          </h3>
          {localTask.dueDate && (
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
              <span className={getDueDateStyle(localTask.dueDate, localTask.status)}>
                {formatDueDate(localTask.dueDate)}
              </span>
            </div>
          )}
        </div>

        <div className="flex items-center space-x-2">
          <button
            onClick={() => setIsDetailModalOpen(true)}
            className="text-xs text-blue-600 hover:text-blue-800 font-medium transition-colors px-2 py-1 rounded hover:bg-blue-50"
            title="View details"
          >
            View
          </button>
          
          <button
            onClick={() => setIsCreateModalOpen(true)}
            className="p-1.5 text-gray-400 hover:text-blue-600 hover:bg-blue-50 rounded transition-colors"
            title="Edit task"
          >
            <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
            </svg>
          </button>

          <button
            onClick={() => setIsDeleteModalOpen(true)}
            className="p-1.5 text-gray-400 hover:text-red-600 hover:bg-red-50 rounded transition-colors"
            title="Delete task"
          >
            <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
            </svg>
          </button>

          <div className="relative">
            <Controller
              name="status"
              control={control}
              render={({ field }) => (
                <Select
                  {...field}
                  value={currentStatusOption}
                  options={taskStatusOptions}
                  className="react-select-container"
                  classNamePrefix="react-select"
                  styles={{
                    ...customSelectStyles,
                    control: (provided, state) => ({
                      ...provided,
                      minHeight: '32px',
                      height: '32px',
                      minWidth: '100px',
                      fontSize: '12px',
                      backgroundColor: getStatusBadgeColor(localTask.status).includes('bg-green') ? '#dcfce7' :
                                     getStatusBadgeColor(localTask.status).includes('bg-yellow') ? '#fef3c7' : '#dbeafe',
                      border: 'none',
                      borderRadius: '9999px',
                      boxShadow: state.isFocused ? '0 0 0 2px rgba(59, 130, 246, 0.5)' : 'none',
                    }),
                    valueContainer: (provided) => ({
                      ...provided,
                      height: '32px',
                      padding: '0 8px',
                    }),
                    input: (provided) => ({
                      ...provided,
                      margin: '0px',
                    }),
                    indicatorsContainer: (provided) => ({
                      ...provided,
                      height: '32px',
                    }),
                    singleValue: (provided) => ({
                      ...provided,
                      fontSize: '12px',
                      fontWeight: '500',
                      color: getStatusBadgeColor(localTask.status).includes('bg-green') ? '#166534' :
                             getStatusBadgeColor(localTask.status).includes('bg-yellow') ? '#92400e' : '#1e40af',
                    }),
                  }}
                  isDisabled={isUpdating}
                  onChange={(newValue) => {
                    if (newValue && newValue.value !== localTask.status) {
                      field.onChange(newValue);
                      handleSubmit(onsubmit)();
                    }
                  }}
                />
              )}
            />
          </div>
        </div>
      </div>

      <Modal
        isOpen={isCreateModalOpen}
        onClose={() => setIsCreateModalOpen(false)}
        size="xl"
      >
        <TaskForm
          taskToEdit={localTask}
          onSuccess={handleTaskUpdated}
          onCancel={() => setIsCreateModalOpen(false)}
        />
      </Modal>

      <ConfirmationModal
        isOpen={isDeleteModalOpen}
        onClose={() => setIsDeleteModalOpen(false)}
        onConfirm={handleDeleteTask}
        title="Delete Task"
        message={`Are you sure you want to delete "${task.title}"? This action cannot be undone.`}
        confirmText="Delete Task"
        variant="danger"
        isLoading={isDeleting}
      />

      <TaskDetailModal
        isOpen={isDetailModalOpen}
        onClose={() => setIsDetailModalOpen(false)}
        task={localTask}
        onTaskUpdate={handleDetailTaskUpdate}
      />
    </div>
  );
};

export default TaskCard;
