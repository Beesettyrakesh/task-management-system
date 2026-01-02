package com.rakesh.taskmanagement.service;

import java.time.LocalDate;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.rakesh.taskmanagement.dto.TaskStatisticsDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rakesh.taskmanagement.entity.Priority;
import com.rakesh.taskmanagement.entity.Tag;
import com.rakesh.taskmanagement.entity.Task;
import com.rakesh.taskmanagement.entity.TaskStatus;
import com.rakesh.taskmanagement.entity.User;
import com.rakesh.taskmanagement.exception.ResourceNotFoundException;
import com.rakesh.taskmanagement.repository.TagRepository;
import com.rakesh.taskmanagement.repository.TaskRepository;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class TaskService {

    private final UserService userService;
    private final TaskRepository taskRepository;
    private final TagService tagService;
    private final TagRepository tagRepository;

    // CRUD services
    public Task createTask(Task task) {
        User currentUser = userService.getCurrentUser();
        task.setUser(currentUser);
        return taskRepository.save(task);
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
    public Task updateTask(Long id, Task task) {
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

        if (task.getTags() != null) {
            List<Long> tagIds = task.getTags().stream()
                .map(Tag::getId)
                .collect(Collectors.toList());

            List<Tag> managedTags = tagRepository.findAllById(tagIds);
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
