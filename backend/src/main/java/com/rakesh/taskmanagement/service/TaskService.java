package com.rakesh.taskmanagement.service;

import java.time.LocalDate;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableArgumentResolver;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rakesh.taskmanagement.dto.TaskRequestDto;
import com.rakesh.taskmanagement.dto.TaskStatisticsDto;
import com.rakesh.taskmanagement.entity.Priority;
import com.rakesh.taskmanagement.entity.Tag;
import com.rakesh.taskmanagement.entity.Task;
import com.rakesh.taskmanagement.entity.TaskStatus;
import com.rakesh.taskmanagement.entity.User;
import com.rakesh.taskmanagement.exception.ResourceNotFoundException;
import com.rakesh.taskmanagement.repository.TagRepository;
import com.rakesh.taskmanagement.repository.TaskRepository;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService {

    private final UserService userService;
    private final TaskRepository taskRepository;
    private final TagService tagService;
    private final TagRepository tagRepository;
    private final EmailService emailService;
    private final PageableArgumentResolver pageableArgumentResolver;

    private Task convertDtoToEntity(TaskRequestDto dto) {
        Task task = new Task();
        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        task.setStatus(dto.getStatus());
        task.setPriority(dto.getPriority());
        task.setDueDate(dto.getDueDate());
        return task;
    }

    public Task createTask(TaskRequestDto taskRequestDto) {
        User currentUser = userService.getCurrentUser();
        log.info("Creating new task: '{}' for user: {}", taskRequestDto.getTitle(), currentUser.getUsername());
        
        Task task = convertDtoToEntity(taskRequestDto);
        task.setUser(currentUser);

        if(taskRequestDto.getDueDate() != null && taskRequestDto.getDueDate().isBefore(LocalDate.now())) {
            log.warn("Task creation failed: Due date in the past for user: {}, task: '{}'", 
                     currentUser.getUsername(), taskRequestDto.getTitle());
            throw new IllegalArgumentException("Due date cannot be in the past");
        }

        if (taskRequestDto.getTagIds() != null && !taskRequestDto.getTagIds().isEmpty()) {
            log.debug("Assigning {} tags to new task for user: {}", 
                     taskRequestDto.getTagIds().size(), currentUser.getUsername());
            List<Tag> tags = tagRepository.findAllById(taskRequestDto.getTagIds());
            task.setTags(new HashSet<>(tags));
        }
        
        Task savedTask = taskRepository.save(task);
        log.info("Task created successfully with ID: {} for user: {}", savedTask.getId(), currentUser.getUsername());

        try {
            emailService.sendTaskCreatedEmail(currentUser, savedTask);
            log.info("Task creation email sent for task: {}", savedTask.getId());
        } catch (Exception e) {
            log.error("Failed to send task created email for task: {} - {}", savedTask.getId(), e.getMessage());
        }

        return savedTask;
    }

    @Transactional(readOnly = true)
    public List<Task> getAllTasks() {
        User currentUser = userService.getCurrentUser();
        log.debug("Fetching all tasks for user: {}", currentUser.getUsername());
        
        List<Task> tasks = taskRepository.findByUserIdOptimized(currentUser.getId());

        log.info("Found {} tasks for user: {}", tasks.size(), currentUser.getUsername());
        
        return tasks;
    }

    @Transactional(readOnly = true)
    public Task getTaskById(Long id) {
        User currentUser = userService.getCurrentUser();
        log.debug("Fetching task ID: {} for user: {}", id, currentUser.getUsername());
        
        Task task = taskRepository
                .findByIdAndUserIdWithTags(id, currentUser.getId())
                .orElseThrow(() -> {
                    log.warn("Task not found: ID {} requested by user: {}", id, currentUser.getUsername());
                    return new ResourceNotFoundException("Task not found");
                });
        
        log.debug("Task ID: {} found and authorized for user: {}", id, currentUser.getUsername());
        return task;
    }

    @Transactional
    public Task updateTask(Long id, @Valid TaskRequestDto task) {
        User currentUser = userService.getCurrentUser();
        log.info("Updating task with ID: {} for user: {}", id, currentUser.getUsername());
        
        Task existingTask = taskRepository
                .findById(id)
                .orElseThrow(() -> {
                    log.warn("Task not found during update: ID {} requested by user: {}", id, currentUser.getUsername());
                    return new ResourceNotFoundException("Task not found");
                });

        if(!existingTask.getUser().getId().equals(currentUser.getId())) {
            log.warn("Security violation during update: User {} attempted to access task {} owned by {}", 
                     currentUser.getUsername(), id, existingTask.getUser().getUsername());
            throw new ResourceNotFoundException("Task not found");
        }
        
        log.debug("Updating task '{}' with new data: title='{}', status='{}'", 
                  existingTask.getTitle(), task.getTitle(), task.getStatus());

        existingTask.setTitle(task.getTitle());
        existingTask.setDescription(task.getDescription());
        existingTask.setPriority(task.getPriority());
        existingTask.setStatus(task.getStatus());
        existingTask.setDueDate(task.getDueDate());

        if (task.getTagIds() != null && !task.getTagIds().isEmpty()) {
            log.debug("Updating task tags: {} tags assigned", task.getTagIds().size());
            List<Tag> managedTags = tagRepository.findAllById(task.getTagIds());
            existingTask.setTags(new HashSet<>(managedTags));
        } else {
            existingTask.setTags(new HashSet<>());
        }
        
        Task updatedTask = taskRepository.save(existingTask);
        log.info("Task ID: {} updated successfully for user: {}", updatedTask.getId(), currentUser.getUsername());
        
        return updatedTask;
    }

    public void deleteTask(Long id) {
        User currentUser = userService.getCurrentUser();
        log.info("Deleting task with ID: {} for user: {}", id, currentUser.getUsername());
        
        Task task = taskRepository
                .findById(id)
                .orElseThrow(() -> {
                    log.warn("Task not found during deletion: ID {} requested by user: {}", id, currentUser.getUsername());
                    return new ResourceNotFoundException("Task not found");
                });
                
        if(task.getUser().getId().equals(currentUser.getId())) {
            log.debug("Deleting task: '{}' (Status: {}, Priority: {})", 
                      task.getTitle(), task.getStatus(), task.getPriority());
            taskRepository.deleteById(id);
            log.info("Task ID: {} deleted successfully for user: {}", id, currentUser.getUsername());
        } else  {
            log.warn("Security violation during deletion: User {} attempted to delete task {} owned by {}", 
                     currentUser.getUsername(), id, task.getUser().getUsername());
            throw new ResourceNotFoundException("Task not found");
        }
    }

    public List<Task> getTasksByStatus(TaskStatus status) {
        User currentUser = userService.getCurrentUser();
        return taskRepository.findByUserIdAndStatus(currentUser.getId(), status);
    }

    public List<Task> getTasksByPriority(Priority priority) {
        User currentUser = userService.getCurrentUser();
        return taskRepository.findByUserIdAndPriority(currentUser.getId(), priority);
    }

    private List<Task> getTasksSortedByDueDate(String direction) {
        User currentUser = userService.getCurrentUser();
        if("desc".equalsIgnoreCase(direction)) {
            return taskRepository.findByUserIdOrderByDueDateDesc(currentUser.getId());
        }

        return taskRepository.findByUserIdOrderByDueDateAsc(currentUser.getId());
    }

    private List<Task> getTasksSortedByCreatedAt() {
        User currentUser = userService.getCurrentUser();
        return taskRepository.findByUserIdOrderByCreatedAtDesc(currentUser.getId());
    }

    private List<Task> getTasksSortedByPriority(String sortDirection) {
        User currentUser = userService.getCurrentUser();
        if("asc".equalsIgnoreCase(sortDirection)) {
            return taskRepository.findByUserIdOrderByPriorityAsc(currentUser.getId());
        }
        return taskRepository.findByUserIdOrderByPriorityDesc(currentUser.getId());
    }

    public List<Task> getTasksByTagName(String tagName) {
        User currentUser = userService.getCurrentUser();
        log.info("Filtering tasks by tag: '{}' for user: {}", tagName, currentUser.getUsername());
        
        List<Long> taskIds = taskRepository.findTaskIdsByUserIdAndTagName(currentUser.getId(), tagName);
        
        if (taskIds.isEmpty()) {
            return List.of();
        }
        
        return taskRepository.findTasksWithAllTagsByIds(taskIds);
    }

    public List<Task> getFilteredTasks(TaskStatus status, Priority priority, String sortBy, String sortDirection) {
        User currentUser = userService.getCurrentUser();
        Long userId = currentUser.getId();
        
        if (status != null && priority != null) {
            if ("dueDate".equals(sortBy)) {
                return "desc".equalsIgnoreCase(sortDirection) 
                    ? taskRepository.findByUserIdAndStatusAndPriorityOrderByDueDateDesc(userId, status, priority)
                    : taskRepository.findByUserIdAndStatusAndPriorityOrderByDueDateAsc(userId, status, priority);
            } else if ("priority".equals(sortBy)) {
                return "desc".equalsIgnoreCase(sortDirection)
                ? taskRepository.findByUserIdAndStatusAndPriorityOrderByPriorityDesc(userId, status, priority)
                : taskRepository.findByUserIdAndStatusAndPriorityOrderByPriorityAsc(userId, status, priority);
            } else if ("createdAt".equals(sortBy)) {
                return "desc".equalsIgnoreCase(sortDirection)
                    ? taskRepository.findByUserIdAndStatusAndPriorityOrderByCreatedAtDesc(userId, status, priority)
                    : taskRepository.findByUserIdAndStatusAndPriorityOrderByCreatedAtAsc(userId, status, priority);
            } 
            
            return taskRepository.findByUserIdAndStatusAndPriority(userId, status, priority);
        }
        
        if (status != null) {
            if ("dueDate".equals(sortBy)) {
                return "desc".equalsIgnoreCase(sortDirection)
                    ? taskRepository.findByUserIdAndStatusOrderByDueDateDesc(userId, status)
                    : taskRepository.findByUserIdAndStatusOrderByDueDateAsc(userId, status);
            } else if ("priority".equals(sortBy)) {
                return "desc".equalsIgnoreCase(sortDirection)
                    ? taskRepository.findByUserIdAndStatusOrderByPriorityDesc(userId, status)
                    : taskRepository.findByUserIdAndStatusOrderByPriorityAsc(userId, status);
            } else if ("createdAt".equals(sortBy)) {
                return "desc".equalsIgnoreCase(sortDirection)
                    ? taskRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, status)
                    : taskRepository.findByUserIdAndStatusOrderByCreatedAtAsc(userId, status);
            }

            return taskRepository.findByUserIdAndStatus(userId, status);
        }
        
        if (priority != null) {
            if ("dueDate".equals(sortBy)) {
                return "desc".equalsIgnoreCase(sortDirection)
                    ? taskRepository.findByUserIdAndPriorityOrderByDueDateDesc(userId, priority)
                    : taskRepository.findByUserIdAndPriorityOrderByDueDateAsc(userId, priority);
            } else if ("priority".equals(sortBy)) {
                return "desc".equalsIgnoreCase(sortDirection)
                    ? taskRepository.findByUserIdAndPriorityOrderByPriorityDesc(userId, priority)
                    : taskRepository.findByUserIdAndPriorityOrderByPriorityAsc(userId, priority);
            } else if ("createdAt".equals(sortBy)) {
                return "desc".equalsIgnoreCase(sortDirection)
                    ? taskRepository.findByUserIdAndPriorityOrderByCreatedAtDesc(userId, priority)
                    : taskRepository.findByUserIdAndPriorityOrderByCreatedAtAsc(userId, priority);
            }

            return taskRepository.findByUserIdAndPriority(userId, priority);
        }
        
        if ("dueDate".equals(sortBy)) {
            return getTasksSortedByDueDate(sortDirection);
        } else if ("createdAt".equals(sortBy)) {
            return getTasksSortedByCreatedAt();
        } else if ("priority".equals(sortBy)) {
            return getTasksSortedByPriority(sortDirection);
        }
        
        return getAllTasks();
    }

    public Page<Task> getFilteredTasksPageable(
            TaskStatus status,
            Priority priority,
            String tagName,
            Pageable pageable
    ) {
        User currentUser = userService.getCurrentUser();
        Long userId = currentUser.getId();

        if(tagName != null && !tagName.trim().isEmpty()){
            List<Long> taskIds = taskRepository.findTaskIdsByUserIdAndTagName(userId, tagName);

            if(taskIds.isEmpty()) {
                return Page.empty(pageable);
            }

            List<Task> tasks = taskRepository.findTasksWithAllTagsByIds(taskIds);

            if(status != null) {
                tasks = tasks.stream()
                        .filter(task -> task.getStatus() == status)
                        .toList();
            }

            if(priority != null) {
                tasks = tasks.stream()
                        .filter(task -> task.getPriority() == priority)
                        .toList();
            }

            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), tasks.size());
            List<Task> pageContent = tasks.subList(start, end);

            return new PageImpl<>(pageContent, pageable, tasks.size());
        }

        if(status != null && priority != null) {
            return taskRepository.findByUserIdAndStatusAndPriority(userId, status, priority,  pageable);
        } else if(status != null) {
            return taskRepository.findByUserIdAndStatus(userId, status, pageable);
        } else if (priority != null) {
            return taskRepository.findByUserIdAndPriority(userId, priority, pageable);
        }

        return taskRepository.findByUserId(userId, pageable);
    }

    @Transactional
    public void assignTagToTask(Long taskId, Long tagId) {
        Task task = getTaskById(taskId);
        Tag tag = tagService.getTagById(tagId);

        task.getTags().add(tag);
        taskRepository.save(task);
    }

    @Transactional
    public void removeTagFromTask(Long taskId, Long tagId) {
        Task task = getTaskById(taskId);
        Tag tag = tagService.getTagById(tagId);

        task.getTags().remove(tag);
        taskRepository.save(task);
    }

    public TaskStatisticsDto getStatistics() {
        User currentUser = userService.getCurrentUser();
        Long userId = currentUser.getId();

        Long totalTasks = taskRepository.countByUserId(userId);
        Long completedTasks = taskRepository.countByUserIdAndStatus(userId, TaskStatus.DONE);
        Long inProgressTasks = taskRepository.countByUserIdAndStatus(userId, TaskStatus.IN_PROGRESS);
        Long todoTasks = taskRepository.countByUserIdAndStatus(userId, TaskStatus.TODO);
        Long overdueTasks = taskRepository.countByUserIdAndDueDateBeforeAndStatusNot(userId, LocalDate.now(), TaskStatus.DONE);

        List<Object[]> priorityResults = taskRepository.getPriorityStatistics(userId);
        Map<Priority, Long> priorityStats = new EnumMap<>(Priority.class);
        
        for(Priority priority: Priority.values()) {
            priorityStats.put(priority, 0L);
        }
        
        for(Object[] result : priorityResults) {
            Priority priority = (Priority) result[0];
            Long count = ((Number) result[1]).longValue();
            priorityStats.put(priority, count);
        }

        return new TaskStatisticsDto(totalTasks, completedTasks, inProgressTasks, todoTasks, overdueTasks, priorityStats);
    }

    public List<Task> getRecentTasks() {
        User currentUser = userService.getCurrentUser();
        return taskRepository.findTop5ByUserIdWithTagsOrderByCreatedAtDesc(currentUser.getId());
    }

}
