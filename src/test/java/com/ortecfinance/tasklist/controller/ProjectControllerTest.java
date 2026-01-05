package com.ortecfinance.tasklist.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ortecfinance.tasklist.api.controller.ProjectController;
import com.ortecfinance.tasklist.api.controller.json.request.ProjectCreationRequest;
import com.ortecfinance.tasklist.model.Task;
import com.ortecfinance.tasklist.service.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProjectController.class)
@ActiveProfiles("web")
class ProjectControllerTest {
    @MockBean
    TaskService taskService;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

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
}
