import API from "@/services/api";
import { Attachment } from "@/types";
import { useState } from "react";
import { showSuccessToast, showErrorToast } from "../config/toastConfig";
import { ConfirmationModal } from "./ConfirmationModal";
import { ButtonSpinner } from "./LoadingSpinner";

interface AttachmentCardProps {
  file: Attachment;
  onDelete?: (fileId: number) => void;
}

const AttachmentCard: React.FC<AttachmentCardProps> = ({ file, onDelete }) => {
  const [isDownloading, setIsDownloading] = useState(false);
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [isDeleting, setIsDeleting] = useState(false);

  const formatFileSize = (bytes: number): string => {
    if (bytes === 0) return "0 B";
    const k = 1024;
    const sizes = ["B", "KB", "MB", "GB"];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return `${parseFloat((bytes / Math.pow(k, i)).toFixed(1))} ${sizes[i]}`;
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString("en-US", {
      year: "numeric",
      month: "short",
      day: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    });
  };

  const handleDownload = async () => {
    setIsDownloading(true);
    try {
      const response = await API.get(`/attachments/${file.id}/download`);
      const downloadUrl = response.data;
      window.open(downloadUrl, "_blank");
      showSuccessToast("Download started!");
    } catch (error) {
      console.error("Failed to download attachment:", error);
      showErrorToast("Failed to download attachment");
    } finally {
      setIsDownloading(false);
    }
  };

  const handleDelete = async () => {
    setIsDeleting(true);
    try {
      await API.delete(`/attachments/${file.id}`);
      showSuccessToast("Attachment deleted successfully!");
      setIsDeleteModalOpen(false);
      onDelete?.(file.id);
    } catch (error) {
      console.error("Failed to delete the attachment:", error);
      showErrorToast("Failed to delete attachment");
    } finally {
      setIsDeleting(false);
    }
  };

  return (
    <>
      <div className="bg-white rounded-lg shadow-sm hover:shadow-md transition-shadow duration-200 border border-gray-200 p-4 w-full max-w-full">
        {/* File Header */}
        <div className="flex items-start justify-between mb-3">
          <div className="flex items-center space-x-3 flex-1 min-w-0">
            <div className="flex-shrink-0">
              <span className="text-2xl">📎</span>
            </div>
            <div className="flex-1 min-w-0" style={{maxWidth: 'calc(100% - 3rem)'}}>
              <h4 
                className="text-sm font-medium text-gray-900 overflow-hidden" 
                style={{
                  display: '-webkit-box',
                  WebkitLineClamp: 2,
                  WebkitBoxOrient: 'vertical',
                  wordBreak: 'break-word'
                }}
                title={file.originalFileName}
              >
                {file.originalFileName}
              </h4>
              <div className="flex items-center space-x-4 text-xs text-gray-500 mt-1">
                <span>{formatFileSize(file.fileSize)}</span>
                <span>{formatDate(file.uploadedAt)}</span>
              </div>
            </div>
          </div>
        </div>

        {/* Action Buttons */}
        <div className="flex space-x-2 mt-3 pt-3 border-t border-gray-100">
          <button
            onClick={handleDownload}
            disabled={isDownloading}
            className="flex-1 flex items-center justify-center space-x-1 px-3 py-2 text-sm font-medium text-blue-700 bg-blue-50 border border-blue-200 rounded-md hover:bg-blue-100 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
          >
            {isDownloading ? (
              <>
                <ButtonSpinner />
                <span>Downloading...</span>
              </>
            ) : (
              <>
                <svg
                  className="h-4 w-4"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M12 10v6m0 0l-3-3m3 3l3-3m2 8H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"
                  />
                </svg>
                <span>Download</span>
              </>
            )}
          </button>

          <button
            onClick={() => setIsDeleteModalOpen(true)}
            className="px-3 py-2 text-sm font-medium text-red-700 bg-red-50 border border-red-200 rounded-md hover:bg-red-100 focus:outline-none focus:ring-2 focus:ring-red-500 focus:ring-offset-2 transition-colors"
            title="Delete attachment"
          >
            <svg
              className="h-4 w-4"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"
              />
            </svg>
          </button>
        </div>
      </div>

      <ConfirmationModal
        isOpen={isDeleteModalOpen}
        onClose={() => setIsDeleteModalOpen(false)}
        onConfirm={handleDelete}
        title="Delete Attachment"
        message={`Are you sure you want to delete "${file.originalFileName}"? This action cannot be undone.`}
        confirmText="Delete Attachment"
        variant="danger"
        isLoading={isDeleting}
      />
    </>
  );
};

export default AttachmentCard;
