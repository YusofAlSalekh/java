package com.ortecfinance.tasklist;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public final class ApplicationTest {
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
    void it_works() throws IOException {
        applicationTestSetup.execute("show");

        applicationTestSetup.execute("add project secrets");
        applicationTestSetup.execute("add task secrets Eat more donuts.");
        applicationTestSetup.execute("add task secrets Destroy all humans.");

        applicationTestSetup.execute("show");
        applicationTestSetup.readLines(
                "secrets",
                "    [ ] 1: Eat more donuts.",
                "    [ ] 2: Destroy all humans.",
                ""
        );

        applicationTestSetup.execute("add project training");
        applicationTestSetup.execute("add task training Four Elements of Simple Design");
        applicationTestSetup.execute("add task training SOLID");
        applicationTestSetup.execute("add task training Coupling and Cohesion");
        applicationTestSetup.execute("add task training Primitive Obsession");
        applicationTestSetup.execute("add task training Outside-In TDD");
        applicationTestSetup.execute("add task training Interaction-Driven Design");

        applicationTestSetup.execute("check 1");
        applicationTestSetup.execute("check 3");
        applicationTestSetup.execute("check 5");
        applicationTestSetup.execute("check 6");

        applicationTestSetup.execute("show");
        applicationTestSetup.readLines(
                "secrets",
                "    [x] 1: Eat more donuts.",
                "    [ ] 2: Destroy all humans.",
                "",
                "training",
                "    [x] 3: Four Elements of Simple Design",
                "    [ ] 4: SOLID",
                "    [x] 5: Coupling and Cohesion",
                "    [x] 6: Primitive Obsession",
                "    [ ] 7: Outside-In TDD",
                "    [ ] 8: Interaction-Driven Design",
                ""
        );

        applicationTestSetup.execute("quit");
    }
}
