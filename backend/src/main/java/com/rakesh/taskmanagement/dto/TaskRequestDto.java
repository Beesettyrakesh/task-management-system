package com.rakesh.taskmanagement.dto;

import com.rakesh.taskmanagement.entity.Priority;
import com.rakesh.taskmanagement.entity.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskRequestDto {

    @NotBlank(message = "Task title is required")
    @Size(min = 1, max = 100, message = "Task title must be between 1 to 100 characters")
    private String title;

    @Size(max = 1000, message = "Task description cannot exceed 1000 characters")
    private String description;

    @NotNull(message = "Task status is required")
    private TaskStatus status;

    @NotNull(message = "Task priority is required")
    private Priority priority;

    @NotNull(message = "Due date is required")
    private LocalDate dueDate;

    private Set<Long> tagIds;
}
