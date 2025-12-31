import React, { useCallback, useState } from "react";
import { useDropzone } from "react-dropzone";
import toast from "react-hot-toast";

export interface FileWithValidation extends File {
  validationError?: string;
}

interface FileDropzoneProps {
  onFilesSelected: (files: FileWithValidation[]) => void;
  disabled?: boolean;
  maxFileSize?: number; // in bytes
  acceptedFileTypes?: string[];
  maxFiles?: number;
  className?: string;
}

const FileDropzone: React.FC<FileDropzoneProps> = ({
  onFilesSelected,
  disabled = false,
  maxFileSize = 10 * 1024 * 1024, // 10MB default
  acceptedFileTypes = [
    // Documents
    '.pdf', '.doc', '.docx', '.txt', '.rtf',
    // Images
    '.jpg', '.jpeg', '.png', '.gif', '.webp',
    // Spreadsheets
    '.xls', '.xlsx', '.csv',
    // Presentations
    '.ppt', '.pptx',
    // Archives
    '.zip', '.rar', '.7z',
    // Other
    '.json', '.xml'
  ],
  maxFiles = 5,
  className = "",
}) => {
  const [dragCount, setDragCount] = useState(0);

  const validateFile = (file: File): string | null => {
    // Check file size
    if (file.size > maxFileSize) {
      const maxSizeMB = maxFileSize / (1024 * 1024);
      return `File size exceeds ${maxSizeMB}MB limit`;
    }

    // Check file type
    const fileExtension = '.' + file.name.split('.').pop()?.toLowerCase();
    if (!acceptedFileTypes.includes(fileExtension)) {
      return `File type not supported. Allowed types: ${acceptedFileTypes.join(', ')}`;
    }

    // Check for dangerous file extensions
    const dangerousExtensions = ['.exe', '.bat', '.cmd', '.scr', '.vbs', '.js', '.jar'];
    if (dangerousExtensions.includes(fileExtension)) {
      return `File type not allowed for security reasons`;
    }

    return null;
  };

  const onDrop = useCallback((acceptedFiles: File[], rejectedFiles: any[]) => {
    // Handle rejected files
    rejectedFiles.forEach((rejection) => {
      const errors = rejection.errors.map((e: any) => e.message).join(', ');
      toast.error(`${rejection.file.name}: ${errors}`);
    });

    if (acceptedFiles.length === 0 && rejectedFiles.length > 0) {
      return;
    }

    // Validate accepted files
    const validatedFiles: FileWithValidation[] = acceptedFiles.map((file) => {
      const validationError = validateFile(file);
      const fileWithValidation = file as FileWithValidation;
      
      if (validationError) {
        fileWithValidation.validationError = validationError;
        toast.error(`${file.name}: ${validationError}`);
      }
      
      return fileWithValidation;
    });

    // Filter out files with validation errors
    const validFiles = validatedFiles.filter(file => !file.validationError);

    if (validFiles.length > 0) {
      onFilesSelected(validFiles);
      
      // Show success message
      toast.success(
        `${validFiles.length} file${validFiles.length > 1 ? 's' : ''} ready to upload`
      );
    }

    setDragCount(0);
  }, [acceptedFileTypes, maxFileSize, onFilesSelected]);

  const {
    getRootProps,
    getInputProps,
    isDragActive,
    isDragReject,
  } = useDropzone({
    onDrop,
    disabled,
    maxFiles,
    maxSize: maxFileSize,
    accept: {
      // Convert our string array to the format react-dropzone expects
      'application/pdf': ['.pdf'],
      'application/msword': ['.doc'],
      'application/vnd.openxmlformats-officedocument.wordprocessingml.document': ['.docx'],
      'text/plain': ['.txt'],
      'image/jpeg': ['.jpg', '.jpeg'],
      'image/png': ['.png'],
      'image/gif': ['.gif'],
      'image/webp': ['.webp'],
      'application/vnd.ms-excel': ['.xls'],
      'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet': ['.xlsx'],
      'text/csv': ['.csv'],
      'application/vnd.ms-powerpoint': ['.ppt'],
      'application/vnd.openxmlformats-officedocument.presentationml.presentation': ['.pptx'],
      'application/zip': ['.zip'],
      'application/json': ['.json'],
      'application/xml': ['.xml'],
    },
    onDragEnter: () => setDragCount(prev => prev + 1),
    onDragLeave: () => setDragCount(prev => prev - 1),
  });

  const formatFileSize = (bytes: number): string => {
    if (bytes === 0) return '0 B';
    const k = 1024;
    const sizes = ['B', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return `${parseFloat((bytes / Math.pow(k, i)).toFixed(1))} ${sizes[i]}`;
  };

  const getDropzoneStyle = () => {
    if (disabled) {
      return "border-gray-200 bg-gray-50 cursor-not-allowed";
    }
    
    if (isDragReject) {
      return "border-red-300 bg-red-50 border-solid";
    }
    
    if (isDragActive || dragCount > 0) {
      return "border-blue-400 bg-blue-50 border-solid scale-105";
    }
    
    return "border-gray-300 bg-white hover:border-blue-400 hover:bg-blue-50";
  };

  return (
    <div className={`w-full ${className}`}>
      <div
        {...getRootProps()}
        className={`
          relative border-2 border-dashed rounded-lg p-8 text-center transition-all duration-200 
          ${getDropzoneStyle()}
          ${disabled ? '' : 'cursor-pointer'}
        `}
      >
        <input {...getInputProps()} />
        
        <div className="space-y-4">
          {/* Upload Icon */}
          <div className="mx-auto flex items-center justify-center h-12 w-12 rounded-full bg-gray-100">
            {isDragActive ? (
              <svg
                className={`h-6 w-6 ${isDragReject ? 'text-red-600' : 'text-blue-600'}`}
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M9 19l3 3m0 0l3-3m-3 3V10"
                />
              </svg>
            ) : (
              <svg
                className={`h-6 w-6 ${disabled ? 'text-gray-400' : 'text-gray-600'}`}
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M12 4v16m8-8H4"
                />
              </svg>
            )}
          </div>

          {/* Upload Text */}
          <div>
            {isDragActive ? (
              <p className={`text-sm font-medium ${isDragReject ? 'text-red-600' : 'text-blue-600'}`}>
                {isDragReject ? 'Some files are not supported' : 'Drop files here...'}
              </p>
            ) : (
              <>
                <p className={`text-sm font-medium ${disabled ? 'text-gray-400' : 'text-gray-900'}`}>
                  {disabled ? 'Upload disabled' : 'Drag & drop files here, or click to browse'}
                </p>
                {!disabled && (
                  <p className="text-xs text-gray-500 mt-1">
                    Maximum {maxFiles} files, up to {formatFileSize(maxFileSize)} each
                  </p>
                )}
              </>
            )}
          </div>

          {/* Supported file types */}
          {!disabled && !isDragActive && (
            <div className="text-xs text-gray-400">
              <p className="font-medium mb-1">Supported formats:</p>
              <p className="leading-relaxed">
                Documents (PDF, DOC, DOCX, TXT), Images (JPG, PNG, GIF), 
                Spreadsheets (XLS, XLSX, CSV), Archives (ZIP), and more
              </p>
            </div>
          )}
        </div>

        {/* Loading overlay when disabled */}
        {disabled && (
          <div className="absolute inset-0 bg-gray-50 bg-opacity-75 flex items-center justify-center rounded-lg">
            <svg
              className="animate-spin h-6 w-6 text-gray-400"
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
          </div>
        )}
      </div>
    </div>
  );
};

export default FileDropzone;
