import React, { useState } from "react";
import API from "../services/api";
import { FileUploadProgress } from "../types";
import toast from "react-hot-toast";
import FileDropzone, { FileWithValidation } from "./FileDropzone";

interface AttachmentUploaderProps {
  taskId: number;
  onUploadComplete?: () => void;
  onUploadStart?: () => void;
  disabled?: boolean;
  className?: string;
}

const AttachmentUploader: React.FC<AttachmentUploaderProps> = ({
  taskId,
  onUploadComplete,
  onUploadStart,
  disabled = false,
  className = "",
}) => {
  const [uploadProgress, setUploadProgress] = useState<FileUploadProgress[]>([]);
  const [isUploading, setIsUploading] = useState(false);

  const formatFileSize = (bytes: number): string => {
    if (bytes === 0) return "0 B";
    const k = 1024;
    const sizes = ["B", "KB", "MB", "GB"];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return `${parseFloat((bytes / Math.pow(k, i)).toFixed(1))} ${sizes[i]}`;
  };

  const uploadFile = async (file: File): Promise<{ success: boolean; error?: string }> => {
    return new Promise((resolve, reject) => {
      const formData = new FormData();
      formData.append('file', file);

      const xhr = new XMLHttpRequest();

      // Update progress
      xhr.upload.addEventListener('progress', (event) => {
        if (event.lengthComputable) {
          const progress = Math.round((event.loaded / event.total) * 100);
          setUploadProgress(prev => 
            prev.map(item => 
              item.file === file 
                ? { ...item, progress, status: 'uploading' as const }
                : item
            )
          );
        }
      });

      // Handle completion
      xhr.addEventListener('load', () => {
        if (xhr.status >= 200 && xhr.status < 300) {
          setUploadProgress(prev => 
            prev.map(item => 
              item.file === file 
                ? { ...item, progress: 100, status: 'completed' as const }
                : item
            )
          );
          resolve({ success: true });
        } else {
          const error = `Upload failed: ${xhr.status} ${xhr.statusText}`;
          setUploadProgress(prev => 
            prev.map(item => 
              item.file === file 
                ? { ...item, status: 'error' as const, error }
                : item
            )
          );
          resolve({ success: false, error });
        }
      });

      // Handle errors
      xhr.addEventListener('error', () => {
        const error = 'Network error occurred during upload';
        setUploadProgress(prev => 
          prev.map(item => 
            item.file === file 
              ? { ...item, status: 'error' as const, error }
              : item
          )
        );
        resolve({ success: false, error });
      });

      // Start upload
      xhr.open('POST', `https://doqueue.ddns.net/api/tasks/${taskId}/attachments`);
      
      // Add authorization header if available
      const token = localStorage.getItem('token');
      if (token) {
        xhr.setRequestHeader('Authorization', `Bearer ${token}`);
      }
      
      xhr.send(formData);
    });
  };

  const uploadFilesBulk = async (files: File[]): Promise<{ successCount: number; errorCount: number; response?: any }> => {
    return new Promise((resolve, reject) => {
      const formData = new FormData();
      files.forEach(file => {
        formData.append('files', file);
      });

      const xhr = new XMLHttpRequest();

      // Update progress for all files
      xhr.upload.addEventListener('progress', (event) => {
        if (event.lengthComputable) {
          const progress = Math.round((event.loaded / event.total) * 100);
          setUploadProgress(prev => 
            prev.map(item => ({
              ...item, 
              progress, 
              status: 'uploading' as const
            }))
          );
        }
      });

      // Handle completion
      xhr.addEventListener('load', () => {
        if (xhr.status >= 200 && xhr.status < 300) {
          try {
            const response = JSON.parse(xhr.responseText);
            
            // Update progress based on bulk upload response
            setUploadProgress(prev => 
              prev.map(item => {
                const successful = response.successfulUploads?.find(
                  (upload: any) => upload.originalFilename === item.file.name
                );
                const failed = response.failedUploads?.find(
                  (error: any) => error.filename === item.file.name
                );
                
                if (successful) {
                  return { ...item, progress: 100, status: 'completed' as const };
                } else if (failed) {
                  return { ...item, status: 'error' as const, error: failed.errorMessage };
                } else {
                  return { ...item, progress: 100, status: 'completed' as const };
                }
              })
            );
            
            // Return actual counts from response
            resolve({
              successCount: response.successCount || 0,
              errorCount: response.failureCount || 0,
              response
            });
          } catch (e) {
            // Fallback: mark all as completed and assume success
            setUploadProgress(prev => 
              prev.map(item => ({ ...item, progress: 100, status: 'completed' as const }))
            );
            resolve({
              successCount: files.length,
              errorCount: 0
            });
          }
        } else {
          const error = `Bulk upload failed: ${xhr.status} ${xhr.statusText}`;
          setUploadProgress(prev => 
            prev.map(item => ({ ...item, status: 'error' as const, error }))
          );
          resolve({
            successCount: 0,
            errorCount: files.length
          });
        }
      });

      // Handle errors
      xhr.addEventListener('error', () => {
        const error = 'Network error occurred during bulk upload';
        setUploadProgress(prev => 
          prev.map(item => ({ ...item, status: 'error' as const, error }))
        );
        resolve({
          successCount: 0,
          errorCount: files.length
        });
      });

      // Start bulk upload
      xhr.open('POST', `https://doqueue.ddns.net/api/tasks/${taskId}/attachments/bulk`);
      
      // Add authorization header if available
      const token = localStorage.getItem('token');
      if (token) {
        xhr.setRequestHeader('Authorization', `Bearer ${token}`);
      }
      
      xhr.send(formData);
    });
  };

  const handleFilesSelected = async (files: FileWithValidation[]) => {
    if (files.length === 0) return;

    setIsUploading(true);
    onUploadStart?.();

    // Initialize progress tracking
    const initialProgress: FileUploadProgress[] = files.map(file => ({
      file,
      progress: 0,
      status: 'uploading'
    }));
    setUploadProgress(initialProgress);

    try {
      if (files.length > 1) {
        // Use bulk upload for multiple files
        try {
          const bulkResult = await uploadFilesBulk(files);
          
          // Use direct response data instead of stale state
          if (bulkResult.successCount > 0) {
            toast.success(`${bulkResult.successCount} file${bulkResult.successCount > 1 ? 's' : ''} uploaded successfully!`);
            onUploadComplete?.();
          }

          if (bulkResult.errorCount > 0) {
            toast.error(`${bulkResult.errorCount} file${bulkResult.errorCount > 1 ? 's' : ''} failed to upload`);
          }
        } catch (bulkError) {
          console.log('Bulk upload failed, falling back to individual uploads:', bulkError);
          // Fallback to individual uploads
          const results = await Promise.allSettled(
            files.map(file => uploadFile(file))
          );
          
          // Count results from Promise responses
          let successCount = 0;
          let errorCount = 0;
          
          results.forEach((result) => {
            if (result.status === 'fulfilled' && result.value.success) {
              successCount++;
            } else {
              errorCount++;
            }
          });

          if (successCount > 0) {
            toast.success(`${successCount} file${successCount > 1 ? 's' : ''} uploaded successfully!`);
            onUploadComplete?.();
          }

          if (errorCount > 0) {
            toast.error(`${errorCount} file${errorCount > 1 ? 's' : ''} failed to upload`);
          }
        }
      } else {
        // Single file upload
        const result = await uploadFile(files[0]);
        
        if (result.success) {
          toast.success('File uploaded successfully!');
          onUploadComplete?.();
        } else {
          toast.error('File upload failed');
        }
      }

    } catch (error) {
      console.error('Upload error:', error);
      toast.error('Upload failed. Please try again.');
    } finally {
      setIsUploading(false);
      // Clear progress after a delay
      setTimeout(() => {
        setUploadProgress([]);
      }, 3000);
    }
  };

  const removeFromProgress = (file: File) => {
    setUploadProgress(prev => prev.filter(item => item.file !== file));
  };

  return (
    <div className={`space-y-4 ${className}`}>
      {/* File Dropzone */}
      <FileDropzone
        onFilesSelected={handleFilesSelected}
        disabled={disabled || isUploading}
      />

      {/* Upload Progress */}
      {uploadProgress.length > 0 && (
        <div className="space-y-3">
          <h4 className="text-sm font-medium text-gray-900 flex items-center space-x-2">
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
                d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12"
              />
            </svg>
            <span>Upload Progress</span>
          </h4>

          <div className="space-y-2">
            {uploadProgress.map((item, index) => (
              <div
                key={index}
                className="bg-white border border-gray-200 rounded-lg p-3"
              >
                <div className="flex items-center justify-between mb-2">
                  <div className="flex items-center space-x-2 flex-1">
                    <span className="text-lg">📎</span>
                    <div className="flex-1 min-w-0">
                      <p className="text-sm font-medium text-gray-900 truncate">
                        {item.file.name}
                      </p>
                      <p className="text-xs text-gray-500">
                        {formatFileSize(item.file.size)}
                      </p>
                    </div>
                  </div>

                  <div className="flex items-center space-x-2">
                    {item.status === 'uploading' && (
                      <span className="text-xs text-blue-600 font-medium">
                        {item.progress}%
                      </span>
                    )}
                    
                    {item.status === 'completed' && (
                      <svg
                        className="h-5 w-5 text-green-600"
                        fill="none"
                        stroke="currentColor"
                        viewBox="0 0 24 24"
                      >
                        <path
                          strokeLinecap="round"
                          strokeLinejoin="round"
                          strokeWidth={2}
                          d="M5 13l4 4L19 7"
                        />
                      </svg>
                    )}
                    
                    {item.status === 'error' && (
                      <button
                        onClick={() => removeFromProgress(item.file)}
                        className="text-red-600 hover:text-red-800"
                        title="Remove from list"
                      >
                        <svg
                          className="h-5 w-5"
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
                    )}
                  </div>
                </div>

                {/* Progress Bar */}
                {item.status === 'uploading' && (
                  <div className="w-full bg-gray-200 rounded-full h-2">
                    <div
                      className="bg-blue-600 h-2 rounded-full transition-all duration-300"
                      style={{ width: `${item.progress}%` }}
                    />
                  </div>
                )}

                {/* Error Message */}
                {item.status === 'error' && item.error && (
                  <div className="mt-2 p-2 bg-red-50 border border-red-200 rounded text-xs text-red-600">
                    {item.error}
                  </div>
                )}

                {/* Success Message */}
                {item.status === 'completed' && (
                  <div className="mt-2 p-2 bg-green-50 border border-green-200 rounded text-xs text-green-600">
                    Upload completed successfully
                  </div>
                )}
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Overall Status */}
      {isUploading && (
        <div className="text-center py-2">
          <div className="inline-flex items-center space-x-2 text-sm text-blue-600">
            <svg
              className="animate-spin h-4 w-4"
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
              />
              <path
                className="opacity-75"
                fill="currentColor"
                d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
              />
            </svg>
            <span>Uploading files...</span>
          </div>
        </div>
      )}
    </div>
  );
};

export default AttachmentUploader;
