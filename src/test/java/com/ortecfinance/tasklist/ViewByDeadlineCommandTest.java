package com.ortecfinance.tasklist;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class ViewByDeadlineCommandTest {
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
    void view_by_deadline_groups_tasks_chronologically_putting_no_deadline_last() throws IOException {
        applicationTestSetup.execute("add project secrets");
        applicationTestSetup.execute("add task secrets Eat more donuts");

        applicationTestSetup.execute("add project training");
        applicationTestSetup.execute("add task training Refactor the codebase");
        applicationTestSetup.execute("add task training Interaction-Driven Design");
        applicationTestSetup.execute("add task training Four Elements of Simple Design");

        applicationTestSetup.execute("deadline 1 11-11-2021");
        applicationTestSetup.execute("deadline 3 13-11-2021");
        applicationTestSetup.execute("deadline 4 11-11-2021");

        applicationTestSetup.execute("view-by-deadline");
        applicationTestSetup.readLines(
                "11-11-2021:",
                "    1: Eat more donuts",
                "    4: Four Elements of Simple Design",
                "13-11-2021:",
                "    3: Interaction-Driven Design",
                "No deadline:",
                "    2: Refactor the codebase"
        );

        applicationTestSetup.execute("quit");
    }
}
