package com.ortecfinance.tasklist.api.controller.json.response;

import com.ortecfinance.tasklist.Task;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ProjectResponse {
    private String name;
    private List<TaskResponse> tasks;
}
