package com.ortecfinance.tasklist.repository;

import com.ortecfinance.tasklist.model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskRepositoryTest {
    private InMemoryTaskRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryTaskRepository();
    }

    @Test
    void addProject_creates_empty_project() {
        repository.addProject("secrets");

        assertTrue(repository.projectExists("secrets"));
        assertTrue(repository.tasksInProject("secrets").isEmpty());
    }

    @Test
    void addTask_adds_task_with_incrementing_id() {
        repository.addProject("secrets");

        Task t1 = repository.addTask("secrets", "Eat donuts");
        Task t2 = repository.addTask("secrets", "Destroy humans");

        assertEquals(1, t1.getId());
        assertEquals(2, t2.getId());
        assertEquals("Eat donuts", t1.getDescription());
        assertEquals("Destroy humans", t2.getDescription());
    }

    @Test
    void findTaskById_returns_task_if_exists() {
        repository.addProject("secrets");
        Task task = repository.addTask("secrets", "A");

        Optional<Task> result = repository.findTaskById(task.getId());

        assertTrue(result.isPresent());
        assertSame(task, result.get());
    }

    @Test
    void findTaskById_returns_empty_if_task_not_found() {
        repository.addProject("secrets");

        Optional<Task> result = repository.findTaskById(99);

        assertTrue(result.isEmpty());
    }

    @Test
    void projects_returns_projects() {
        repository.addProject("secrets");
        repository.addProject("training");

        List<String> projects = new ArrayList<>(repository.projects());

        assertEquals(List.of("secrets", "training"), projects);
    }
}
