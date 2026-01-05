package com.ortecfinance.tasklist.api.controller.json.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class ViewByDeadlineResponse {
    private Map<String, Map<String, List<TaskResponse>>> groupedByDeadline;
    private Map<String, List<TaskResponse>> noDeadline;
}
