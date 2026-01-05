package com.ortecfinance.tasklist.repository;

import com.ortecfinance.tasklist.model.Task;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TaskRepository {
    void addProject(String name);

    boolean projectExists(String name);

    Task addTask(String project, String description);

    Optional<Task> findTaskById(long id);

    Collection<String> projects();

    List<Task> tasksInProject(String project);
}
