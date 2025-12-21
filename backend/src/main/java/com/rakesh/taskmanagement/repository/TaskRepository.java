package com.rakesh.taskmanagement.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.rakesh.taskmanagement.entity.Priority;
import com.rakesh.taskmanagement.entity.Task;
import com.rakesh.taskmanagement.entity.TaskStatus;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByUserId(Long userId);

    @Query("SELECT DISTINCT t FROM Task t LEFT JOIN FETCH t.tags WHERE t.user.id = :userId")
    List<Task> findAllByUserIdWithTags(@Param("userId") Long userId);

    @Query("SELECT t FROM Task t LEFT JOIN FETCH t.tags WHERE t.id = :id")
    Optional<Task> findByIdWithTags(@Param("id") Long id);

    @Query("SELECT t FROM Task t LEFT JOIN FETCH t.tags WHERE t.id = :id AND t.user.id = :userId")
    Optional<Task> findByIdAndUserIdWithTags(@Param("id") Long id, @Param("userId") Long userId);

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

    // Combined filters (status + priority + sorting)
    List<Task> findByUserIdAndStatusOrderByDueDateAsc(Long userId, TaskStatus status);
    List<Task> findByUserIdAndPriorityOrderByDueDateAsc(Long userId, Priority priority);
    List<Task> findByUserIdAndStatusAndPriority(Long userId, TaskStatus status, Priority priority);
    List<Task> findByUserIdAndStatusAndPriorityOrderByDueDateAsc(Long userId, TaskStatus status, Priority priority);
    List<Task> findByUserIdAndStatusAndPriorityOrderByDueDateDesc(Long userId, TaskStatus status, Priority priority);
    List<Task> findByUserIdAndStatusAndPriorityOrderByCreatedAtDesc(Long userId, TaskStatus status, Priority priority);
    List<Task> findByUserIdAndStatusAndPriorityOrderByCreatedAtAsc(Long userId, TaskStatus status, Priority priority);
    List<Task> findByUserIdAndStatusAndPriorityOrderByPriorityAsc(Long userId, TaskStatus status, Priority priority);
    List<Task> findByUserIdAndStatusAndPriorityOrderByPriorityDesc(Long userId, TaskStatus status, Priority priority);


    // Status + Priority sorting combinations
    List<Task> findByUserIdAndStatusOrderByPriorityAsc(Long userId, TaskStatus status);
    List<Task> findByUserIdAndStatusOrderByPriorityDesc(Long userId, TaskStatus status);
    List<Task> findByUserIdAndStatusOrderByDueDateDesc(Long userId, TaskStatus status);
    List<Task> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, TaskStatus status);
    List<Task> findByUserIdAndStatusOrderByCreatedAtAsc(Long userId, TaskStatus status);


    // Priority + Priority sorting combinations (for completeness) 
    List<Task> findByUserIdAndPriorityOrderByPriorityAsc(Long userId, Priority priority);  
    List<Task> findByUserIdAndPriorityOrderByPriorityDesc(Long userId, Priority priority);
    
    // Priority + Sorting  
    List<Task> findByUserIdAndPriorityOrderByDueDateDesc(Long userId, Priority priority);
    List<Task> findByUserIdAndPriorityOrderByCreatedAtDesc(Long userId, Priority priority);
    List<Task> findByUserIdAndPriorityOrderByCreatedAtAsc(Long userId, Priority priority);

}
