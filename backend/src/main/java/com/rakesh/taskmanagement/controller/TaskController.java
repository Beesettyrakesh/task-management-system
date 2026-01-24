package com.rakesh.taskmanagement.controller;

import java.util.List;
import java.util.stream.Collectors;

import com.rakesh.taskmanagement.dto.ErrorResponseDto;
import com.rakesh.taskmanagement.dto.TaskRequestDto;
import com.rakesh.taskmanagement.dto.TaskStatisticsDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import com.rakesh.taskmanagement.service.TaskService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Tag(name = "Task Management", description = "Complete task CRUD operations with filtering, sorting, and tag management")
public class TaskController {

    private final TaskService taskService;

    @Operation(
            summary = "Create a new task",
            description = "Creates a task for the authenticated user with title, description, due date, and priority"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Task created successfully",
                    content = @Content(schema = @Schema(implementation = TaskResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid task data or validation errors",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "JWT token required")
    })
    @PostMapping
    @SecurityRequirement(name = "JWT")
    public ResponseEntity<TaskResponseDto> createTask(@Valid @RequestBody TaskRequestDto taskRequestDto) {
        Task createdTask = taskService.createTask(taskRequestDto);
        TaskResponseDto responseDto = TaskResponseDto.from(createdTask);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @Operation(
            summary = "Get all tasks with optional filtering and sorting",
            description = "Retrieve user's tasks with flexible filtering and sorting options.\n\n" +
                         "**All parameters are optional:**\n" +
                         "• No parameters → Returns all user's tasks\n" +
                         "• Single filter → ?status=TODO (only TODO tasks)\n" +
                         "• Multiple filters → ?status=TODO&priority=HIGH&sortBy=dueDate&sortDirection=asc\n\n" +
                         "**Available Filter Options:**\n" +
                         "• Filter by status: TODO, IN_PROGRESS, DONE\n" +
                         "• Filter by priority: LOW, MEDIUM, HIGH\n" +
                         "• Sort by: dueDate, priority, createdAt\n" +
                         "• Sort direction: asc (ascending), desc (descending)\n\n" +
                         "**Examples:**\n" +
                         "• `/api/tasks` → All tasks\n" +
                         "• `/api/tasks?status=TODO` → Only TODO tasks\n" +
                         "• `/api/tasks?priority=HIGH&sortBy=dueDate` → High priority tasks sorted by due date"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tasks retrieved successfully",
                    content = @Content(schema = @Schema(implementation = TaskResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid filter values",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "JWT token required")
    })
    @GetMapping
    public ResponseEntity<List<TaskResponseDto>> getAllTasks(
        @Parameter(description = "Filter tasks by status", example = "TODO",
                  schema = @Schema(allowableValues = {"TODO", "IN_PROGRESS", "DONE"}))
        @RequestParam(required = false) String status,
        
        @Parameter(description = "Filter tasks by priority level", example = "HIGH",
                  schema = @Schema(allowableValues = {"LOW", "MEDIUM", "HIGH"}))
        @RequestParam(required = false) String priority,
        
        @Parameter(description = "Sort tasks by field", example = "dueDate",
                  schema = @Schema(allowableValues = {"dueDate", "priority", "createdAt"}))
        @RequestParam(required = false) String sortBy,
        
        @Parameter(description = "Sort direction", example = "asc",
                  schema = @Schema(allowableValues = {"asc", "desc"}))
        @RequestParam(required = false) String sortDirection
    ) {
        List<Task> tasks;

        if(status != null || priority != null || sortBy != null || sortDirection != null) {
            try {
                TaskStatus taskStatus = status != null ? TaskStatus.valueOf(status.toUpperCase()) : null;
                Priority taskPriority = priority != null ? Priority.valueOf(priority.toUpperCase()) : null;
                tasks = taskService.getFilteredTasks(taskStatus, taskPriority, sortBy, sortDirection);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid status or priority value: " + e.getMessage());
            }
        } else {
            tasks = taskService.getAllTasks();
        }

        List<TaskResponseDto> responseDtos = tasks.stream()
            .map(TaskResponseDto::from)
            .collect(Collectors.toList());

        return ResponseEntity.ok(responseDtos);
    }

    @Operation(
            summary = "Get task by ID",
            description = "Retrieve a specific task by its ID. User can only access their own tasks."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task found and returned",
                    content = @Content(schema = @Schema(implementation = TaskResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Task not found or not owned by user",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "JWT token required")
    })
    @GetMapping("/{id}")
    public ResponseEntity<TaskResponseDto> getTaskById(@PathVariable Long id) {
        Task task = taskService.getTaskById(id);
        TaskResponseDto responseDto = TaskResponseDto.from(task);
        return ResponseEntity.ok(responseDto);
    }

    @Operation(
            summary = "Update an existing task",
            description = "Update task details including title, description, due date, priority, and status"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task updated successfully",
                    content = @Content(schema = @Schema(implementation = TaskResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid task data",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Task not found or not owned by user",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "JWT token required")
    })
    @PutMapping("/{id}")
    public ResponseEntity<TaskResponseDto> updateTask(@PathVariable Long id, @Valid @RequestBody TaskRequestDto taskRequestDto) {
        Task updatedTask = taskService.updateTask(id, taskRequestDto);
        TaskResponseDto responseDto = TaskResponseDto.from(updatedTask);
        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }

    @Operation(
            summary = "Delete a task",
            description = "Permanently delete a task. User can only delete their own tasks."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Task deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Task not found or not owned by user",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "JWT token required")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Assign tag to task",
            description = "Associate an existing tag with a task. Both tag and task must belong to the authenticated user."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Tag assigned successfully"),
            @ApiResponse(responseCode = "404", description = "Task or tag not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "JWT token required")
    })
    @PostMapping("/{taskId}/tags/{tagId}")
    public ResponseEntity<Void> assignTagToTask(@PathVariable Long taskId, @PathVariable Long tagId) {
        taskService.assignTagToTask(taskId, tagId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Remove tag from task",
            description = "Remove tag association from a task"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Tag removed successfully"),
            @ApiResponse(responseCode = "404", description = "Task or tag not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "JWT token required")
    })
    @DeleteMapping("/{taskId}/tags/{tagId}")
    public ResponseEntity<Void> removeTagFromTask(@PathVariable Long taskId, @PathVariable Long tagId) {
        taskService.removeTagFromTask(taskId, tagId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Get tasks by status",
            description = "Retrieve all tasks with a specific status (TODO, IN_PROGRESS, DONE)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tasks with specified status retrieved",
                    content = @Content(schema = @Schema(implementation = TaskResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "JWT token required")
    })
    @GetMapping("/status/{status}")
    public ResponseEntity<List<TaskResponseDto>> getTasksByStatus(
            @Parameter(description = "Task status to filter by", example = "TODO")
            @PathVariable TaskStatus status) {
        List<Task> tasks = taskService.getTasksByStatus(status);
        List<TaskResponseDto> responseDtos = tasks.stream()
                .map(TaskResponseDto::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseDtos);
    }

    @Operation(
            summary = "Get tasks by priority",
            description = "Retrieve all tasks with a specific priority level (LOW, MEDIUM, HIGH)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tasks with specified priority retrieved",
                    content = @Content(schema = @Schema(implementation = TaskResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "JWT token required")
    })
    @GetMapping("/priority/{priority}")
    public ResponseEntity<List<TaskResponseDto>> getTasksByPriority(
            @Parameter(description = "Task priority to filter by", example = "HIGH")
            @PathVariable Priority priority) {
        List<Task> tasks = taskService.getTasksByPriority(priority);
        List<TaskResponseDto> responseDtos = tasks.stream()
                .map(TaskResponseDto::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseDtos);
    }

    @Operation(
            summary = "Get task statistics",
            description = "Retrieve user's task statistics including counts by status and priority"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task statistics retrieved successfully",
                    content = @Content(schema = @Schema(implementation = TaskStatisticsDto.class))),
            @ApiResponse(responseCode = "401", description = "JWT token required")
    })
    @GetMapping("/statistics")
    public ResponseEntity<TaskStatisticsDto> getTaskStatistics() {
        TaskStatisticsDto taskStatisticsDto = taskService.getStatistics();
        return ResponseEntity.ok(taskStatisticsDto);
    }

    @Operation(
            summary = "Get recent tasks",
            description = "Retrieve the most recently created tasks for the authenticated user"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Recent tasks retrieved successfully",
                    content = @Content(schema = @Schema(implementation = TaskResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "JWT token required")
    })
    @GetMapping("/recent")
    public ResponseEntity<List<TaskResponseDto>> getRecentTasks() {
        List<Task> recentTasks = taskService.getRecentTasks();
        List<TaskResponseDto> response = recentTasks.stream()
                .map(TaskResponseDto::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
}
