package com.rakesh.taskmanagement.service;

import com.rakesh.taskmanagement.entity.Task;
import com.rakesh.taskmanagement.entity.User;
import com.rakesh.taskmanagement.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduledTaskService {

    private final TaskRepository taskRepository;
    private final EmailService emailService;

    @Transactional
    @Scheduled(cron = "0 0 9 * * ?")
    public void sendTaskReminders() {
        log.info("Starting scheduled task reminders at {}", LocalDate.now());
        try {
            LocalDate today = LocalDate.now();
            LocalDate tomorrow = today.plusDays(1);

            List<Task> overdueTasks = taskRepository.findByDueDateBefore(today);
            List<Task> tasksDueToday = taskRepository.findByDueDate(today);
            List<Task> tasksDueTomorrow = taskRepository.findByDueDate(tomorrow);

            int remindersSent = 0;

            for (Task task : overdueTasks) {
                User taskOwner = task.getUser();
                emailService.sendTaskReminderEmail(taskOwner, task);
                remindersSent++;
                log.info("Sent OVERDUE reminder to {} for: '{}'", taskOwner.getEmail(), task.getTitle());
            }

            for (Task task : tasksDueToday) {
                User taskOwner = task.getUser();
                emailService.sendTaskReminderEmail(taskOwner, task);
                remindersSent++;
                log.info("Sent TODAY reminder to {} for: '{}'", taskOwner.getEmail(), task.getTitle());
            }

            for (Task task : tasksDueTomorrow) {
                User taskOwner = task.getUser();
                emailService.sendTaskReminderEmail(taskOwner, task);
                remindersSent++;
                log.info("Sent TOMORROW reminder to {} for: '{}'", taskOwner.getEmail(), task.getTitle());
            }

            log.info("Task reminders completed successfully!");
            log.info("Summary: {} total reminders sent | {} tasks due today | {} tasks due tomorrow",
                    remindersSent, tasksDueToday.size(), tasksDueTomorrow.size());
        } catch (Exception e) {
            log.error("Critical error during scheduled task reminders: {}", e.getMessage(), e);
        }
    }
}
