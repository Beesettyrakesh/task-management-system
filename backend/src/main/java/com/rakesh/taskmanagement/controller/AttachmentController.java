package com.rakesh.taskmanagement.controller;

import java.util.List;
import java.util.stream.Collectors;

import com.rakesh.taskmanagement.dto.AttachmentResponseDto;
import com.rakesh.taskmanagement.dto.BulkUploadResponseDto;
import com.rakesh.taskmanagement.dto.ErrorResponseDto;
import com.rakesh.taskmanagement.entity.Attachment;
import com.rakesh.taskmanagement.service.AttachmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Tag(name = "File Attachments", description = "File upload, download, and management operations for task attachments with AWS S3 integration")
public class AttachmentController {

    private final AttachmentService attachmentService;

    @Operation(
            summary = "Upload single file to task",
            description = "Upload a single file attachment to a specific task. File is stored in AWS S3 and metadata in database."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "File uploaded successfully",
                    content = @Content(schema = @Schema(implementation = AttachmentResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid file or file size exceeds limit",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Task not found or not owned by user",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "JWT token required")
    })
    @PostMapping("/api/tasks/{taskId}/attachments")
    public ResponseEntity<AttachmentResponseDto> uploadFile(
            @Parameter(description = "Task ID to attach file to", example = "1")
            @PathVariable Long taskId,
            @Parameter(description = "File to upload (max 10MB)")
            @RequestParam("file") MultipartFile file
    ) {
        Attachment attachment = attachmentService.uploadFile(taskId, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(AttachmentResponseDto.from(attachment));
    }

    @Operation(
            summary = "Upload multiple files to task (bulk upload)",
            description = "Upload multiple file attachments to a specific task in a single request. Provides detailed success/failure report."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "All files uploaded successfully",
                    content = @Content(schema = @Schema(implementation = BulkUploadResponseDto.class))),
            @ApiResponse(responseCode = "207", description = "Partial success - some files uploaded, some failed",
                    content = @Content(schema = @Schema(implementation = BulkUploadResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "All files failed to upload",
                    content = @Content(schema = @Schema(implementation = BulkUploadResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Task not found or not owned by user",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "JWT token required")
    })
    @PostMapping("/api/tasks/{taskId}/attachments/bulk")
    public ResponseEntity<BulkUploadResponseDto> uploadFiles(
            @Parameter(description = "Task ID to attach files to", example = "1")
            @PathVariable Long taskId,
            @Parameter(description = "Multiple files to upload (each max 10MB)")
            @RequestParam("files") MultipartFile[] files
    ) {
        BulkUploadResponseDto response = attachmentService.uploadFiles(taskId, files);
        
        // Return appropriate HTTP status based on results
        if (response.getFailureCount() == 0) {
            // All files uploaded successfully
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else if (response.getSuccessCount() > 0) {
            // Partial success (some files uploaded, some failed)
            return ResponseEntity.status(HttpStatus.MULTI_STATUS).body(response);
        } else {
            // All files failed
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @Operation(
            summary = "Get all attachments for a task",
            description = "Retrieve all file attachments associated with a specific task"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Attachments retrieved successfully",
                    content = @Content(schema = @Schema(implementation = AttachmentResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Task not found or not owned by user",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "JWT token required")
    })
    @GetMapping("/api/tasks/{taskId}/attachments")
    public ResponseEntity<List<AttachmentResponseDto>> getFilesByTaskId(
            @Parameter(description = "Task ID to get attachments for", example = "1")
            @PathVariable Long taskId) {
        List<Attachment> attachments = attachmentService.getFilesByTaskId(taskId);
        List<AttachmentResponseDto> dtos = attachments.stream()
                .map(AttachmentResponseDto::from)
                .collect(Collectors.toList());

        return  ResponseEntity.status(HttpStatus.OK).body(dtos);
    }

    @Operation(
            summary = "Get file download URL",
            description = "Get a pre-signed download URL for a specific attachment from AWS S3"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Download URL generated successfully",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "404", description = "Attachment not found or not owned by user",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "JWT token required")
    })
    @GetMapping("/api/attachments/{id}/download")
    public ResponseEntity<String> getDownloadUrl(
            @Parameter(description = "Attachment ID", example = "1")
            @PathVariable Long id) {
        String downloadUrl = attachmentService.getDownloadUrl(id);
        return ResponseEntity.ok(downloadUrl);
    }

    @Operation(
            summary = "Delete an attachment",
            description = "Permanently delete a file attachment from both AWS S3 and database"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "File deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Attachment not found or not owned by user",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "JWT token required")
    })
    @DeleteMapping("/api/attachments/{id}")
    public ResponseEntity<Void> deleteFile(
            @Parameter(description = "Attachment ID to delete", example = "1")
            @PathVariable Long id) {
        attachmentService.deleteFile(id);
        return ResponseEntity.noContent().build();
    }
}
