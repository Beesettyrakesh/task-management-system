package com.rakesh.taskmanagement.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tasks")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    private TaskStatus status;

    @Enumerated(EnumType.ORDINAL)
    private Priority priority;

    private LocalDate dueDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id",  nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User user;

    // ===== AUDIT FIELDS (Automatically managed by Spring) =====

    @CreatedDate // ← Automatically set on creation
    @Column(name = "created_at", nullable = false,  updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate // ← Automatically updated on every save
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @CreatedBy // ← Who created this (will be "system" until Spring Security is added)
    @Column(name = "created_by",  updatable = false)
    private String createdBy;

    @LastModifiedBy // ← Who last modified this
    @Column(name = "last_modified_by")
    private String lastModifiedBy;

}
