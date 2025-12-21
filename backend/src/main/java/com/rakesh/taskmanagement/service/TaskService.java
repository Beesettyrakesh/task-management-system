package com.rakesh.taskmanagement.service;

import java.util.List;

import com.rakesh.taskmanagement.entity.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rakesh.taskmanagement.exception.ResourceNotFoundException;
import com.rakesh.taskmanagement.repository.TaskRepository;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class TaskService {

    private final UserService userService;
    private final TaskRepository taskRepository;
    private final TagService tagService;

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

        task.setId(id);
        task.setUser(currentUser);
        return taskRepository.save(task);
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


}
