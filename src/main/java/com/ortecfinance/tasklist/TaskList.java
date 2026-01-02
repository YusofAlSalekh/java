package com.ortecfinance.tasklist;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

public final class TaskList implements Runnable {
    private static final String QUIT = "quit";
    private static final DateTimeFormatter DEADLINE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-uuuu");

    private final Map<String, List<Task>> tasks = new LinkedHashMap<>();
    private final BufferedReader in;
    private final PrintWriter out;

    private long lastId = 0;

    public static void startConsole() {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter out = new PrintWriter(System.out);
        new TaskList(in, out).run();
    }

    public TaskList(BufferedReader reader, PrintWriter writer) {
        this.in = reader;
        this.out = writer;
    }

    public void run() {
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

        Optional<Task> taskOptional = findTaskById(id);
        if (taskOptional.isEmpty()) {
            out.printf("Could not find a task with an ID of %d.", id);
            out.println();
            return;
        }

        LocalDate date = parseDate(idAndDate);
        if (date == null) return;

        Task task = taskOptional.get();
        task.setDeadline(date);
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
        for (Map.Entry<String, List<Task>> project : tasks.entrySet()) {
            out.println(project.getKey());
            for (Task task : project.getValue()) {
                printTask(task);
            }
            out.println();
        }
    }

    private void today() {
        LocalDate today = LocalDate.now();

        for (Map.Entry<String, List<Task>> project : tasks.entrySet()) {
            List<Task> dueTodayTasks = new ArrayList<>();

            for (Task task : project.getValue()) {
                if (task.getDeadline().isPresent() && task.getDeadline().get().equals(today)) {
                    dueTodayTasks.add(task);
                }
            }

            if (dueTodayTasks.isEmpty()) {
                continue;
            }

            out.println(project.getKey());
            for (Task task : dueTodayTasks) {
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
        Map<LocalDate, List<Task>> groupedByDeadline = new TreeMap<>();
        List<Task> noDeadline = new ArrayList<>();

        groupTasksByDeadline(noDeadline, groupedByDeadline);

        sortTasksById(groupedByDeadline, noDeadline);

        printByDeadline(groupedByDeadline, noDeadline);
    }

    private void groupTasksByDeadline(List<Task> noDeadline, Map<LocalDate, List<Task>> groupedByDeadline) {
        for (List<Task> projectTasks : tasks.values()) {
            for (Task task : projectTasks) {
                Optional<LocalDate> deadlineOptional = task.getDeadline();
                if (deadlineOptional.isEmpty()) {
                    noDeadline.add(task);
                    continue;
                }

                LocalDate date = deadlineOptional.get();
                groupedByDeadline.computeIfAbsent(date, key -> new ArrayList<>()).add(task);
            }
        }
    }

    private void printByDeadline(Map<LocalDate, List<Task>> groupedByDeadline, List<Task> noDeadline) {
        for (Map.Entry<LocalDate, List<Task>> entry : groupedByDeadline.entrySet()) {
            out.println(DEADLINE_FORMAT.format(entry.getKey()) + ":");
            for (Task task : entry.getValue()) {
                out.printf("    %d: %s%n", task.getId(), task.getDescription());
            }
        }

        if (!noDeadline.isEmpty()) {
            out.println("No deadline:");
            for (Task task : noDeadline) {
                out.printf("    %d: %s%n", task.getId(), task.getDescription());
            }
        }
    }

    private static void sortTasksById(Map<LocalDate, List<Task>> groupedByDeadline, List<Task> noDeadline) {
        for (List<Task> list : groupedByDeadline.values()) {
            list.sort(Comparator.comparingLong(Task::getId));
        }
        noDeadline.sort(Comparator.comparingLong(Task::getId));
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
        tasks.put(name, new ArrayList<Task>());
    }

    private void addTask(String project, String description) {
        List<Task> projectTasks = tasks.get(project);
        if (projectTasks == null) {
            out.printf("Could not find a project with the name \"%s\".", project);
            out.println();
            return;
        }
        projectTasks.add(new Task(nextId(), description, false));
    }

    private void check(String idString) {
        setDone(idString, true);
    }

    private void uncheck(String idString) {
        setDone(idString, false);
    }

    private void setDone(String idString, boolean done) {
        int id = Integer.parseInt(idString);
        for (Map.Entry<String, List<Task>> project : tasks.entrySet()) {
            for (Task task : project.getValue()) {
                if (task.getId() == id) {
                    task.setDone(done);
                    return;
                }
            }
        }
        out.printf("Could not find a task with an ID of %d.", id);
        out.println();
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

    private long nextId() {
        return ++lastId;
    }

    private Optional<Task> findTaskById(Long id) {
        for (Map.Entry<String, List<Task>> project : tasks.entrySet()) {
            for (Task task : project.getValue()) {
                if (task.getId() == id) {
                    return Optional.of(task);
                }
            }
        }
        return Optional.empty();
    }
}
