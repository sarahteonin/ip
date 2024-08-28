import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Scanner;

public class Bob {
    enum Command {
        LIST, UNMARK, MARK, DELETE, ON, TODO, DEADLINE, EVENT, BYE, INVALID
    }

    private static final String DIR_PATH = "./data";
    private static final String FILE_PATH = DIR_PATH + "/bob.txt";

    public static void main(String[] args) throws IOException {
        String logo = "Bob";
        System.out.println("Hello! I'm " + logo);
        System.out.println("What can I do for you?");

        FileReading.createDirectory(DIR_PATH);
        FileReading.createFile(FILE_PATH);
        ArrayList<Task> list = FileReading.loadTasks(FILE_PATH);
        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine();

        while (!getCommand(input).equals(Command.BYE)) {
            try {
                Command command = getCommand(input);
                switch(command) {
                    case LIST:
                        listTasks(list);
                        break;
                    case UNMARK:
                        unmarkTask(list, input);
                        break;
                    case MARK:
                        markTask(list, input);
                        break;
                    case ON:
                        String date;
                        try {
                            date = scanner.nextLine();
                            listTasksOnDate(date, list);
                        } catch (DateTimeParseException e) {
                            throw new BobException("Invalid date. Please enter a valid date in the format: dd/MM/yyyy");
                        }
                        break;
                    case DELETE:
                        deleteTask(list, input);
                        break;
                    case TODO:
                        addToDoTask(list, input);
                        break;
                    case DEADLINE:
                        addDeadlineTask(list,input);
                        break;
                    case EVENT:
                        addEventTask(list, input);
                        break;
                    case INVALID:
                        throw new BobException("Invalid command. Please enter a valid command. Valid commands are: list, unmark, mark, delete, on, todo, deadline, event, and bye.");
                }
            } catch (BobException e) {
                System.out.println(e.getMessage());
            } catch (Exception e) {
                System.out.println("An unexpected error occurred: " + e.getMessage());
            }
            input = scanner.nextLine();
        }
        FileWriting.saveTasks(FILE_PATH, list);
        System.out.println("Bye. Hope to see you again soon!");
    }

    private static Command getCommand(String input) {
        for (Command command : Command.values()) {
            if (input.startsWith(command.name().toLowerCase())) {
                return command;
            }
        }
        return Command.INVALID;
    }

    private static void listTasks(ArrayList<Task> list) {
        if (list.isEmpty()) {
            System.out.println("There are no tasks in your list.");
            return;
        }
        System.out.println("Here are the tasks in your list:");

        for (int i = 0; i < list.size(); i++) {
            System.out.println((i + 1) + ". " + list.get(i));
        }
    }

    private static void markTask(ArrayList<Task> list, String input) throws BobException {
        String[] parts = input.split(" ");
        int index = Integer.parseInt(parts[1]) - 1;
        if (index < 0 || index >= list.size()) {
            throw new BobException("Please enter a valid task number.");
        }
        list.get(index).markAsDone();
        System.out.println("Nice! I've marked this task as done:\n" + list.get(index));
    }

    private static void unmarkTask(ArrayList<Task> list, String input) throws BobException {
        String[] parts = input.split(" ");
        int index = Integer.parseInt(parts[1]) - 1;
        if (index < 0 || index >= list.size()) {
            throw new BobException("Please enter a valid task number.");
        }
        list.get(index).markAsNotDone();
        System.out.println("OK, I've marked this task as not done yet:\n" + list.get(index));
    }

    private static void deleteTask(ArrayList<Task> list, String input) throws BobException {
        String[] parts = input.split(" ");
        int index = Integer.parseInt(parts[1]) - 1;
        if (index < 0 || index >= list.size()) {
            throw new BobException("Please enter a valid task number.");
        }

        System.out.println("Noted. I've removed this task:\n" + list.get(index));
        list.remove(index);
        System.out.println("Now you have " + list.size() + (list.isEmpty() ? " task in the list." : " tasks in the list."));
    }

    private static void addToDoTask(ArrayList<Task> list, String input) throws BobException {
        String description = input.substring("todo".length()).trim();
        if (description.isEmpty()) {
            throw new BobException("The description of a todo cannot be empty.");
        }
        Task t = new Todo(description);
        list.add(t);
        taskAdded(list, t);
    }

    private static void addDeadlineTask(ArrayList<Task> list, String input) throws BobException {
        String[] parts = input.split("/by");
        if (parts.length < 2) {
            throw new BobException("The date of a deadline cannot be empty. Please enter in the format: description /by <date>");
        }
        String description = parts[0].substring("deadline".length()).trim();
        String date = parts[1].trim();
        if (description.isEmpty()) {
            throw new BobException("The description of a deadline cannot be empty.");
        }
        Task t;
        try {
            LocalDateTime by = Deadline.parseDateTime(date);
            t = new Deadline(description, by);
        } catch (DateTimeParseException e) {
            throw new BobException("Invalid date. Please enter in the format: yyyy-MM-dd HH:mm");
        }
        list.add(t);
        taskAdded(list, t);
    }

    private static void addEventTask(ArrayList<Task> list, String input) throws BobException {
        String[] parts = input.split("/from|/to");
        if (parts.length < 3) {
            throw new BobException("The start and end date/time of an event cannot be empty. Please enter in the format: description /from <start> /to <end>");
        }
        String description = parts[0].substring("event".length()).trim();
        String from = parts[1].trim();
        String to = parts[2].trim();
        if (description.isEmpty()) {
            throw new BobException("The description of an event cannot be empty.");
        }
        Task t;
        try {
            LocalDateTime fromTime = Event.parseDateTime(from);
            t = new Event(description, fromTime, to);
        } catch (DateTimeParseException e) {
            throw new BobException("Invalid start and end date. Please enter in the format: /from yyyy-MM-dd HH:mm /to: yyyy-MM-dd HH:mm or HH:mm");
        }
        list.add(t);
        taskAdded(list, t);
    }

    private static void taskAdded(ArrayList<Task> list, Task t) {
        System.out.println("Got it. I've added this task:\n" + t);
        System.out.println("Now you have " + list.size() + (list.size() == 1 ? " task in the list." : " tasks in the list."));
    }

    private static void listTasksOnDate(String date, ArrayList<Task> list) {
        int count = 0;
        LocalDate d = LocalDate.parse(date, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        String dateFormatted = d.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        StringBuilder taskOnDate = new StringBuilder("Here are the tasks on " + dateFormatted + ":");
        for (Task t : list) {
            LocalDate taskDate = t.getDate();
            if (t.getDate()!=(null) && taskDate.equals(d)) {
                taskOnDate.append("\n").append(++count).append(". ").append(t);
            }
        }
        if (count == 0) {
            System.out.println("There are no tasks on " + dateFormatted + ".");
            return;
        }
        System.out.println(taskOnDate);
    }
}
