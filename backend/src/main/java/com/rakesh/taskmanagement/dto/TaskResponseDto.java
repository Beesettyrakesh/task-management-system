package com.rakesh.taskmanagement.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.rakesh.taskmanagement.entity.Priority;
import com.rakesh.taskmanagement.entity.Tag;
import com.rakesh.taskmanagement.entity.Task;
import com.rakesh.taskmanagement.entity.TaskStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponseDto {
    private Long id;
    private String title;
    private String description;
    private TaskStatus status;
    private Priority priority;
    private LocalDate dueDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<TagResponseDto> tags;

    public static TaskResponseDto from(Task task) {

        Set<Tag> tagsCopy = new HashSet<>(task.getTags());

        List<TagResponseDto> tagDtos = tagsCopy.stream()
            .map(TagResponseDto::from)
            .collect(Collectors.toList());

        return new TaskResponseDto(
            task.getId(),
            task.getTitle(),
            task.getDescription(),
            task.getStatus(),
            task.getPriority(),
            task.getDueDate(),
            task.getCreatedAt(),
            task.getUpdatedAt(),
            tagDtos
        );
    }

}
