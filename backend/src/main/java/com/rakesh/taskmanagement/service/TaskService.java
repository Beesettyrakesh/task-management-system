package com.rakesh.taskmanagement.service;

import com.rakesh.taskmanagement.entity.Task;
import com.rakesh.taskmanagement.entity.User;
import com.rakesh.taskmanagement.exception.ResourceNotFoundException;
import com.rakesh.taskmanagement.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserService userService;

    public Task createTask(Task task) {
        User currentUser = userService.getCurrentUser();
        task.setUser(currentUser);
        return taskRepository.save(task);
    }

    public List<Task> getAllTasks() {
        User currentUser = userService.getCurrentUser();
        return taskRepository.findByUserId(currentUser.getId());
    }

    public Task getTaskById(Long id) {
        User currentUser = userService.getCurrentUser();
        Task task = taskRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        if(task.getUser().getId() != currentUser.getId()) {
            throw new ResourceNotFoundException("Task not found");
        }

        return task;
    }

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

}
