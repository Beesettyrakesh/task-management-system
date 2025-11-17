package com.rakesh.taskmanagement.service;

import com.rakesh.taskmanagement.entity.Task;
import com.rakesh.taskmanagement.exception.ResourceNotFoundException;
import com.rakesh.taskmanagement.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public Task createTask(Task task) {
        return taskRepository.save(task);
    }

    public List<Task> getAllTasks() {
       List<Task> allTasks = taskRepository.findAll();
       return allTasks;
    }

    public Task getTaskById(Long id) {
        return taskRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
    }

    public Task updateTask(Long id, Task task) {
        if(taskRepository.findById(id).isPresent()) {
            task.setId(id);
            return taskRepository.save(task);
        } else  {
            throw new ResourceNotFoundException("Task not found");
        }
    }

    public void deleteTask(Long id) {
        if(!taskRepository.existsById(id)) {
            throw new ResourceNotFoundException("Task not found");
        }
        taskRepository.deleteById(id);
    }

}
