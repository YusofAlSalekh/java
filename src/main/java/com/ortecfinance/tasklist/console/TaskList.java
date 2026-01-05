package com.ortecfinance.tasklist.console;

import com.ortecfinance.tasklist.model.Task;
import com.ortecfinance.tasklist.service.ViewByDeadlineResult;
import com.ortecfinance.tasklist.exceptions.TaskNotFoundException;
import com.ortecfinance.tasklist.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

@Profile("console")
@Component
@RequiredArgsConstructor
public final class TaskList implements CommandLineRunner {
    private static final String QUIT = "quit";
    private static final DateTimeFormatter DEADLINE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-uuuu");

    private final TaskService taskService;
    private final BufferedReader in;
    private final PrintWriter out;

    @Override
    public void run(String... args) {
        out.println("Welcome to TaskList! Type 'help' for available commands.");
        while (true) {
            out.print("> ");
            out.flush();
            String command;
            try {
                command = in.readLine();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (command.equals(QUIT)) {
                break;
            }
            execute(command);
        }
    }

    private void execute(String commandLine) {
        String[] commandRest = commandLine.split(" ", 2);
        String command = commandRest[0];
        switch (command) {
            case "show":
                show();
                break;
            case "add":
                add(commandRest[1]);
                break;
            case "check":
                check(commandRest[1]);
                break;
            case "uncheck":
                uncheck(commandRest[1]);
                break;
            case "deadline":
                if (commandRest.length < 2) {
                    out.println("Incorrect command line input, should be: deadline <task ID> <dd-mm-yyyy>");
                    break;
                }
                addDeadline(commandRest[1]);
                break;
            case "today":
                today();
                break;
            case "view-by-deadline":
                viewByDeadline();
                break;
            case "help":
                help();
                break;
            default:
                error(command);
                break;
        }
    }

    private void addDeadline(String commandLine) {
        String[] idAndDate = commandLine.split(" ", 2);

        if (!hasExactlyTwoArguments(idAndDate,
                "Incorrect command line input, should be: deadline <task ID> <dd-mm-yyyy>.")) {
            return;
        }

        Long id = parseId(idAndDate);
        if (id == null) return;

        LocalDate date = parseDate(idAndDate);
        if (date == null) return;

        try {
            taskService.addDeadline(id, date);
        } catch (TaskNotFoundException e) {
            out.println(e.getMessage());
        }
    }

    private LocalDate parseDate(String[] idAndDate) {
        LocalDate date;
        try {
            date = LocalDate.parse(idAndDate[1], DEADLINE_FORMAT);
        } catch (DateTimeParseException e) {
            out.printf("Invalid date format: %s, use format dd-mm-yyyy.", idAndDate[1]);
            out.println();
            return null;
        }
        return date;
    }

    private Long parseId(String[] idAndDate) {
        Long id;
        try {
            id = Long.parseLong(idAndDate[0]);
        } catch (NumberFormatException e) {
            out.printf("Invalid id format: %s.", idAndDate[0]);
            out.println();
            return null;
        }
        return id;
    }

    private boolean hasExactlyTwoArguments(String[] idAndDate, String message) {
        if (idAndDate.length != 2) {
            out.println(message);
            return false;
        }
        return true;
    }

    private void show() {
        Map<String, List<Task>> data = taskService.show();

        for (Map.Entry<String, List<Task>> entry : data.entrySet()) {
            out.println(entry.getKey());
            for (Task task : entry.getValue()) {
                printTask(task);
            }
            out.println();
        }
    }

    private void today() {
        Map<String, List<Task>> data = taskService.today(LocalDate.now());

        for (Map.Entry<String, List<Task>> entry : data.entrySet()) {
            out.println(entry.getKey());

            for (Task task : entry.getValue()) {
                printTask(task);
            }

            out.println();
        }
    }

    private void printTask(Task task) {
        String line = String.format(
                "    [%c] %d: %s",
                task.isDone() ? 'x' : ' ',
                task.getId(),
                task.getDescription()
        );

        if (task.getDeadline().isPresent()) {
            line += String.format(
                    " (deadline: %s)",
                    DEADLINE_FORMAT.format(task.getDeadline().get())
            );
        }

        out.println(line);
    }

    private void viewByDeadline() {
        ViewByDeadlineResult result = taskService.viewByDeadline();
        printByDeadline(result.groupedByDeadline(), result.noDeadline());
    }

    private void printByDeadline(
            Map<LocalDate, Map<String, List<Task>>> groupedByDeadline,
            Map<String, List<Task>> noDeadline
    ) {
        for (Map.Entry<LocalDate, Map<String, List<Task>>> entry : groupedByDeadline.entrySet()) {
            out.println(DEADLINE_FORMAT.format(entry.getKey()) + ":");

            for (Map.Entry<String, List<Task>> projectEntry : entry.getValue().entrySet()) {
                out.println("    " + projectEntry.getKey() + ":");
                for (Task task : projectEntry.getValue()) {
                    out.printf("        %d: %s%n", task.getId(), task.getDescription());
                }
            }
        }

        if (!noDeadline.isEmpty()) {
            out.println("No deadline:");
            for (Map.Entry<String, List<Task>> projectEntry : noDeadline.entrySet()) {
                out.println("    " + projectEntry.getKey() + ":");
                for (Task task : projectEntry.getValue()) {
                    out.printf("        %d: %s%n", task.getId(), task.getDescription());
                }
            }
        }
    }

    private void add(String commandLine) {
        String[] subcommandRest = commandLine.split(" ", 2);
        String subcommand = subcommandRest[0];
        if (subcommand.equals("project")) {
            addProject(subcommandRest[1]);
        } else if (subcommand.equals("task")) {
            String[] projectTask = subcommandRest[1].split(" ", 2);
            addTask(projectTask[0], projectTask[1]);
        }
    }

    private void addProject(String name) {
        try {
            taskService.addProject(name);
        } catch (IllegalArgumentException e) {
            out.println(e.getMessage());
        }
    }

    private void addTask(String project, String description) {
        try {
            taskService.addTask(project, description);
        } catch (IllegalArgumentException e) {
            out.println(e.getMessage());
        }
    }

    private void check(String idString) {
        setDone(idString, true);
    }

    private void uncheck(String idString) {
        setDone(idString, false);
    }

    private void setDone(String idString, boolean done) {
        Long id;
        try {
            id = Long.parseLong(idString);
        } catch (NumberFormatException e) {
            out.printf("Invalid id format: %s.%n", idString);
            return;
        }

        try {
            if (done) {
                taskService.check(id);
            } else {
                taskService.uncheck(id);
            }
        } catch (IllegalArgumentException e) {
            out.println(e.getMessage());
        }
    }

    private void help() {
        out.println("Commands:");
        out.println("  show");
        out.println("  add project <project name>");
        out.println("  add task <project name> <task description>");
        out.println("  check <task ID>");
        out.println("  uncheck <task ID>");
        out.println("  deadline <ID> <dd-mm-yyyy>");
        out.println("  today");
        out.println("  view-by-deadline");
        out.println();
    }

    private void error(String command) {
        out.printf("I don't know what the command \"%s\" is.", command);
        out.println();
    }
}
