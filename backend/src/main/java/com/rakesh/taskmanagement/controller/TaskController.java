package com.rakesh.taskmanagement.controller;

import java.util.List;
import java.util.stream.Collectors;

import com.rakesh.taskmanagement.dto.TaskRequestDto;
import com.rakesh.taskmanagement.dto.TaskStatisticsDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rakesh.taskmanagement.dto.TaskResponseDto;
import com.rakesh.taskmanagement.entity.Priority;
import com.rakesh.taskmanagement.entity.Task;
import com.rakesh.taskmanagement.entity.TaskStatus;
import com.rakesh.taskmanagement.exception.InvalidParameterException;
import com.rakesh.taskmanagement.service.TaskService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<TaskResponseDto> createTask(@Valid @RequestBody TaskRequestDto taskRequestDto) {
        Task createdTask = taskService.createTask(taskRequestDto);
        TaskResponseDto responseDto = TaskResponseDto.from(createdTask);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @GetMapping
    public ResponseEntity<List<TaskResponseDto>> getAllTasks(    
        @RequestParam(required = false) String status,
        @RequestParam(required = false) String priority,
        @RequestParam(required = false) String sortBy,
        @RequestParam(required = false) String sortDirection
    ) {
        List<Task> tasks;

        if(status != null || priority != null || sortBy != null || sortDirection != null) {
            try {
                TaskStatus taskStatus = status != null ? TaskStatus.valueOf(status.toUpperCase()) : null;
                Priority taskPriority = priority != null ? Priority.valueOf(priority.toUpperCase()) : null;
                tasks = taskService.getFilteredTasks(taskStatus, taskPriority, sortBy, sortDirection);
            } catch (IllegalArgumentException e) {
                throw new InvalidParameterException("Invalid status or priority value: " + e.getMessage());
            }
        } else {
            tasks = taskService.getAllTasks();
        }

        List<TaskResponseDto> responseDtos = tasks.stream()
            .map(TaskResponseDto::from)
            .collect(Collectors.toList());

        return ResponseEntity.ok(responseDtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponseDto> getTaskById(@PathVariable Long id) {
        Task task = taskService.getTaskById(id);
        TaskResponseDto responseDto = TaskResponseDto.from(task);
        return ResponseEntity.ok(responseDto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskResponseDto> updateTask(@PathVariable Long id, @Valid @RequestBody TaskRequestDto taskRequestDto) {
        Task updatedTask = taskService.updateTask(id, taskRequestDto);
        TaskResponseDto responseDto = TaskResponseDto.from(updatedTask);
        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<TaskResponseDto>> getTasksByStatus(@PathVariable TaskStatus status) {
        List<Task> tasks = taskService.getTasksByStatus(status);
        List<TaskResponseDto> responseDtos = tasks.stream()
            .map(TaskResponseDto::from)
            .collect(Collectors.toList());
        return ResponseEntity.ok(responseDtos);
    }

    @GetMapping("/priority/{priority}")
    public ResponseEntity<List<TaskResponseDto>> getTasksByPriority(@PathVariable Priority priority) {
        List<Task> tasks = taskService.getTasksByPriority(priority);
        List<TaskResponseDto> responseDtos = tasks.stream()
            .map(TaskResponseDto::from)
            .collect(Collectors.toList());
        return ResponseEntity.ok(responseDtos);
    }

    @PostMapping("/{taskId}/tags/{tagId}")
    public ResponseEntity<Void> assignTagToTask(@PathVariable Long taskId, @PathVariable Long tagId) {
        taskService.assignTagToTask(taskId, tagId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{taskId}/tags/{tagId}")
    public ResponseEntity<Void> removeTagFromTask(@PathVariable Long taskId, @PathVariable Long tagId) {
        taskService.removeTagFromTask(taskId, tagId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/statistics")
    public ResponseEntity<TaskStatisticsDto> getTaskStatistics() {
        TaskStatisticsDto taskStatisticsDto = taskService.getStatistics();
        return ResponseEntity.ok(taskStatisticsDto);
    }

    @GetMapping("/recent")
    public ResponseEntity<List<TaskResponseDto>> getRecentTasks() {
        List<Task> recentTasks = taskService.getRecentTasks();
        List<TaskResponseDto> response = recentTasks.stream()
                .map(TaskResponseDto::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
}
