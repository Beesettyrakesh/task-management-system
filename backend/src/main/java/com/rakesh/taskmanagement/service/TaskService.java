package com.rakesh.taskmanagement.service;

import java.time.LocalDate;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
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


@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService {

    private final UserService userService;
    private final TaskRepository taskRepository;
    private final TagService tagService;
    private final TagRepository tagRepository;
    private final EmailService emailService;

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
        Task task = convertDtoToEntity(taskRequestDto);
        User currentUser = userService.getCurrentUser();
        task.setUser(currentUser);

        if(taskRequestDto.getDueDate() != null && taskRequestDto.getDueDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Due date cannot be in the past");
        }

        if (taskRequestDto.getTagIds() != null && !taskRequestDto.getTagIds().isEmpty()) {
            List<Tag> tags = tagRepository.findAllById(taskRequestDto.getTagIds());
            task.setTags(new HashSet<>(tags));
        }
        Task savedTask = taskRepository.save(task);

        try {
            emailService.sendTaskCreatedEmail(currentUser, savedTask);
            log.info("Task created email sent for task: {}", savedTask.getId());
        } catch (Exception e) {
            log.error("Failed to send task created email", e);
        }

        return savedTask;
    }

    @Transactional(readOnly = true)
    public List<Task> getAllTasks() {
        User currentUser = userService.getCurrentUser();
        return taskRepository.findAllByUserIdWithTags(currentUser.getId());
    }

    @Transactional(readOnly = true)
    public Task getTaskById(Long id) {
        User currentUser = userService.getCurrentUser();
        Task task = taskRepository
                .findByIdAndUserIdWithTags(id, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        
        return task;
    }

    @Transactional
    public Task updateTask(Long id, @Valid TaskRequestDto task) {
        User currentUser = userService.getCurrentUser();
        Task existingTask = taskRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        if(existingTask.getUser().getId() != currentUser.getId()) {
            throw new ResourceNotFoundException("Task not found");
        }

        existingTask.setTitle(task.getTitle());
        existingTask.setDescription(task.getDescription());
        existingTask.setPriority(task.getPriority());
        existingTask.setStatus(task.getStatus());
        existingTask.setDueDate(task.getDueDate());

        if (task.getTagIds() != null && !task.getTagIds().isEmpty()) {
            List<Tag> managedTags = tagRepository.findAllById(task.getTagIds());
            existingTask.setTags(new HashSet<>(managedTags));
        } else {
            existingTask.setTags(new HashSet<>());
        }
        return taskRepository.save(existingTask);
    }

    public void deleteTask(Long id) {
        User currentUser = userService.getCurrentUser();
        Task task = taskRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        if(task.getUser().getId() == currentUser.getId()) {
            taskRepository.deleteById(id);
        } else  {
            throw new ResourceNotFoundException("Task not found");
        }
    }

    // filtering services
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

        Map<Priority, Long> priorityStats = new EnumMap<>(Priority.class);

        for(Priority priority: Priority.values()) {
            priorityStats.put(priority,
                    taskRepository.countByUserIdAndPriority(userId, priority));
        }

        return new TaskStatisticsDto(totalTasks, completedTasks, inProgressTasks, todoTasks, overdueTasks, priorityStats);
    }

    public List<Task> getRecentTasks() {
        User currentUser = userService.getCurrentUser();
        return taskRepository.findTop5ByUserIdOrderByCreatedAtDesc(currentUser.getId());
    }

}
