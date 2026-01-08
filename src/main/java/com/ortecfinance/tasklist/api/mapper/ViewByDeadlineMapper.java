package com.ortecfinance.tasklist.api.mapper;

import com.ortecfinance.tasklist.api.controller.json.response.TaskResponse;
import com.ortecfinance.tasklist.api.controller.json.response.ViewByDeadlineResponse;
import com.ortecfinance.tasklist.model.Task;
import com.ortecfinance.tasklist.service.ViewByDeadlineResult;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class ViewByDeadlineMapper {

    public ViewByDeadlineResponse toResponse(ViewByDeadlineResult result) {
        return new ViewByDeadlineResponse(
                convertGroupedByDeadline(result.groupedByDeadline()),
                convertNoDeadline(result.noDeadline())
        );
    }

    private Map<String, Map<String, List<TaskResponse>>> convertGroupedByDeadline(
            Map<LocalDate, Map<String, List<Task>>> source
    ) {
        Map<String, Map<String, List<TaskResponse>>> result = new LinkedHashMap<>();

        for (Map.Entry<LocalDate, Map<String, List<Task>>> dateEntry : source.entrySet()) {
            String dateKey = dateEntry.getKey().toString();
            result.put(dateKey, convertProjects(dateEntry.getValue()));
        }

        return result;
    }

    private Map<String, List<TaskResponse>> convertNoDeadline(Map<String, List<Task>> source) {
        return convertProjects(source);
    }

    private Map<String, List<TaskResponse>> convertProjects(Map<String, List<Task>> source) {
        Map<String, List<TaskResponse>> result = new LinkedHashMap<>();

        for (Map.Entry<String, List<Task>> projectEntry : source.entrySet()) {
            result.put(projectEntry.getKey(), convertTasks(projectEntry.getValue()));
        }

        return result;
    }

    private List<TaskResponse> convertTasks(List<Task> tasks) {
        List<TaskResponse> result = new ArrayList<>();
        for (Task task : tasks) {
            result.add(toTaskResponse(task));
        }
        return result;
    }

    private TaskResponse toTaskResponse(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getDescription(),
                task.isDone(),
                task.getDeadline().map(LocalDate::toString).orElse(null)
        );
    }
}
