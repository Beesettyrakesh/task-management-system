package com.rakesh.taskmanagement.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "attachments")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Attachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotBlank(message = "Original filename is required")
    private String originalFilename;

    @Column(nullable = false)
    @NotBlank(message = "Stored filename is required")
    private String storedFilename;

    @Column(nullable = false)
    @NotBlank(message = "Content type is required")
    private String contentType;

    @Column(nullable = false)
    @NotNull(message = "File size is required")
    private Long fileSize;

    @Column(nullable = false)
    @NotBlank(message = "Storage path is required")
    private String storagePath;

    // ===== RELATIONSHIPS =====
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // ===== AUDIT FIELDS =====
    
    @CreatedDate
    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private LocalDateTime uploadedAt;

    @Override
    public String toString() {
        return "Attachment{id=" + id + ", originalFilename='" + originalFilename + "', fileSize=" + fileSize + ", contentType='" + contentType + "'}";
    }
}
