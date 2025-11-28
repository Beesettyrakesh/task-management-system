import { Priority, TaskStatus } from "../types";

// Status badge color mapping
export const getStatusBadgeColor = (status: TaskStatus): string => {
  switch (status) {
    case TaskStatus.TODO:
      return "bg-blue-100 text-blue-800 border border-blue-200";
    case TaskStatus.IN_PROGRESS:
      return "bg-yellow-100 text-yellow-800 border border-yellow-200";
    case TaskStatus.DONE:
      return "bg-green-100 text-green-800 border border-green-200";
    default:
      return "bg-gray-100 text-gray-800 border border-gray-200";
  }
};

// Priority left border color
export const getPriorityBorderColor = (priority: Priority): string => {
  switch (priority) {
    case Priority.LOW:
      return "border-l-4 border-l-green-500";
    case Priority.MEDIUM:
      return "border-l-4 border-l-yellow-500";
    case Priority.HIGH:
      return "border-l-4 border-l-red-500";
    default:
      return "border-l-4 border-l-gray-300";
  }
};

// Priority text color
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

// Format due date
export const formatDueDate = (dueDateString: string): string => {
  const dueDate = new Date(dueDateString);
  const today = new Date();
  const diffTime = dueDate.getTime() - today.getTime();
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));

  if (diffDays < 0) {
    return `Overdue by ${Math.abs(diffDays)} day${Math.abs(diffDays) !== 1 ? 's' : ''}`;
  } else if (diffDays === 0) {
    return "Due today";
  } else if (diffDays === 1) {
    return "Due tomorrow";
  } else if (diffDays <= 7) {
    return `Due in ${diffDays} days`;
  } else {
    return dueDate.toLocaleDateString('en-US', {
      month: 'short',
      day: 'numeric',
      year: dueDate.getFullYear() !== today.getFullYear() ? 'numeric' : undefined
    });
  }
};

// Check if task is overdue
export const isOverdue = (dueDateString: string): boolean => {
  const dueDate = new Date(dueDateString);
  const today = new Date();
  return dueDate < today;
};

// Get due date style
export const getDueDateStyle = (dueDateString: string): string => {
  const dueDate = new Date(dueDateString);
  const today = new Date();
  const diffTime = dueDate.getTime() - today.getTime();
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));

  if (diffDays < 0) {
    return "text-red-600 font-medium"; // Overdue
  } else if (diffDays === 0) {
    return "text-orange-600 font-medium"; // Due today
  } else if (diffDays === 1) {
    return "text-yellow-600 font-medium"; // Due tomorrow
  } else if (diffDays <= 7) {
    return "text-blue-600"; // Due this week
  } else {
    return "text-gray-600"; // Future
  }
};

// Format status display text
export const formatStatusText = (status: TaskStatus): string => {
  switch (status) {
    case TaskStatus.TODO:
      return "To Do";
    case TaskStatus.IN_PROGRESS:
      return "In Progress";
    case TaskStatus.DONE:
      return "Done";
    default:
      return status;
  }
};
