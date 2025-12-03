package com.rakesh.taskmanagement.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rakesh.taskmanagement.entity.Priority;
import com.rakesh.taskmanagement.entity.Task;
import com.rakesh.taskmanagement.entity.TaskStatus;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByUserId(Long userId);

    // Status-based filtering
    List<Task> findByUserIdAndStatus(Long userId, TaskStatus status);
    
    // Priority-based filtering
    List<Task> findByUserIdAndPriority(Long userId, Priority priority);

    // Date-based sorting
    List<Task> findByUserIdOrderByDueDateAsc(Long userId);
    List<Task> findByUserIdOrderByDueDateDesc(Long userId);

    // Creation date sorting (newest first)
    List<Task> findByUserIdOrderByCreatedAtDesc(Long userId);

    // Priority sorting
    List<Task> findByUserIdOrderByPriorityDesc(Long userId);
    List<Task> findByUserIdOrderByPriorityAsc(Long userId);

    // Combined filters (status + sorting)
    List<Task> findByUserIdAndStatusOrderByDueDateAsc(Long userId, TaskStatus status);
    List<Task> findByUserIdAndPriorityOrderByDueDateAsc(Long userId, Priority priority);
    List<Task> findByUserIdAndStatusAndPriority(Long userId, TaskStatus status, Priority priority);
    List<Task> findByUserIdAndStatusAndPriorityOrderByDueDateAsc(Long userId, TaskStatus status, Priority priority);
    List<Task> findByUserIdAndStatusAndPriorityOrderByDueDateDesc(Long userId, TaskStatus status, Priority priority);
    List<Task> findByUserIdAndStatusAndPriorityOrderByCreatedAtDesc(Long userId, TaskStatus status, Priority priority);

    // Status + Priority sorting combinations
    List<Task> findByUserIdAndStatusOrderByPriorityAsc(Long userId, TaskStatus status);
    List<Task> findByUserIdAndStatusOrderByPriorityDesc(Long userId, TaskStatus status);

    // Priority + Priority sorting combinations (for completeness) 
    List<Task> findByUserIdAndPriorityOrderByPriorityAsc(Long userId, Priority priority);  
    List<Task> findByUserIdAndPriorityOrderByPriorityDesc(Long userId, Priority priority);


}
