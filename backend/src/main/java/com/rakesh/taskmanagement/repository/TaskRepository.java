package com.rakesh.taskmanagement.repository;

import java.time.LocalDate;
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

    // Statistics
    Long countByUserId(Long userId);
    Long countByUserIdAndStatus(Long userId, TaskStatus status);
    Long countByUserIdAndPriority(Long userId, Priority priority);
    Long countByUserIdAndDueDateBeforeAndStatusNot(Long userId, LocalDate date, TaskStatus status);
    List<Task> findTop5ByUserIdOrderByCreatedAtDesc(Long userId);

    List<Task> findByDueDate(LocalDate dueDate);
    List<Task> findByDueDateBefore(LocalDate date);
    List<Task> findByDueDateBetween(LocalDate startDate, LocalDate dueDate);

    // ⚡ QUERY OPTIMIZATION: JOIN FETCH methods to solve N+1 problems
    
    @Query("SELECT DISTINCT t FROM Task t JOIN FETCH t.user LEFT JOIN FETCH t.tags WHERE t.user.id = :userId")
    List<Task> findByUserIdOptimized(@Param("userId") Long userId);

    @Query("SELECT DISTINCT t FROM Task t LEFT JOIN FETCH t.tags WHERE t.user.id = :userId ORDER BY t.createdAt DESC LIMIT 5")
    List<Task> findTop5ByUserIdWithTagsOrderByCreatedAtDesc(@Param("userId") Long userId);

    // ⚡ OPTIMIZED STATISTICS: Single query to replace multiple count queries
    @Query("""
        SELECT 
            COUNT(t.id) as totalTasks,
            COALESCE(SUM(CASE WHEN t.status = 'DONE' THEN 1 ELSE 0 END), 0) as completedTasks,
            COALESCE(SUM(CASE WHEN t.status = 'IN_PROGRESS' THEN 1 ELSE 0 END), 0) as inProgressTasks,
            COALESCE(SUM(CASE WHEN t.status = 'TODO' THEN 1 ELSE 0 END), 0) as todoTasks,
            COALESCE(SUM(CASE WHEN t.dueDate < :currentDate AND t.status != 'DONE' THEN 1 ELSE 0 END), 0) as overdueTasks
        FROM Task t 
        WHERE t.user.id = :userId
        """)
    Object[] getTaskStatisticsSummary(@Param("userId") Long userId, @Param("currentDate") LocalDate currentDate);

    @Query("""
        SELECT t.priority as priority, COUNT(t.id) as count
        FROM Task t 
        WHERE t.user.id = :userId 
        GROUP BY t.priority
        """)
    List<Object[]> getPriorityStatistics(@Param("userId") Long userId);

    // Tag-based filtering - Two-step approach to fetch all tags for matching tasks
    @Query("SELECT DISTINCT t.id FROM Task t JOIN t.tags tag WHERE t.user.id = :userId AND tag.name = :tagName")
    List<Long> findTaskIdsByUserIdAndTagName(@Param("userId") Long userId, @Param("tagName") String tagName);
    
    @Query("SELECT DISTINCT t FROM Task t LEFT JOIN FETCH t.tags WHERE t.id IN :taskIds")
    List<Task> findTasksWithAllTagsByIds(@Param("taskIds") List<Long> taskIds);

}
