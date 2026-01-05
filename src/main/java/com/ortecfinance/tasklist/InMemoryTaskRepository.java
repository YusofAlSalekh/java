package com.ortecfinance.tasklist;

import com.ortecfinance.tasklist.exceptions.ProjectNotFoundException;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public final class InMemoryTaskRepository implements TaskRepository {
    private final Map<String, List<Task>> tasks = new LinkedHashMap<>();
    private long lastId = 0;

    @Override
    public void addProject(String name) {
        tasks.put(name, new ArrayList<Task>());
    }

    @Override
    public boolean projectExists(String name) {
        return tasks.containsKey(name);
    }

    @Override
    public Task addTask(String project, String description) {
        List<Task> projectTasks = tasks.get(project);
        if (projectTasks == null) {
            throw new ProjectNotFoundException("Could not find a project with the name: " + project);
        }
        Task task = new Task(nextId(), description, false);
        projectTasks.add(task);
        return task;
    }

    @Override
    public Optional<Task> findTaskById(long id) {
        for (Map.Entry<String, List<Task>> entry : tasks.entrySet()) {
            for (Task task : entry.getValue()) {
                if (task.getId() == id) {
                    return Optional.of(task);
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public Collection<String> projects() {
        return tasks.keySet();
    }

    @Override
    public List<Task> tasksInProject(String project) {
        return tasks.getOrDefault(project, List.of());
    }

    private long nextId() {
        return ++lastId;
    }
}
