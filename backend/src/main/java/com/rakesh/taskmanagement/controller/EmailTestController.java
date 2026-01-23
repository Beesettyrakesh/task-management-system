package com.rakesh.taskmanagement.controller;

import com.rakesh.taskmanagement.entity.Task;
import com.rakesh.taskmanagement.entity.User;
import com.rakesh.taskmanagement.service.EmailService;
import com.rakesh.taskmanagement.service.TaskService;
import com.rakesh.taskmanagement.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class EmailTestController {

    private final EmailService emailService;
    private final UserService userService;
    private final TaskService taskService;

    @PostMapping("/remainder/{taskId}")
    public ResponseEntity<String> testReminderEmail(@PathVariable Long taskId) {
        try {
            // Get the task by ID
            Task task = taskService.getTaskById(taskId);

            // Get current authenticated user
            User user = userService.getCurrentUser();

            // Send reminder email
            emailService.sendTaskRemainderEmail(user, task);

            return ResponseEntity.ok("✅ Reminder email sent for task: " + task.getTitle());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("❌ Error: " + e.getMessage());
        }
    }

}
