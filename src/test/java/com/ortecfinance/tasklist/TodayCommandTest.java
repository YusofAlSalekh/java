package com.ortecfinance.tasklist;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class TodayCommandTest {
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("dd-MM-uuuu");
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
    void today_shows_only_tasks_due_today_and_only_matching_projects() throws IOException {
        String today = LocalDate.now().format(DATE_TIME_FORMAT);
        String tomorrow = LocalDate.now().plusDays(1).format(DATE_TIME_FORMAT);

        applicationTestSetup.execute("add project secrets");
        applicationTestSetup.execute("add task secrets Eat more donuts");
        applicationTestSetup.execute("add task secrets Destroy all humans");

        applicationTestSetup.execute("add project training");
        applicationTestSetup.execute("add task training SOLID");

        applicationTestSetup.execute("deadline 2 " + today);
        applicationTestSetup.execute("deadline 3 " + tomorrow);

        applicationTestSetup.execute("today");
        applicationTestSetup.readLines(
                "secrets",
                "    [ ] 2: Destroy all humans (deadline: " + today + ")",
                ""
        );

        applicationTestSetup.execute("quit");
    }

    @Test
    void today_does_not_show_tasks_whose_deadlines_is_not_today() throws IOException {
        String tomorrow = LocalDate.now().plusDays(1).format(DATE_TIME_FORMAT);

        applicationTestSetup.execute("add project secrets");
        applicationTestSetup.execute("add task secrets Eat more donuts");
        applicationTestSetup.execute("add task secrets Destroy all humans");

        applicationTestSetup.execute("deadline 1 " + tomorrow);

        applicationTestSetup.execute("today");

        applicationTestSetup.execute("show");
        applicationTestSetup.readLines(
                "secrets",
                "    [ ] 1: Eat more donuts (deadline: " + tomorrow + ")",
                "    [ ] 2: Destroy all humans",
                ""
        );

        applicationTestSetup.execute("quit");
    }
}
