package com.ortecfinance.tasklist.api.controller;

import com.ortecfinance.tasklist.Task;
import com.ortecfinance.tasklist.TaskService;
import com.ortecfinance.tasklist.api.controller.json.request.ProjectCreationRequest;
import com.ortecfinance.tasklist.api.controller.json.response.ProjectResponse;
import com.ortecfinance.tasklist.api.controller.json.response.TaskResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Profile("web")
@RestController
@RequiredArgsConstructor
@RequestMapping("/projects")
public class ProjectController {
    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<Void> createProject(@RequestBody @Valid ProjectCreationRequest request) {
        taskService.addProject(request.getName());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping
    public List<ProjectResponse> getProjects() {
        Map<String, List<Task>> data = taskService.show();
        List<ProjectResponse> response = new ArrayList<>();

        for (Map.Entry<String, List<Task>> entry : data.entrySet()) {
            List<TaskResponse> tasks = new ArrayList<>();
            for (Task task : entry.getValue()) {
                tasks.add(new TaskResponse(
                        task.getId(),
                        task.getDescription(),
                        task.isDone(),
                        task.getDeadline().map(LocalDate::toString).orElse(null)
                ));
            }
            response.add(new ProjectResponse(entry.getKey(), tasks));
        }

        return response;
    }
}
