import React, { useEffect, useState } from "react";
import DatePicker from "react-datepicker";
import "react-datepicker/dist/react-datepicker.css";
import { Controller, SubmitHandler, useForm } from "react-hook-form";
import toast from "react-hot-toast";
import Select from "react-select";
import API from "../services/api";
import {
  Priority,
  SelectOption,
  Tag,
  Task,
  TaskFormData,
  TaskStatus,
} from "../types";
import { convertTaskToFormData, formatTaskForApi } from "../utils/taskUtils";
import TagSelector from "./TagSelector";

interface TaskFormProps {
  onSuccess?: (updatedTask?: Task) => void;
  onCancel?: () => void;
  taskToEdit?: Task;
  taskDefaultValues?: TaskFormData;
}

const TaskForm: React.FC<TaskFormProps> = ({
  onSuccess,
  onCancel,
  taskToEdit,
  taskDefaultValues,
}) => {
  const isEditMode = !!taskToEdit;
  const [availableTags, setAvailableTags] = useState<Tag[]>([]);

  useEffect(() => {
    const fetchTags = async () => {
      try {
        const response = await API.get("/tags");
        setAvailableTags(response.data);
      } catch (error) {
        console.error("Failed to fetch available tags:", error);
      }
    };
    fetchTags();
  }, []);

  const formDefaultValues = taskToEdit
    ? convertTaskToFormData(taskToEdit)
    : taskDefaultValues || {
        title: "",
        description: "",
        dueDate: null,
        priority: null,
        status: null,
        tags: [],
      };

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
    control,
    reset,
    setValue,
    watch,
  } = useForm<TaskFormData>({
    mode: "onChange",
    defaultValues: formDefaultValues,
  });

  const priorityOptions: SelectOption<Priority>[] = Object.values(Priority).map(
    (priority) => ({
      value: priority,
      label: priority.charAt(0) + priority.slice(1).toLowerCase(),
    }),
  );

  const taskStatusOptions: SelectOption<TaskStatus>[] = Object.values(
    TaskStatus,
  ).map((status) => ({
    value: status,
    label: status
      .replace("_", " ")
      .toLowerCase()
      .replace(/\b\w/g, (l) => l.toUpperCase()),
  }));

  const onSubmit: SubmitHandler<TaskFormData> = async (data) => {
    try {
      const taskData = formatTaskForApi(data);

      let updatedTask: Task;
      if (isEditMode && taskToEdit) {
        const response = await API.put(`/tasks/${taskToEdit.id}`, taskData);
        updatedTask = response.data;
        toast.success("Task updated successfully!");
      } else {
        const response = await API.post("/tasks", taskData);
        updatedTask = response.data;
        toast.success("Task created successfully!");
      }
      reset();
      onSuccess?.(updatedTask);
    } catch (error: any) {
      console.error(
        `Error ${isEditMode ? "updating" : "creating"} task:`,
        error,
      );
      toast.error(
        error.response?.data?.message ||
          `Failed to ${isEditMode ? "update" : "create"} task`,
      );
    }
  };

  return (
    <div className="bg-white p-4 rounded-lg">
      <h2 className="text-xl font-bold text-gray-900 mb-4">
        {isEditMode ? "Edit Task" : "Create New Task"}
      </h2>

      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
        <div>
          <label
            htmlFor="title"
            className="block text-sm font-medium text-gray-700 mb-2"
          >
            Task Title *
          </label>
          <input
            id="title"
            type="text"
            {...register("title", {
              required: "Task title is required",
              minLength: {
                value: 3,
                message: "Title must be at least 3 characters",
              },
              maxLength: {
                value: 100,
                message: "Title cannot exceed 100 characters",
              },
            })}
            className={`appearance-none relative block w-full px-3 py-3 border ${
              errors.title ? "border-red-300" : "border-gray-300"
            } placeholder-gray-500 text-gray-900 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors`}
            placeholder="Enter task title"
          />
          {errors.title && (
            <p className="mt-2 text-sm text-red-600 flex items-center">
              <svg
                className="w-4 h-4 mr-1"
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
              {errors.title.message}
            </p>
          )}
        </div>

        <div>
          <label
            htmlFor="description"
            className="block text-sm font-medium text-gray-700 mb-2"
          >
            Description
          </label>
          <textarea
            id="description"
            rows={4}
            {...register("description", {
              maxLength: {
                value: 1000,
                message: "Description must not exceed 1000 characters",
              },
            })}
            className={`appearance-none relative block w-full px-3 py-3 border ${
              errors.description ? "border-red-300" : "border-gray-300"
            } placeholder-gray-500 text-gray-900 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors resize-none`}
            placeholder="Enter task description (optional)"
          />
          {errors.description && (
            <p className="mt-2 text-sm text-red-600 flex items-center">
              <svg
                className="w-4 h-4 mr-1"
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
              {errors.description.message}
            </p>
          )}
        </div>

        <div>
          <label
            htmlFor="dueDate"
            className="block text-sm font-medium text-gray-700 mb-2"
          >
            Due Date *
          </label>
          <div
            className={`border ${
              errors.dueDate ? "border-red-300" : "border-gray-300"
            } rounded-lg`}
          >
            <Controller
              control={control}
              name="dueDate"
              rules={{ required: "Due date is required" }}
              render={({ field }) => (
                <DatePicker
                  selected={field.value}
                  onChange={(date: Date | null) => field.onChange(date)}
                  dateFormat="dd/MM/yyyy"
                  minDate={new Date()}
                  placeholderText="Select due date"
                  showYearDropdown
                  scrollableYearDropdown
                  isClearable
                  yearDropdownItemNumber={10}
                  className="w-full px-3 py-3 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors"
                />
              )}
            />
          </div>
          {errors.dueDate && (
            <p className="mt-2 text-sm text-red-600 flex items-center">
              <svg
                className="w-4 h-4 mr-1"
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
              {errors.dueDate.message}
            </p>
          )}
        </div>

        <div>
          <label
            htmlFor="priority"
            className="block text-sm font-medium text-gray-700 mb-2"
          >
            Priority
          </label>
          <Controller
            name="priority"
            control={control}
            rules={{ required: "Task priority is required" }}
            render={({ field }) => (
              <Select
                {...field}
                options={priorityOptions}
                placeholder="Select task priority"
                isClearable
                menuPlacement="auto"
                className="react-select-container"
                classNamePrefix="react-select"
              />
            )}
          />
          {errors.priority && (
            <p className="mt-2 text-sm text-red-600 flex items-center">
              <svg
                className="w-4 h-4 mr-1"
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
              {errors.priority.message}
            </p>
          )}
        </div>

        <div>
          <label
            htmlFor="status"
            className="block text-sm font-medium text-gray-700 mb-2"
          >
            Status
          </label>
          <Controller
            name="status"
            control={control}
            rules={{ required: "Task status is required" }}
            render={({ field }) => (
              <Select
                {...field}
                options={taskStatusOptions}
                placeholder="Select task status"
                menuPlacement="auto"
                className="react-select-container"
                classNamePrefix="react-select"
              />
            )}
          />
          {errors.status && (
            <p className="mt-2 text-sm text-red-600 flex items-center">
              <svg
                className="w-4 h-4 mr-1"
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
              {errors.status.message}
            </p>
          )}
        </div>

        <div className="mb-4">
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Tags
          </label>
          <TagSelector
            selectedTags={watch("tags") || []}
            availableTags={availableTags}
            onTagsChange={(tags) => setValue("tags", tags)}
          />
        </div>

        <div className="flex space-x-4 pt-4">
          <button
            type="submit"
            disabled={isSubmitting}
            className="flex-1 bg-blue-600 text-white py-3 px-4 rounded-lg hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed transition-colors font-medium"
          >
            {isSubmitting ? (
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
                {isEditMode ? "Updating..." : "Creating..."}
              </>
            ) : isEditMode ? (
              "Update Task"
            ) : (
              "Create Task"
            )}
          </button>

          {onCancel && (
            <button
              type="button"
              onClick={onCancel}
              className="flex-1 bg-gray-300 text-gray-700 py-3 px-4 rounded-lg hover:bg-gray-400 focus:outline-none focus:ring-2 focus:ring-gray-500 focus:ring-offset-2 transition-colors font-medium"
            >
              Cancel
            </button>
          )}
        </div>
      </form>
    </div>
  );
};

export default TaskForm;
