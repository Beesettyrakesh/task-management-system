package com.rakesh.taskmanagement.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rakesh.taskmanagement.entity.Attachment;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, Long>{

    List<Attachment> findByTaskIdAndUserId(Long taskId, Long userId);
    List<Attachment> findByUserId(Long userId);
    Optional<Attachment> findByIdAndUserId(Long attachmentId, Long userId);
    long countByTaskIdAndUserId(Long taskId, Long userId);
    void deleteByTaskIdAndUserId(Long taskId, Long userId);
}
