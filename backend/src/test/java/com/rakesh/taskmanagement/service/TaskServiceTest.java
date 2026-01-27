package com.rakesh.taskmanagement.service;

import com.rakesh.taskmanagement.dto.TaskRequestDto;
import com.rakesh.taskmanagement.entity.Priority;
import com.rakesh.taskmanagement.entity.Task;
import com.rakesh.taskmanagement.entity.TaskStatus;
import com.rakesh.taskmanagement.entity.User;
import com.rakesh.taskmanagement.exception.ResourceNotFoundException;
import com.rakesh.taskmanagement.repository.TagRepository;
import com.rakesh.taskmanagement.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserService userService;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private TaskService taskService;

    private User testUser;
    private Task testTask;
    private TaskRequestDto testTaskDto;

    @BeforeEach
    void setup() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testUser");
        testUser.setEmail("test@example.com");

        testTask = new Task();
        testTask.setId(1L);
        testTask.setTitle("Test Task");
        testTask.setDescription("Test Description");
        testTask.setStatus(TaskStatus.TODO);
        testTask.setPriority(Priority.MEDIUM);
        testTask.setUser(testUser);

        testTaskDto = new TaskRequestDto();
        testTaskDto.setTitle("Test Task");
        testTaskDto.setDescription("Test Description");
        testTaskDto.setPriority(Priority.MEDIUM);
        testTaskDto.setStatus(TaskStatus.TODO);
    }

    @Test
    void testCreateTask_Success() {
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);
        doNothing().when(emailService).sendTaskCreatedEmail(any(User.class), any(Task.class));

        Task result = taskService.createTask(testTaskDto);

        assertNotNull(result);
        assertEquals("Test Task", result.getTitle());
        assertEquals(testUser, result.getUser());

        verify(userService, times(1)).getCurrentUser();
        verify(taskRepository, times(1)).save(any(Task.class));
        verify(emailService, times(1)).sendTaskCreatedEmail(testUser, testTask);
    }

    @Test
    void testCreateTask_PastDueDate_ThrowsException() {
        testTaskDto.setDueDate(LocalDate.now().minusDays(1));
        when(userService.getCurrentUser()).thenReturn(testUser);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> taskService.createTask(testTaskDto)
        );

        assertEquals("Due date cannot be in the past", exception.getMessage());

        verify(taskRepository, never()).save(any(Task.class));
        verify(userService, times(1)).getCurrentUser();
    }

    @Test
    void testCreateTask_NullDueDate_Success() {
        testTaskDto.setDueDate(null);
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        Task result = taskService.createTask(testTaskDto);

        assertNotNull(result);
        assertNull(result.getDueDate());

        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    void testGetTaskById_WrongUser_ThrowsException() {
        when(taskRepository.findByIdAndUserIdWithTags(1L, testUser.getId()))
                .thenReturn(Optional.empty());
        when(userService.getCurrentUser()).thenReturn(testUser);

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> taskService.getTaskById(1L)
        );

        assertEquals("Task not found", exception.getMessage());

        verify(taskRepository, times(1)).findByIdAndUserIdWithTags(1L, testUser.getId());
        verify(userService, times(1)).getCurrentUser();
    }

    @Test
    void testGetTaskById_Success() {
        when(taskRepository.findByIdAndUserIdWithTags(1L, testUser.getId())).thenReturn(Optional.of(testTask));
        when(userService.getCurrentUser()).thenReturn(testUser);

        Task result = taskService.getTaskById(1L);

        assertNotNull(result);
        assertEquals("Test Task", result.getTitle());
        assertEquals(testUser, result.getUser());

        verify(taskRepository, times(1)).findByIdAndUserIdWithTags(1L,  testUser.getId());
        verify(userService, times(1)).getCurrentUser();
    }

    @Test
    void testUpdateTask_TaskNotFound_ThrowsException() {
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(taskRepository.findById(143L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> taskService.updateTask(143L, testTaskDto)
        );
        assertEquals("Task not found", exception.getMessage());

        verify(taskRepository, times(1)).findById(143L);
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void testDeleteTask_TaskNotFound_ThrowsException() {
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(taskRepository.findById(666L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> taskService.deleteTask(666L)
        );

        assertEquals("Task not found", exception.getMessage());

        verify(taskRepository, times(1)).findById(666L);
        verify(taskRepository, never()).delete(any(Task.class));
    }

    @Test
    void testGetFilteredTasks_ByStatus_ReturnsFilteredTasks() {
        List<Task> todoTasks = Arrays.asList(testTask);

        when(userService.getCurrentUser()).thenReturn(testUser);
        when(taskRepository.findByUserIdAndStatus(testUser.getId(), TaskStatus.TODO))
                .thenReturn(todoTasks);

        List<Task> result = taskService.getFilteredTasks(
                TaskStatus.TODO,
                null,
                null,
                null
        );

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(TaskStatus.TODO, result.get(0).getStatus());

        verify(taskRepository, times(1)).findByUserIdAndStatus(testTask.getId(), TaskStatus.TODO);
    }

    @Test
    void testGetFilteredTasks_ByPriority_ReturnsFilteredTasks() {
        List<Task> todoTasks = Arrays.asList(testTask);

        when(userService.getCurrentUser()).thenReturn(testUser);
        when(taskRepository.findByUserIdAndPriority(testUser.getId(), Priority.HIGH))
                .thenReturn(todoTasks);

        List<Task> result = taskService.getFilteredTasks(
                null,
                Priority.HIGH,
                null,
                null
        );

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(Priority.MEDIUM, result.get(0).getPriority());

        verify(taskRepository, times(1)).findByUserIdAndPriority(testUser.getId(), Priority.MEDIUM);
    }

    @Test
    void testGetFilteredTasks_SortByDueDate_ReturnsSortedTasks() {
        List<Task> sortedTasks = Arrays.asList(testTask);

        when(userService.getCurrentUser()).thenReturn(testUser);
        when(taskRepository.findByUserIdOrderByDueDateAsc(testUser.getId()))
                .thenReturn(sortedTasks);

        List<Task> result = taskService.getFilteredTasks(
                null,
                null,
                "dueDate",
                "asc"
        );

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(taskRepository, times(1)).findByUserIdOrderByDueDateAsc(testUser.getId());
    }

    @Test
    void testGetFilteredTasks_StatusAndSort_ReturnsFilteredAndSorted() {
        List<Task> filteredAndSorted = Arrays.asList(testTask);

        when(userService.getCurrentUser()).thenReturn(testUser);
        when(taskRepository.findByUserIdAndStatusOrderByDueDateAsc(
                testUser.getId(), TaskStatus.TODO))
                .thenReturn(filteredAndSorted);

        List<Task> result = taskService.getFilteredTasks(
                TaskStatus.TODO,
                null,
                "dueDate",
                "asc"
        );

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(TaskStatus.TODO, result.get(0).getStatus());

        verify(taskRepository, times(1))
                .findByUserIdAndStatusOrderByDueDateAsc(testUser.getId(), TaskStatus.TODO);
    }
}
