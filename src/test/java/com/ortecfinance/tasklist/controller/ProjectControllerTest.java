package com.ortecfinance.tasklist.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ortecfinance.tasklist.api.controller.DeadlineParser;
import com.ortecfinance.tasklist.api.controller.ProjectController;
import com.ortecfinance.tasklist.api.controller.json.request.ProjectCreationRequest;
import com.ortecfinance.tasklist.api.controller.json.request.TaskCreationRequest;
import com.ortecfinance.tasklist.api.controller.json.response.TaskResponse;
import com.ortecfinance.tasklist.api.controller.json.response.ViewByDeadlineResponse;
import com.ortecfinance.tasklist.api.mapper.ViewByDeadlineMapper;
import com.ortecfinance.tasklist.model.Task;
import com.ortecfinance.tasklist.service.TaskService;
import com.ortecfinance.tasklist.service.ViewByDeadlineResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProjectController.class)
@ActiveProfiles("web")
class ProjectControllerTest {
    @MockBean
    TaskService taskService;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    ViewByDeadlineMapper mapper;

    @MockBean
    DeadlineParser deadlineParser;

    @Test
    void createProject_success_201() throws Exception {
        ProjectCreationRequest request = new ProjectCreationRequest("secrets");

        mockMvc.perform(post("/projects")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        verify(taskService).addProject("secrets");
        verifyNoMoreInteractions(taskService);
    }

    @Test
    void createProject_validationError_400() throws Exception {
        ProjectCreationRequest request = new ProjectCreationRequest("");

        mockMvc.perform(post("/projects")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(taskService);
    }

    @Test
    void getProjects_ok_returnsListWithTasks() throws Exception {
        Task t1 = new Task(1, "Eat more donuts", false);
        Task t2 = new Task(2, "Destroy all humans", true);
        t2.setDeadline(LocalDate.of(2025, 1, 15));

        Map<String, List<Task>> data = new LinkedHashMap<>();
        data.put("secrets", List.of(t1, t2));
        data.put("training", List.of());

        when(taskService.show()).thenReturn(data);

        mockMvc.perform(get("/projects").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))

                .andExpect(jsonPath("$[0].name").value("secrets"))
                .andExpect(jsonPath("$[0].tasks", hasSize(2)))
                .andExpect(jsonPath("$[0].tasks[0].id").value(1))
                .andExpect(jsonPath("$[0].tasks[0].description").value("Eat more donuts"))
                .andExpect(jsonPath("$[0].tasks[0].done").value(false))
                .andExpect(jsonPath("$[0].tasks[0].deadline").doesNotExist())

                .andExpect(jsonPath("$[0].tasks[1].id").value(2))
                .andExpect(jsonPath("$[0].tasks[1].done").value(true))
                .andExpect(jsonPath("$[0].tasks[1].deadline").value("2025-01-15"))

                .andExpect(jsonPath("$[1].name").value("training"))
                .andExpect(jsonPath("$[1].tasks", hasSize(0)));

        verify(taskService).show();
        verifyNoMoreInteractions(taskService);
    }

    @Test
    void createTask_success_returnsCreatedAndTaskResponse() throws Exception {
        String projectName = "secrets";
        TaskCreationRequest request = new TaskCreationRequest("Eat more donuts");

        Task created = new Task(1, "Eat more donuts", false);

        when(taskService.addTask(projectName, request.getDescription())).thenReturn(created);

        mockMvc.perform(post("/projects/{projectName}/tasks", projectName)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("Eat more donuts"))
                .andExpect(jsonPath("$.done").value(false))
                .andExpect(jsonPath("$.deadline").doesNotExist());

        verify(taskService).addTask(projectName, "Eat more donuts");
        verifyNoMoreInteractions(taskService);
        verifyNoInteractions(mapper);
    }

    @Test
    void createTask_validationError_blankDescription_returns400() throws Exception {
        String projectName = "secrets";
        TaskCreationRequest request = new TaskCreationRequest("");

        mockMvc.perform(post("/projects/{projectName}/tasks", projectName)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(taskService, mapper);
    }

    @Test
    void addDeadline_success_returns204_andCallsService() throws Exception {
        mockMvc.perform(put("/projects/{projectName}/tasks/{taskId}", "secrets", 5)
                        .queryParam("deadline", "15-01-2025"))
                .andExpect(status().isNoContent());

        verify(taskService).addDeadline(5, LocalDate.of(2025, 1, 15));
        verifyNoMoreInteractions(taskService);
        verifyNoInteractions(mapper);
    }

    @Test
    void addDeadline_missingQueryParam_returns400() throws Exception {
        mockMvc.perform(put("/projects/{projectName}/tasks/{taskId}", "secrets", 5))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(taskService, mapper);
    }

    @Test
    void addDeadline_badDateFormat_returns400() throws Exception {
        when(deadlineParser.parse("2025-01-15"))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid date format, use dd-MM-yyyy."));


        mockMvc.perform(put("/projects/{projectName}/tasks/{taskId}", "secrets", 5)
                        .queryParam("deadline", "2025-01-15"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(taskService, mapper);
    }

    @Test
    void viewByDeadline_success_returnsMapperResponse() throws Exception {
        ViewByDeadlineResult result = new ViewByDeadlineResult(
                new LinkedHashMap<>(),
                new LinkedHashMap<>()
        );

        ViewByDeadlineResponse mapped = new ViewByDeadlineResponse(
                Map.of("2025-01-15", Map.of("secrets", List.of(new TaskResponse(1, "A", false, "2025-01-15")))),
                Map.of("training", List.of(new TaskResponse(2, "B", false, null)))
        );

        when(taskService.viewByDeadline()).thenReturn(result);
        when(mapper.toResponse(result)).thenReturn(mapped);

        mockMvc.perform(get("/projects/view_by_deadline")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.groupedByDeadline['2025-01-15'].secrets[0].id").value(1))
                .andExpect(jsonPath("$.noDeadline.training[0].id").value(2));

        verify(taskService).viewByDeadline();
        verify(mapper).toResponse(result);
        verifyNoMoreInteractions(taskService, mapper);
    }
}
