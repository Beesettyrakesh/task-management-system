import React, { useEffect, useState } from "react";
import API from "../services/api";
import { Attachment } from "../types";
import { showErrorToast } from "../config/toastConfig";
import AttachmentCard from "./AttachmentCard";

interface AttachmentListProps {
  taskId: number;
  refreshTrigger?: number; // Optional prop to trigger refresh from parent
  onAttachmentDeleted?: () => void; // Callback to notify parent of deletion
}

const AttachmentList: React.FC<AttachmentListProps> = ({ 
  taskId, 
  refreshTrigger,
  onAttachmentDeleted
}) => {
  const [attachments, setAttachments] = useState<Attachment[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchAttachments = async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await API.get(`/tasks/${taskId}/attachments`);
      setAttachments(response.data);
    } catch (error: any) {
      console.error("Failed to fetch attachments:", error);
      setError("Failed to load attachments");
      showErrorToast("Failed to load attachments");
    } finally {
      setLoading(false);
    }
  };

  const handleAttachmentDelete = (deletedFileId: number) => {
    setAttachments(prev => prev.filter(attachment => attachment.id !== deletedFileId));
    // Notify parent component that an attachment was deleted
    onAttachmentDeleted?.();
  };

  useEffect(() => {
    fetchAttachments();
  }, [taskId, refreshTrigger]);

  if (loading) {
    return (
      <div className="space-y-4">
        <div className="flex items-center justify-between mb-4">
          <h3 className="text-lg font-medium text-gray-900">Attachments</h3>
          <div className="animate-pulse flex items-center space-x-2">
            <div className="h-4 w-16 bg-gray-300 rounded"></div>
          </div>
        </div>
        
        {/* Loading Skeleton */}
        {[1, 2, 3].map((index) => (
          <div key={index} className="bg-white rounded-lg shadow-sm border border-gray-200 p-4">
            <div className="animate-pulse">
              <div className="flex items-center space-x-3 mb-3">
                <div className="h-8 w-8 bg-gray-300 rounded"></div>
                <div className="flex-1">
                  <div className="h-4 w-48 bg-gray-300 rounded mb-2"></div>
                  <div className="flex space-x-4">
                    <div className="h-3 w-16 bg-gray-200 rounded"></div>
                    <div className="h-3 w-24 bg-gray-200 rounded"></div>
                  </div>
                </div>
              </div>
              <div className="flex space-x-2 pt-3 border-t border-gray-100">
                <div className="h-8 w-20 bg-gray-300 rounded"></div>
                <div className="h-8 w-8 bg-gray-300 rounded"></div>
              </div>
            </div>
          </div>
        ))}
      </div>
    );
  }

  if (error) {
    return (
      <div className="text-center py-8">
        <div className="mx-auto flex items-center justify-center h-12 w-12 rounded-full bg-red-100 mb-4">
          <svg
            className="h-6 w-6 text-red-600"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L3.732 16c-.77.833.192 2.5 1.732 2.5z"
            />
          </svg>
        </div>
        <h3 className="text-lg font-medium text-gray-900 mb-2">Error loading attachments</h3>
        <p className="text-sm text-gray-500 mb-4">{error}</p>
        <button
          onClick={fetchAttachments}
          className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
        >
          Try Again
        </button>
      </div>
    );
  }

  if (attachments.length === 0) {
    return (
      <div className="text-center py-8">
        <div className="mx-auto flex items-center justify-center h-12 w-12 rounded-full bg-gray-100 mb-4">
          <svg
            className="h-6 w-6 text-gray-400"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M15.172 7l-6.586 6.586a2 2 0 102.828 2.828l6.414-6.586a4 4 0 00-5.656-5.656l-6.415 6.585a6 6 0 108.486 8.486L20.5 13"
            />
          </svg>
        </div>
        <h3 className="text-lg font-medium text-gray-900 mb-2">No attachments</h3>
        <p className="text-sm text-gray-500">
          This task doesn't have any files attached yet.
        </p>
      </div>
    );
  }

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between mb-4">
        <h3 className="text-lg font-medium text-gray-900">
          Attachments
        </h3>
        <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
          {attachments.length} {attachments.length === 1 ? 'file' : 'files'}
        </span>
      </div>

      {/* Grid layout for attachments */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4 w-full">
        {attachments.map((attachment) => (
          <div key={attachment.id} className="min-w-0 w-full">
            <AttachmentCard
              file={attachment}
              onDelete={handleAttachmentDelete}
            />
          </div>
        ))}
      </div>
    </div>
  );
};

export default AttachmentList;
