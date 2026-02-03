import React, { useState } from "react";
import { Task } from "../types";
import {
  formatDueDate,
  formatStatusText,
  getDueDateStyle,
  getPriorityTextColor,
  getStatusBadgeColor,
} from "../utils/taskUtils";
import AttachmentList from "./AttachmentList";
import AttachmentUploader from "./AttachmentUploader";
import Modal from "./Modal";
import TagBadge from "./TagBadge";

interface TaskDetailModalProps {
  isOpen: boolean;
  onClose: () => void;
  task: Task;
  onTaskUpdate?: (updatedTask?: Task) => void;
}

const TaskDetailModal: React.FC<TaskDetailModalProps> = ({
  isOpen,
  onClose,
  task,
  onTaskUpdate,
}) => {
  const [refreshTrigger, setRefreshTrigger] = useState(0);
  const [activeTab, setActiveTab] = useState<"details" | "attachments">(
    "details",
  );

  const handleUploadComplete = () => {
    setRefreshTrigger((prev) => prev + 1);
    onTaskUpdate?.();
  };

  const handleAttachmentDeleted = () => {
    setRefreshTrigger((prev) => prev + 1);
    onTaskUpdate?.();
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString("en-US", {
      weekday: "long",
      year: "numeric",
      month: "long",
      day: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    });
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} size="xl">
      <div className="bg-white rounded-lg max-h-[90vh] overflow-hidden flex flex-col">
        <div className="border-b border-gray-200 p-6">
          <div className="flex items-start justify-between mb-4">
            <div className="flex-1">
              <h1 className="text-2xl font-bold text-gray-900 mb-2 pr-4">
                {task.title}
              </h1>
              <div className="flex items-center space-x-3">
                <span
                  className={`
                    inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium
                    ${getStatusBadgeColor(task.status)}
                  `}
                >
                  {formatStatusText(task.status)}
                </span>
                <span
                  className={`text-sm font-medium ${getPriorityTextColor(task.priority)}`}
                >
                  {task.priority} Priority
                </span>
                {task.dueDate && (
                  <span className={`text-sm ${getDueDateStyle(task.dueDate)}`}>
                    Due: {formatDueDate(task.dueDate)}
                  </span>
                )}
              </div>
            </div>
            <button
              onClick={onClose}
              className="text-gray-400 hover:text-gray-600 transition-colors"
            >
              <svg
                className="w-6 h-6"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M6 18L18 6M6 6l12 12"
                />
              </svg>
            </button>
          </div>

          <div className="flex space-x-6">
            <button
              onClick={() => setActiveTab("details")}
              className={`
                py-2 px-1 border-b-2 font-medium text-sm transition-colors
                ${
                  activeTab === "details"
                    ? "border-blue-500 text-blue-600"
                    : "border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300"
                }
              `}
            >
              Task Details
            </button>
            <button
              onClick={() => setActiveTab("attachments")}
              className={`
                py-2 px-1 border-b-2 font-medium text-sm transition-colors
                ${
                  activeTab === "attachments"
                    ? "border-blue-500 text-blue-600"
                    : "border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300"
                }
              `}
            >
              Files & Attachments
            </button>
          </div>
        </div>

        <div className="flex-1 overflow-y-auto">
          {activeTab === "details" && (
            <div className="p-6 space-y-6">
              {task.description && (
                <div>
                  <h3 className="text-lg font-medium text-gray-900 mb-3">
                    Description
                  </h3>
                  <div className="bg-gray-50 rounded-lg p-4">
                    <p className="text-gray-700 whitespace-pre-wrap leading-relaxed">
                      {task.description}
                    </p>
                  </div>
                </div>
              )}

              <div>
                <h3 className="text-lg font-medium text-gray-900 mb-3">Tags</h3>
                {task.tags && task.tags.length > 0 ? (
                  <div className="flex flex-wrap gap-2">
                    {task.tags.map((tag) => (
                      <TagBadge key={tag.id} tag={tag} size="md" />
                    ))}
                  </div>
                ) : (
                  <p className="text-gray-500 text-sm">
                    No tags assigned to this task.
                  </p>
                )}
              </div>

              <div>
                <h3 className="text-lg font-medium text-gray-900 mb-3">
                  Task Information
                </h3>
                <div className="bg-gray-50 rounded-lg p-4 space-y-3">
                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <span className="text-sm font-medium text-gray-500">
                        Status
                      </span>
                      <p className="text-sm text-gray-900 mt-1">
                        {formatStatusText(task.status)}
                      </p>
                    </div>
                    <div>
                      <span className="text-sm font-medium text-gray-500">
                        Priority
                      </span>
                      <p
                        className={`text-sm mt-1 font-medium ${getPriorityTextColor(task.priority)}`}
                      >
                        {task.priority}
                      </p>
                    </div>
                  </div>

                  {task.dueDate && (
                    <div>
                      <span className="text-sm font-medium text-gray-500">
                        Due Date
                      </span>
                      <p
                        className={`text-sm mt-1 ${getDueDateStyle(task.dueDate)}`}
                      >
                        {formatDate(task.dueDate)}
                      </p>
                    </div>
                  )}

                  <div className="grid grid-cols-2 gap-4 pt-3 border-t border-gray-200">
                    <div>
                      <span className="text-sm font-medium text-gray-500">
                        Created
                      </span>
                      <p className="text-sm text-gray-700 mt-1">
                        {formatDate(task.createdAt)}
                      </p>
                      {task.createdBy && (
                        <p className="text-xs text-gray-500 mt-1">
                          by {task.createdBy}
                        </p>
                      )}
                    </div>
                    {task.updatedAt && (
                      <div>
                        <span className="text-sm font-medium text-gray-500">
                          Last Updated
                        </span>
                        <p className="text-sm text-gray-700 mt-1">
                          {formatDate(task.updatedAt)}
                        </p>
                        {task.lastModifiedBy && (
                          <p className="text-xs text-gray-500 mt-1">
                            by {task.lastModifiedBy}
                          </p>
                        )}
                      </div>
                    )}
                  </div>
                </div>
              </div>
            </div>
          )}

          {activeTab === "attachments" && (
            <div className="p-6 space-y-6">
              <div>
                <h3 className="text-lg font-medium text-gray-900 mb-3">
                  Upload New Files
                </h3>
                <AttachmentUploader
                  taskId={task.id}
                  onUploadComplete={handleUploadComplete}
                />
              </div>

              <div className="border-t border-gray-200"></div>

              <div>
                <AttachmentList
                  taskId={task.id}
                  refreshTrigger={refreshTrigger}
                  onAttachmentDeleted={handleAttachmentDeleted}
                />
              </div>
            </div>
          )}
        </div>

        <div className="border-t border-gray-200 px-6 py-4">
          <div className="flex justify-between items-center">
            <button
              onClick={onClose}
              className="inline-flex items-center px-4 py-2 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
            >
              Close
            </button>
          </div>
        </div>
      </div>
    </Modal>
  );
};

export default TaskDetailModal;
