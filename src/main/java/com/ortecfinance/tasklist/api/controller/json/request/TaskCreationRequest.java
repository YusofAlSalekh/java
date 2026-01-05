package com.ortecfinance.tasklist.api.controller.json.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskCreationRequest {
    @NotBlank(message = "Task description is required")
    private String description;
}