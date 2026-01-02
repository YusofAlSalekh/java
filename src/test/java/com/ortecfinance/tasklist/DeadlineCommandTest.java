package com.ortecfinance.tasklist;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public final class DeadlineCommandTest {
    private ApplicationTestSetup applicationTestSetup;

    @BeforeEach
    public void start() throws IOException {
        applicationTestSetup = new ApplicationTestSetup();
        applicationTestSetup.start_the_application();
    }

    @AfterEach
    public void close() throws IOException, InterruptedException {
        applicationTestSetup.kill_the_application();
    }

    @Test
    void deadline_is_shown_in_show_output() throws IOException {
        applicationTestSetup.execute("add project secrets");
        applicationTestSetup.execute("add task secrets Eat more donuts");
        applicationTestSetup.execute("add task secrets Destroy all humans");

        applicationTestSetup.execute("deadline 2 15-01-2025");

        applicationTestSetup.execute("show");
        applicationTestSetup.readLines(
                "secrets",
                "    [ ] 1: Eat more donuts",
                "    [ ] 2: Destroy all humans (deadline: 15-01-2025)",
                ""
        );

        applicationTestSetup.execute("quit");
    }

    @Test
    void deadline_with_invalid_date_prints_error() throws IOException {
        applicationTestSetup.execute("add project secrets");
        applicationTestSetup.execute("add task secrets Eat more donuts");

        applicationTestSetup.execute("deadline 1 2024-11-25");

        applicationTestSetup.readLines("Invalid date format: 2024-11-25, use format dd-mm-yyyy.");

        applicationTestSetup.execute("quit");
    }

    @Test
    void deadline_for_unknown_task_prints_error() throws IOException {
        applicationTestSetup.execute("add project secrets");
        applicationTestSetup.execute("add task secrets Eat more donuts");

        applicationTestSetup.execute("deadline 99 25-11-2024");

        applicationTestSetup.readLines("Could not find a task with an ID of 99.");

        applicationTestSetup.execute("quit");
    }

    @Test
    void deadline_without_arguments_prints_usage_message() throws IOException {
        applicationTestSetup.execute("deadline");

        applicationTestSetup.readLines(
                "Incorrect command line input, should be: deadline <task ID> <dd-mm-yyyy>"
        );

        applicationTestSetup.execute("quit");
    }

    @Test
    void deadline_with_only_date_prints_usage_message() throws IOException {
        applicationTestSetup.execute("deadline 01-01-2026");

        applicationTestSetup.readLines(
                "Incorrect command line input, should be: deadline <task ID> <dd-mm-yyyy>."
        );

        applicationTestSetup.execute("quit");
    }

    @Test
    void deadline_with_invalid_id_prints_error() throws IOException {
        applicationTestSetup.execute("deadline a 25-11-2024");

        applicationTestSetup.readLines(
                "Invalid id format: a."
        );

        applicationTestSetup.execute("quit");
    }

    @Test
    void deadline_with_too_many_arguments_prints_error() throws IOException {
        applicationTestSetup.execute("add project secrets");
        applicationTestSetup.execute("add task secrets Eat more donuts");

        applicationTestSetup.execute("deadline 1 25-11-2024 extra");

        applicationTestSetup.readLines(
                "Invalid date format: 25-11-2024 extra, use format dd-mm-yyyy."
        );

        applicationTestSetup.execute("quit");
    }
}
