package com.rakesh.taskmanagement.service;

import com.rakesh.taskmanagement.entity.Task;
import com.rakesh.taskmanagement.entity.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.email.from}")
    private String fromEmail;

    public void sendTaskCreatedEmail(User user, Task task) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(user.getEmail());
            helper.setSubject("Task Created:" + task.getTitle());

            String htmlContent = buildTaskCreatedEmailTemplate(user, task);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Task created email sent to: {}", user.getEmail());
        } catch (MessagingException ex) {
            log.error("Failed to send task created email to: {}", user.getEmail(), ex);
        }
    }

    private String buildTaskCreatedEmailTemplate(User user, Task task) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        String formattedDueDate = task.getDueDate() != null ?
                task.getDueDate().format(formatter) : "No due date";

        return """
                <!DOCTYPE html>
                        <html>
                        <head>
                            <meta charset="UTF-8">
                            <style>
                                body { font-family: Arial, sans-serif; margin: 0; padding: 20px; background-color: #f5f5f5; }
                                .container { max-width: 600px; margin: 0 auto; background-color: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
                                .header { background-color: #4f46e5; color: white; padding: 20px; border-radius: 8px; text-align: center; margin-bottom: 20px; }
                                .task-details { background-color: #f8fafc; padding: 20px; border-radius: 8px; margin: 20px 0; }
                                .priority { padding: 4px 12px; border-radius: 20px; font-size: 12px; font-weight: bold; }
                                .priority-HIGH { background-color: #fecaca; color: #dc2626; }
                                .priority-MEDIUM { background-color: #fed7aa; color: #ea580c; }
                                .priority-LOW { background-color: #d1fae5; color: #059669; }
                                .footer { text-align: center; color: #6b7280; font-size: 14px; margin-top: 30px; }
                            </style>
                        </head>
                        <body>
                            <div class="container">
                                <div class="header">
                                    <h1>New Task Created!</h1>
                                </div>
                
                                <p>Hi <strong>%s</strong>,</p>
                                <p>You've successfully created a new task in your Task Management System.</p>
                
                                <div class="task-details">
                                    <h3>Task Details</h3>
                                    <p><strong>Title:</strong> %s</p>
                                    <p><strong>Description:</strong> %s</p>
                                    <p><strong>Due Date:</strong> %s</p>
                                    <p><strong>Priority:</strong> <span class="priority priority-%s">%s</span></p>
                                    <p><strong>Status:</strong> %s</p>
                                </div>
                                <div class="footer">
                                    <p>This email was sent from your Task Management System</p>
                                    <p>© 2026 Task Management App</p>
                                </div>
                            </div>
                        </body>
                        </html>
                """.formatted(
                        user.getUsername(),
                        task.getTitle(),
                        task.getDescription() != null ? task.getDescription() : "No description",
                        formattedDueDate,
                        task.getPriority().toString(),
                        task.getPriority().toString(),
                        task.getStatus().toString()
                    );
    }

    public void sendTaskRemainderEmail(User user,  Task task) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(user.getEmail());

            String dueDateText = calculateDueDateText(task.getDueDate());
            helper.setSubject("Task Reminder: " + task.getTitle() + " due " + dueDateText);

            String htmlContent = buildTaskReminderEmailTemplate(user, task);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Task reminder email sent to: {}", user.getEmail());
        } catch (MessagingException e) {
            log.error("Failed to send task reminder email to: {}", user.getEmail(), e);
        }
    }

    private String calculateDueDateText(LocalDate dueDate) {
        if (dueDate == null) return "with no due date";

        long daysUntilDue = ChronoUnit.DAYS.between(LocalDate.now(), dueDate);

        if (daysUntilDue == 0) return "due TODAY";
        else if (daysUntilDue == 1) return "due in 1 day";
        else if (daysUntilDue > 1) return "due in " + daysUntilDue + " days";
        else if (daysUntilDue == -1) return "was due 1 day ago";
        else return "was due " + Math.abs(daysUntilDue) + " days ago";
    }


    private String buildTaskReminderEmailTemplate(User user, Task task) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        String formattedDueDate = task.getDueDate() != null ?
                task.getDueDate().format(formatter) : "No due date";

        long daysUntilDue = task.getDueDate() != null ?
                ChronoUnit.DAYS.between(LocalDate.now(), task.getDueDate()) : 999;

        String urgencyClass = daysUntilDue <= 0 ? "urgent-overdue" :
                daysUntilDue <= 1 ? "urgent-today" : "urgent-soon";

        String urgencyText = daysUntilDue < 0 ? "OVERDUE!" :
                daysUntilDue == 0 ? "DUE TODAY!" :
                        daysUntilDue == 1 ? "Due Tomorrow" :
                                "Due in " + daysUntilDue + " days";

        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <style>
                body { font-family: Arial, sans-serif; margin: 0; padding: 20px; background-color: #f5f5f5; }
                .container { max-width: 600px; margin: 0 auto; background-color: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
                .header { color: white; padding: 20px; border-radius: 8px; text-align: center; margin-bottom: 20px; }
                .urgent-overdue { background-color: #dc2626; }
                .urgent-today { background-color: #f59e0b; }
                .urgent-soon { background-color: #4f46e5; }
                .task-details { background-color: #fef3c7; padding: 20px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #f59e0b; }
                .priority { padding: 4px 12px; border-radius: 20px; font-size: 12px; font-weight: bold; }
                .priority-HIGH { background-color: #fecaca; color: #dc2626; }
                .priority-MEDIUM { background-color: #fed7aa; color: #ea580c; }
                .priority-LOW { background-color: #d1fae5; color: #059669; }
                .cta-button { display: inline-block; background-color: #1f2937; color: white; padding: 12px 24px; text-decoration: none; border-radius: 6px; margin-top: 20px; font-weight: bold; }
                .footer { text-align: center; color: #6b7280; font-size: 14px; margin-top: 30px; }
                .due-date-highlight { font-weight: bold; font-size: 16px; padding: 8px 16px; border-radius: 6px; background-color: %s; color: black; display: inline-block; }
            </style>
        </head>
        <body>
            <div class="container">
                <div class="header %s">
                    <h1>Task Reminder</h1>
                    <p style="margin:0; font-size: 18px; font-weight: bold;">%s</p>
                </div>
        
                <p>Hi <strong>%s</strong>,</p>
                <p>This is a friendly reminder about your task:</p>
        
                <div class="task-details">
                    <h3>Task Details</h3>
                    <p><strong>Title:</strong> %s</p>
                    <p><strong>Description:</strong> %s</p>
                    <p><strong>Due Date:</strong> <span class="due-date-highlight">%s</span></p>
                    <p><strong>Priority:</strong> <span class="priority priority-%s">%s</span></p>
                    <p><strong>Status:</strong> %s</p>
                </div>
        
                <p>⚡ Don't let this task slip by! Take action now to stay on track.</p>
        
                <a href="#" class="cta-button">Open Task Management App</a>
        
                <div class="footer">
                    <p>This is an automatic reminder from your Task Management System</p>
                    <p>© 2026 Task Management App</p>
                </div>
            </div>
        </body>
        </html>
        """.formatted(
                urgencyClass,
                urgencyClass,
                urgencyText,
                user.getUsername(),
                task.getTitle(),
                task.getDescription() != null ? task.getDescription() : "No description",
                formattedDueDate,
                task.getPriority().toString(),
                task.getPriority().toString(),
                task.getStatus().toString()
        );

    }
}
