package com.ortecfinance.tasklist;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;

    public void deadline(long id, LocalDate date) {
        Task task = taskRepository.findTaskById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Could not find a task with an ID of " + id + "."
                ));
        task.setDeadline(date);
    }

    public Map<String, List<Task>> show() {
        Map<String, List<Task>> result = new LinkedHashMap<>();

        for (String project : taskRepository.projects()) {
            result.put(project, taskRepository.tasksInProject(project));
        }

        return result;
    }

    public Map<String, List<Task>> today(LocalDate date) {
        Map<String, List<Task>> result = new LinkedHashMap<>();

        for (String project : taskRepository.projects()) {
            List<Task> dueToday = new ArrayList<>();

            for (Task task : taskRepository.tasksInProject(project)) {
                if (task.getDeadline().isPresent()
                    && task.getDeadline().get().equals(date)) {
                    dueToday.add(task);
                }
            }

            if (!dueToday.isEmpty()) {
                dueToday.sort(Comparator.comparingLong(Task::getId));
                result.put(project, dueToday);
            }
        }

        return result;
    }

    public ViewByDeadlineResult viewByDeadline() {
        Map<LocalDate, Map<String, List<Task>>> groupedByDeadline = new TreeMap<>();
        Map<String, List<Task>> noDeadline = new LinkedHashMap<>();

        groupTasksByDeadlineAndProject(noDeadline, groupedByDeadline);
        sortTasksById(groupedByDeadline, noDeadline);

        return new ViewByDeadlineResult(groupedByDeadline, noDeadline);
    }

    private void groupTasksByDeadlineAndProject(
            Map<String, List<Task>> noDeadline,
            Map<LocalDate, Map<String, List<Task>>> groupedByDeadline
    ) {
        for (String projectName : taskRepository.projects()) {
            for (Task task : taskRepository.tasksInProject(projectName)) {
                Optional<LocalDate> deadlineOptional = task.getDeadline();

                if (deadlineOptional.isEmpty()) {
                    noDeadline
                            .computeIfAbsent(projectName, key -> new ArrayList<>())
                            .add(task);
                    continue;
                }

                LocalDate date = deadlineOptional.get();

                groupedByDeadline
                        .computeIfAbsent(date, key -> new LinkedHashMap<>())
                        .computeIfAbsent(projectName, key -> new ArrayList<>())
                        .add(task);
            }
        }
    }

    private void sortTasksById(
            Map<LocalDate, Map<String, List<Task>>> groupedByDeadline,
            Map<String, List<Task>> noDeadline
    ) {
        for (Map<String, List<Task>> projects : groupedByDeadline.values()) {
            for (List<Task> list : projects.values()) {
                list.sort(Comparator.comparingLong(Task::getId));
            }
        }

        for (List<Task> list : noDeadline.values()) {
            list.sort(Comparator.comparingLong(Task::getId));
        }
    }

    public void addProject(String name) {
        taskRepository.addProject(name);
    }

    public Task addTask(String project, String description) {
        if (!taskRepository.projectExists(project)) {
            throw new IllegalArgumentException(
                    "Could not find a project with the name \"" + project + "\"."
            );
        }
        return taskRepository.addTask(project, description);
    }

    public void check(long id) {
        setDone(id, true);
    }

    public void uncheck(long id) {
        setDone(id, false);
    }

    private void setDone(long id, boolean done) {
        Task task = taskRepository.findTaskById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Could not find a task with an ID of " + id + "."
                ));
        task.setDone(done);
    }

}
