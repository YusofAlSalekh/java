package com.ortecfinance.tasklist.api.controller.json.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectCreationRequest {
    @NotBlank(message = "Project name is required")
    private String name;
}
