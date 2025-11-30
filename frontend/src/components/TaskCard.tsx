import React, { useEffect, useRef, useState } from "react";
import { Controller, SubmitHandler, useForm } from "react-hook-form";
import toast from "react-hot-toast";
import Select from "react-select";
import API from "../services/api";
import { SelectOption, Task, TaskStatus } from "../types";
import { customSelectStyles } from "../utils/selectStyles";
import {
  formatDueDate,
  formatStatusText,
  getDueDateStyle,
  getPriorityBorderColor,
  getPriorityTextColor,
  getStatusBadgeColor,
} from "../utils/taskUtils";

interface TaskCardProps {
  task: Task;
  refreshDashboard?: () => void;
}

const TaskCard: React.FC<TaskCardProps> = ({ task, refreshDashboard }) => {
  const [isStatusDropdownOpen, setIsStatusDropdownOpen] = useState(false);
  const [isUpdating, setIsUpdating] = useState(false);
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
      </div>
    </div>
  );
};

export default TaskCard;
