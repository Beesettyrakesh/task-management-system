package com.rakesh.taskmanagement.dto;

import com.rakesh.taskmanagement.entity.Priority;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskStatisticsDto {

    private Long totalTasks;
    private Long completedTasks;
    private Long inProgressTasks;
    private Long todoTasks;
    private Long overdueTasks;
    private Map<Priority, Long> tasksByPriority;
}
