package com.rakesh.taskmanagement.service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.rakesh.taskmanagement.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.rakesh.taskmanagement.dto.AttachmentResponseDto;
import com.rakesh.taskmanagement.dto.BulkUploadResponseDto;
import com.rakesh.taskmanagement.entity.Attachment;
import com.rakesh.taskmanagement.entity.Task;
import com.rakesh.taskmanagement.entity.User;
import com.rakesh.taskmanagement.repository.AttachmentRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

@Service
@RequiredArgsConstructor
public class AttachmentService {

    private final TaskService taskService;
    private final UserService userService;
    private final AttachmentRepository attachmentRepository;
    private final S3Client s3Client;

    @Value("${aws.s3.bucket.name}")
    private String bucketname;

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        long maxSize = 10 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("File size cannot exceed 10MB");
        }

        String contentType = file.getContentType();
        String filename = file.getOriginalFilename();
        
        if (contentType == null || !isAllowedContentType(contentType)) {
            throw new IllegalArgumentException("File type not allowed");
        }
        
        // Additional security: validate file extension
        if (filename == null || !isAllowedFileExtension(filename)) {
            throw new IllegalArgumentException("File extension not allowed");
        }
    }

    private boolean isAllowedFileExtension(String filename) {
        String extension = filename.toLowerCase();
        
        // Documents
        if (extension.endsWith(".pdf")) return true;
        if (extension.endsWith(".doc")) return true;
        if (extension.endsWith(".docx")) return true;
        if (extension.endsWith(".txt")) return true;
        if (extension.endsWith(".rtf")) return true;
        
        // Images
        if (extension.endsWith(".jpg")) return true;
        if (extension.endsWith(".jpeg")) return true;
        if (extension.endsWith(".png")) return true;
        if (extension.endsWith(".gif")) return true;
        if (extension.endsWith(".webp")) return true;
        
        // Spreadsheets
        if (extension.endsWith(".xls")) return true;
        if (extension.endsWith(".xlsx")) return true;
        if (extension.endsWith(".csv")) return true;
        
        // Presentations
        if (extension.endsWith(".ppt")) return true;
        if (extension.endsWith(".pptx")) return true;
        
        // Archives
        if (extension.endsWith(".zip")) return true;
        if (extension.endsWith(".rar")) return true;
        if (extension.endsWith(".7z")) return true;
        
        // Data formats
        if (extension.endsWith(".json")) return true;
        if (extension.endsWith(".xml")) return true;
        
        return false;
    }

    private boolean isAllowedContentType(String contentType) {
        // Documents
        if (contentType.equals("application/pdf")) return true;
        if (contentType.equals("application/msword")) return true; // DOC
        if (contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document")) return true; // DOCX
        if (contentType.equals("text/plain")) return true; // TXT
        if (contentType.equals("application/rtf")) return true; // RTF
        if (contentType.equals("text/rtf")) return true; // RTF alternative
        
        // Images
        if (contentType.equals("image/jpeg")) return true; // JPG/JPEG
        if (contentType.equals("image/png")) return true; // PNG
        if (contentType.equals("image/gif")) return true; // GIF
        if (contentType.equals("image/webp")) return true; // WEBP
        
        // Spreadsheets
        if (contentType.equals("application/vnd.ms-excel")) return true; // XLS
        if (contentType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) return true; // XLSX
        if (contentType.equals("text/csv")) return true; // CSV
        if (contentType.equals("application/csv")) return true; // CSV alternative
        
        // Presentations
        if (contentType.equals("application/vnd.ms-powerpoint")) return true; // PPT
        if (contentType.equals("application/vnd.openxmlformats-officedocument.presentationml.presentation")) return true; // PPTX
        
        // Archives
        if (contentType.equals("application/zip")) return true; // ZIP
        if (contentType.equals("application/x-zip-compressed")) return true; // ZIP alternative
        if (contentType.equals("application/x-rar-compressed")) return true; // RAR
        if (contentType.equals("application/x-7z-compressed")) return true; // 7Z
        
        // Data formats
        if (contentType.equals("application/json")) return true; // JSON
        if (contentType.equals("application/xml")) return true; // XML
        if (contentType.equals("text/xml")) return true; // XML alternative
        
        return false;
    }

    private String generateUniqueKey(String originalFileName) {
        String uuid = UUID.randomUUID().toString();
        String extension = getFileExtension(originalFileName);
        return "attachments/" + uuid + "_" + originalFileName + extension;
    }

    private String getFileExtension(String filename) {
        return filename.substring(filename.lastIndexOf("."));
    }

    @Transactional
    public Attachment uploadFile(Long taskId, MultipartFile file) {
        validateFile(file);
        User currentUser = userService.getCurrentUser();
        Task task = taskService.getTaskById(taskId);
        if(!task.getUser().getId().equals(currentUser.getId())) {
            throw new ResourceNotFoundException("Task not found");
        }

        String s3Key = generateUniqueKey(file.getOriginalFilename());

        try {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucketname)
                .key(s3Key)
                .contentType(file.getContentType())
                .build();

            s3Client.putObject(putRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            Attachment attachment = new Attachment();
            attachment.setOriginalFilename(file.getOriginalFilename());
            attachment.setFileSize(file.getSize());
            attachment.setContentType(file.getContentType());
            attachment.setStoragePath(s3Key);
            attachment.setStoredFilename(s3Key.substring(s3Key.lastIndexOf("/") + 1));
            attachment.setTask(task);
            attachment.setUser(currentUser);

            return attachmentRepository.save(attachment);
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload files:" + e.getMessage());
        }
    }

    @Transactional
    public BulkUploadResponseDto uploadFiles(Long taskId, MultipartFile[] files) {
        if (files == null || files.length == 0) {
            throw new IllegalArgumentException("No files provided for upload");
        }
        
        // Validate total upload size (50MB limit for bulk)
        long totalSize = 0;
        for (MultipartFile file : files) {
            totalSize += file.getSize();
        }
        if (totalSize > 50 * 1024 * 1024) {
            throw new IllegalArgumentException("Total file size cannot exceed 50MB for bulk upload");
        }

        User currentUser = userService.getCurrentUser();
        Task task = taskService.getTaskById(taskId);
        if(!task.getUser().getId().equals(currentUser.getId())) {
            throw new ResourceNotFoundException("Task not found");
        }

        List<AttachmentResponseDto> successfulUploads = new ArrayList<>();
        List<BulkUploadResponseDto.FileUploadError> failedUploads = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                // Individual file validation
                validateFile(file);
                
                String s3Key = generateUniqueKey(file.getOriginalFilename());

                // Upload to S3
                PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucketname)
                    .key(s3Key)
                    .contentType(file.getContentType())
                    .build();

                s3Client.putObject(putRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

                // Save to database
                Attachment attachment = new Attachment();
                attachment.setOriginalFilename(file.getOriginalFilename());
                attachment.setFileSize(file.getSize());
                attachment.setContentType(file.getContentType());
                attachment.setStoragePath(s3Key);
                attachment.setStoredFilename(s3Key.substring(s3Key.lastIndexOf("/") + 1));
                attachment.setTask(task);
                attachment.setUser(currentUser);

                Attachment savedAttachment = attachmentRepository.save(attachment);
                successfulUploads.add(AttachmentResponseDto.from(savedAttachment));

            } catch (Exception e) {
                failedUploads.add(new BulkUploadResponseDto.FileUploadError(
                    file.getOriginalFilename(), 
                    e.getMessage()
                ));
            }
        }

        // Return appropriate response based on results
        if (failedUploads.isEmpty()) {
            return BulkUploadResponseDto.success(successfulUploads);
        } else {
            return BulkUploadResponseDto.mixed(successfulUploads, failedUploads);
        }
    }

    public List<Attachment> getFilesByTaskId(Long taskId) {
        User currentUser = userService.getCurrentUser();
        Task task = taskService.getTaskById(taskId);
        if(!task.getUser().getId().equals(currentUser.getId())) {
            throw new ResourceNotFoundException("Task not found");
        }
        return attachmentRepository.findByTaskIdAndUserId(taskId, currentUser.getId());
    }

    public String getDownloadUrl(Long attachmentId) {
        User currentUser = userService.getCurrentUser();

        Attachment attachment = attachmentRepository.findByIdAndUserId(attachmentId, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Attachment not found"));

        try (S3Presigner presigner = S3Presigner.create()){
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketname)
                    .key(attachment.getStoragePath())
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofHours(1))
                    .getObjectRequest(getObjectRequest)
                    .build();

            return presigner.presignGetObject(presignRequest).url().toString();
        }
    }

    @Transactional
    public void deleteFile(Long attachmentId) {
        User currentUser = userService.getCurrentUser();

        Attachment attachment = attachmentRepository.findByIdAndUserId(attachmentId, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Attachment not found"));

        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketname)
                    .key(attachment.getStoragePath())
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            attachmentRepository.delete(attachment);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete file:" + e.getMessage());
        }
    }
}
