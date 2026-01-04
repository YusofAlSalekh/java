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
    void view_by_deadline_groups_by_date_then_project_and_puts_no_deadline_last() throws IOException {
        applicationTestSetup.execute("add project Secrets");
        applicationTestSetup.execute("add project Training");

        applicationTestSetup.execute("add task Secrets Eat more donuts");
        applicationTestSetup.execute("add task Training Refactor the codebase");
        applicationTestSetup.execute("add task Training Interaction-Driven Design");
        applicationTestSetup.execute("add task Training Four Elements of Simple Design");

        applicationTestSetup.execute("deadline 1 11-11-2021");
        applicationTestSetup.execute("deadline 3 13-11-2021");
        applicationTestSetup.execute("deadline 4 11-11-2021");

        applicationTestSetup.execute("view-by-deadline");
        applicationTestSetup.readLines(
                "11-11-2021:",
                "    Secrets:",
                "        1: Eat more donuts",
                "    Training:",
                "        4: Four Elements of Simple Design",
                "13-11-2021:",
                "    Training:",
                "        3: Interaction-Driven Design",
                "No deadline:",
                "    Training:",
                "        2: Refactor the codebase"
        );

        applicationTestSetup.execute("quit");
    }
}
