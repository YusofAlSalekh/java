package com.ortecfinance.tasklist.service;

import com.ortecfinance.tasklist.model.Task;
import com.ortecfinance.tasklist.repository.TaskRepository;
import com.ortecfinance.tasklist.exceptions.ProjectNotFoundException;
import com.ortecfinance.tasklist.exceptions.TaskNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {
    @Mock
    TaskRepository taskRepository;

    @InjectMocks
    TaskService subject;

    @Test
    void addDeadline_sets_deadline_when_task_exists() {
        Task task = new Task(1, "Do thing", false);
        when(taskRepository.findTaskById(1)).thenReturn(Optional.of(task));

        LocalDate date = LocalDate.of(2025, 1, 15);
        subject.addDeadline(1, date);

        assertEquals(Optional.of(date), task.getDeadline());
        verify(taskRepository).findTaskById(1);
        verifyNoMoreInteractions(taskRepository);
    }

    @Test
    void addDeadline_throws_error_when_task_is_missing() {
        when(taskRepository.findTaskById(99)).thenReturn(Optional.empty());

        TaskNotFoundException ex = assertThrowsExactly(
                TaskNotFoundException.class,
                () -> subject.addDeadline(99, LocalDate.of(2025, 1, 15))
        );

        assertEquals("Could not find a task with an ID of 99.", ex.getMessage());
        verify(taskRepository).findTaskById(99);
        verifyNoMoreInteractions(taskRepository);
    }

    @Test
    void show_returns_projects_with_tasks_in_insertion_order() {
        when(taskRepository.projects()).thenReturn(List.of("secrets", "training"));

        List<Task> secrets = List.of(new Task(1, "A", false));
        List<Task> training = List.of(new Task(2, "B", false), new Task(3, "C", false));

        when(taskRepository.tasksInProject("secrets")).thenReturn(secrets);
        when(taskRepository.tasksInProject("training")).thenReturn(training);

        Map<String, List<Task>> result = subject.show();

        assertEquals(2, result.size());
        assertTrue(result.containsKey("secrets"));
        assertTrue(result.containsKey("training"));
        assertSame(secrets, result.get("secrets"));
        assertSame(training, result.get("training"));

        verify(taskRepository).projects();
        verify(taskRepository).tasksInProject("secrets");
        verify(taskRepository).tasksInProject("training");
        verifyNoMoreInteractions(taskRepository);
    }

    @Test
    void today_returns_only_tasks_by_given_date() {
        LocalDate date = LocalDate.of(2026, 1, 4);

        when(taskRepository.projects()).thenReturn(List.of("secrets", "training"));

        Task s1 = new Task(2, "S2", false);
        s1.setDeadline(date);
        Task s2 = new Task(1, "S1", false);
        s2.setDeadline(date);
        Task s3 = new Task(3, "S3", false);

        when(taskRepository.tasksInProject("secrets")).thenReturn(List.of(s1, s2, s3));

        Task t1 = new Task(10, "T1", false);
        t1.setDeadline(date.plusDays(1));
        when(taskRepository.tasksInProject("training")).thenReturn(List.of(t1));

        Map<String, List<Task>> result = subject.today(date);

        assertEquals(1, result.size());
        assertTrue(result.containsKey("secrets"));
        assertFalse(result.containsKey("training"));

        List<Task> due = result.get("secrets");
        assertEquals(2, due.size());
        assertEquals(1, due.get(0).getId());
        assertEquals(2, due.get(1).getId());

        verify(taskRepository).projects();
        verify(taskRepository).tasksInProject("secrets");
        verify(taskRepository).tasksInProject("training");
        verifyNoMoreInteractions(taskRepository);
    }

    @Test
    void addProject_delegates_to_repository() {
        subject.addProject("secrets");

        verify(taskRepository).addProject("secrets");
        verifyNoMoreInteractions(taskRepository);
    }

    @Test
    void addTask_throws_when_project_is_missing() {
        when(taskRepository.projectExists("missing")).thenReturn(false);

        ProjectNotFoundException ex = assertThrowsExactly(
                ProjectNotFoundException.class,
                () -> subject.addTask("missing", "Do it")
        );

        assertEquals("Could not find a project with the name \"missing\".", ex.getMessage());
        verify(taskRepository).projectExists("missing");
        verify(taskRepository, never()).addTask(anyString(), anyString());
        verifyNoMoreInteractions(taskRepository);
    }

    @Test
    void addTask_adds_task_when_project_exists() {
        when(taskRepository.projectExists("secrets")).thenReturn(true);

        Task created = new Task(1, "Eat donuts", false);
        when(taskRepository.addTask("secrets", "Eat donuts")).thenReturn(created);

        Task result = subject.addTask("secrets", "Eat donuts");

        assertSame(created, result);

        verify(taskRepository).projectExists("secrets");
        verify(taskRepository).addTask("secrets", "Eat donuts");
        verifyNoMoreInteractions(taskRepository);
    }

    @Test
    void check_sets_done_true_when_task_exists() {
        Task task = new Task(1, "X", false);
        when(taskRepository.findTaskById(1)).thenReturn(Optional.of(task));

        subject.check(1);

        assertTrue(task.isDone());
        verify(taskRepository).findTaskById(1);
        verifyNoMoreInteractions(taskRepository);
    }

    @Test
    void uncheck_sets_done_false_when_task_exists() {
        Task task = new Task(1, "X", true);
        when(taskRepository.findTaskById(1)).thenReturn(Optional.of(task));

        subject.uncheck(1);

        assertFalse(task.isDone());
        verify(taskRepository).findTaskById(1);
        verifyNoMoreInteractions(taskRepository);
    }

    @Test
    void check_throws_when_task_missing() {
        when(taskRepository.findTaskById(123)).thenReturn(Optional.empty());

        TaskNotFoundException ex = assertThrowsExactly(
                TaskNotFoundException.class,
                () -> subject.check(123)
        );

        assertEquals("Could not find a task with an ID of 123.", ex.getMessage());
        verify(taskRepository).findTaskById(123);
        verifyNoMoreInteractions(taskRepository);
    }

    @Test
    void viewByDeadline_groups_by_deadline_then_project_and_sorts_by_id() {
        when(taskRepository.projects()).thenReturn(List.of("Secrets", "Training"));

        Task a = new Task(2, "A", false);
        Task b = new Task(1, "B", false);
        a.setDeadline(LocalDate.of(2021, 11, 11));
        b.setDeadline(LocalDate.of(2021, 11, 11));

        Task c = new Task(3, "C", false);
        c.setDeadline(LocalDate.of(2021, 11, 13));

        Task d = new Task(4, "D", false);

        when(taskRepository.tasksInProject("Secrets")).thenReturn(List.of(b));
        when(taskRepository.tasksInProject("Training")).thenReturn(List.of(a, c, d));

        ViewByDeadlineResult result = subject.viewByDeadline();

        Map<String, List<Task>> on1111 = result.groupedByDeadline().get(LocalDate.of(2021, 11, 11));
        assertNotNull(on1111);
        assertEquals(List.of(b), on1111.get("Secrets"));
        assertEquals(List.of(a), on1111.get("Training"));

        Map<String, List<Task>> on1311 = result.groupedByDeadline().get(LocalDate.of(2021, 11, 13));
        assertNotNull(on1311);
        assertEquals(List.of(c), on1311.get("Training"));

        assertEquals(List.of(d), result.noDeadline().get("Training"));

        verify(taskRepository).projects();
        verify(taskRepository).tasksInProject("Secrets");
        verify(taskRepository).tasksInProject("Training");
        verifyNoMoreInteractions(taskRepository);
    }
}
