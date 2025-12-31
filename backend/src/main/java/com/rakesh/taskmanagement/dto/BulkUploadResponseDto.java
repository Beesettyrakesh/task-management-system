package com.rakesh.taskmanagement.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BulkUploadResponseDto {
    private int totalFiles;
    private int successCount;
    private int failureCount;
    private List<AttachmentResponseDto> successfulUploads;
    private List<FileUploadError> failedUploads;
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FileUploadError {
        private String filename;
        private String errorMessage;
    }
    
    public static BulkUploadResponseDto success(List<AttachmentResponseDto> uploads) {
        return new BulkUploadResponseDto(
            uploads.size(),
            uploads.size(),
            0,
            uploads,
            List.of()
        );
    }
    
    public static BulkUploadResponseDto mixed(
            List<AttachmentResponseDto> successful, 
            List<FileUploadError> failed) {
        return new BulkUploadResponseDto(
            successful.size() + failed.size(),
            successful.size(),
            failed.size(),
            successful,
            failed
        );
    }
}
