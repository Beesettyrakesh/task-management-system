package com.rakesh.taskmanagement.service;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import com.rakesh.taskmanagement.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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
        if (contentType == null || !isAllowedContentType(contentType)) {
            throw new IllegalArgumentException("File type not allowed");
        }
    }

    private boolean isAllowedContentType(String contentType) {
        return contentType.equals("application/pdf") ||
                contentType.equals("image/jpeg") ||
                contentType.equals("image/png") ||
                contentType.equals("text/plain") ||
                contentType.startsWith("application/vnd.openxmlformats");
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
