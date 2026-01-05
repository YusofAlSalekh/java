package com.ortecfinance.tasklist.api.controller;

import com.ortecfinance.tasklist.api.controller.json.request.TaskCreationRequest;
import com.ortecfinance.tasklist.api.controller.json.response.TaskResponse;
import com.ortecfinance.tasklist.api.controller.json.response.ViewByDeadlineResponse;
import com.ortecfinance.tasklist.api.mapper.ViewByDeadlineMapper;
import com.ortecfinance.tasklist.model.Task;
import com.ortecfinance.tasklist.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Profile("web")
@RestController
@RequiredArgsConstructor
@RequestMapping("/task")
public class TaskController {
    private final TaskService taskService;
    private final ViewByDeadlineMapper mapper;

    @PostMapping("/{projectName}/tasks")
    public ResponseEntity<TaskResponse> createTask(
            @PathVariable String projectName,
            @RequestBody @Valid TaskCreationRequest request
    ) {
        Task task = taskService.addTask(projectName, request.getDescription());

        TaskResponse response = new TaskResponse(
                task.getId(),
                task.getDescription(),
                task.isDone(),
                null
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{projectName}/tasks/{taskId}")
    public ResponseEntity<Void> updateDeadline(
            @PathVariable String projectName,
            @PathVariable long taskId,
            @RequestParam("deadline") @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate deadline
    ) {
        taskService.addDeadline(taskId, deadline);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/projects/view_by_deadline")
    public ViewByDeadlineResponse viewByDeadline() {
        return mapper.toResponse(taskService.viewByDeadline());
    }
}
