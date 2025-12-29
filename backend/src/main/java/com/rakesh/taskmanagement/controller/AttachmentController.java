package com.rakesh.taskmanagement.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.rakesh.taskmanagement.dto.AttachmentResponseDto;
import com.rakesh.taskmanagement.entity.Attachment;
import com.rakesh.taskmanagement.service.AttachmentService;

import lombok.RequiredArgsConstructor;

@RestController
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class AttachmentController {

    private final AttachmentService attachmentService;

    @PostMapping("/api/tasks/{taskId}/attachments")
    public ResponseEntity<AttachmentResponseDto> uploadFile(
            @PathVariable Long taskId,
            @RequestParam("file") MultipartFile file
    ) {
        Attachment attachment = attachmentService.uploadFile(taskId, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(AttachmentResponseDto.from(attachment));
    }

    @GetMapping("/api/tasks/{taskId}/attachments")
    public ResponseEntity<List<AttachmentResponseDto>> getFilesByTaskId(@PathVariable Long taskId) {
        List<Attachment> attachments = attachmentService.getFilesByTaskId(taskId);
        List<AttachmentResponseDto> dtos = attachments.stream()
                .map(AttachmentResponseDto::from)
                .collect(Collectors.toList());

        return  ResponseEntity.status(HttpStatus.OK).body(dtos);
    }

    @GetMapping("/api/attachments/{id}/download")
    public ResponseEntity<String> getDownloadUrl(@PathVariable Long id) {
        String downloadUrl = attachmentService.getDownloadUrl(id);
        return ResponseEntity.ok(downloadUrl);
    }

    @DeleteMapping("/api/attachments/{id}")
    public ResponseEntity<Void> deleteFile(@PathVariable Long id) {
        attachmentService.deleteFile(id);
        return ResponseEntity.noContent().build();
    }
}
