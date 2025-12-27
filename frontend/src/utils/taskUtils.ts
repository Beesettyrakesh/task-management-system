import { Priority, Task, TaskFormData, TaskStatus } from "../types";

// Existing utility functions...
export const getPriorityBadgeColor = (priority: Priority): string => {
  switch (priority) {
    case Priority.LOW:
      return "bg-green-100 text-green-800 border-l-4 border-green-500";
    case Priority.MEDIUM:
      return "bg-yellow-100 text-yellow-800 border-l-4 border-yellow-500";
    case Priority.HIGH:
      return "bg-red-100 text-red-800 border-l-4 border-red-500";
    default:
      return "bg-gray-100 text-gray-800 border-l-4 border-gray-500";
  }
};

export const getStatusBadgeColor = (status: TaskStatus): string => {
  switch (status) {
    case TaskStatus.TODO:
      return "bg-blue-100 text-blue-800";
    case TaskStatus.IN_PROGRESS:
      return "bg-yellow-100 text-yellow-800";
    case TaskStatus.DONE:
      return "bg-green-100 text-green-800";
    default:
      return "bg-gray-100 text-gray-800";
  }
};

export const formatStatusText = (status: TaskStatus): string => {
  return status.replace("_", " ");
};

export const formatDate = (dateString: string): string => {
  const date = new Date(dateString);
  const today = new Date();
  const diffTime = date.getTime() - today.getTime();
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));

  if (diffDays === 0) {
    return "Due today";
  } else if (diffDays === 1) {
    return "Due tomorrow";
  } else if (diffDays < 0) {
    return `Overdue by ${Math.abs(diffDays)} day${
      Math.abs(diffDays) > 1 ? "s" : ""
    }`;
  } else {
    return `Due in ${diffDays} day${diffDays > 1 ? "s" : ""}`;
  }
};

export const isOverdue = (dueDateString: string): boolean => {
  const dueDate = new Date(dueDateString);
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  dueDate.setHours(0, 0, 0, 0);
  return dueDate < today;
};

// Utility function to convert Task to TaskFormData
export const convertTaskToFormData = (task: Task): TaskFormData => {
  return {
    title: task.title,
    description: task.description,
    dueDate: task.dueDate ? new Date(task.dueDate) : null,
    priority: task.priority
      ? {
          value: task.priority,
          label: task.priority.charAt(0) + task.priority.slice(1).toLowerCase(),
        }
      : null,
    status: task.status
      ? {
          value: task.status,
          label: task.status
            .replace("_", " ")
            .toLowerCase()
            .replace(/\b\w/g, (l) => l.toUpperCase()),
        }
      : null,
    tags: task.tags || [],
  };
};

// Utility function to format form data for API
export const formatTaskForApi = (data: TaskFormData) => {
  return {
    title: data.title,
    description: data.description,
    dueDate: data.dueDate ? data.dueDate.toISOString().split("T")[0] : null,
    priority: data.priority?.value,
    status: data.status?.value,
    tags: data.tags || [],
  };
};

// Additional utility functions for TaskCard
export const formatDueDate = formatDate; // Alias for backward compatibility

export const getDueDateStyle = (dateString: string): string => {
  const date = new Date(dateString);
  const today = new Date();
  const diffTime = date.getTime() - today.getTime();
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));

  if (diffDays < 0) {
    return "text-red-600 font-medium"; // Overdue
  } else if (diffDays === 0) {
    return "text-orange-600 font-medium"; // Due today
  } else if (diffDays === 1) {
    return "text-yellow-600 font-medium"; // Due tomorrow
  } else {
    return "text-gray-600"; // Future dates
  }
};

export const getPriorityBorderColor = (priority: Priority): string => {
  switch (priority) {
    case Priority.LOW:
      return "border-l-4 border-green-500";
    case Priority.MEDIUM:
      return "border-l-4 border-yellow-500";
    case Priority.HIGH:
      return "border-l-4 border-red-500";
    default:
      return "border-l-4 border-gray-500";
  }
};

export const getPriorityTextColor = (priority: Priority): string => {
  switch (priority) {
    case Priority.LOW:
      return "text-green-600";
    case Priority.MEDIUM:
      return "text-yellow-600";
    case Priority.HIGH:
      return "text-red-600";
    default:
      return "text-gray-600";
  }
};
