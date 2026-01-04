package com.ortecfinance.tasklist.api.controller.json.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TaskResponse {
    private long id;
    private String description;
    private boolean done;
    private String deadline;
}
