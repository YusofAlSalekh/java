package com.ortecfinance.tasklist.service;

import com.ortecfinance.tasklist.model.Task;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record ViewByDeadlineResult(
        Map<LocalDate, Map<String, List<Task>>> groupedByDeadline,
        Map<String, List<Task>> noDeadline
) {
}
