package com.rakesh.taskmanagement.dto;

import com.rakesh.taskmanagement.entity.Attachment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AttachmentResponseDto {
    private Long id;
    private String originalFileName;
    private String contentType;
    private Long fileSize;
    private String storagePath;
    private LocalDateTime uploadedAt;

    public static AttachmentResponseDto from(Attachment attachment) {
        return new AttachmentResponseDto(
                attachment.getId(),
                attachment.getOriginalFilename(),
                attachment.getContentType(),
                attachment.getFileSize(),
                attachment.getStoragePath(),
                attachment.getUploadedAt()
        );
    }
}
