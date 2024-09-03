package bob.ui;

import java.util.Scanner;

/**
 * Class to represent interactions with the user.
 */
public class Ui {
    private final Scanner scanner;

    /**
     * Constructor for user interface.
     */
    public Ui() {
        scanner = new Scanner(System.in);
    }

    /**
     * Shows welcome message to the user.
     */
    public static void showWelcome() {
        String logo = "Bob";
        System.out.println("Hello! I'm " + logo);
        System.out.println("What can I do for you?");
    }

    public String readCommand() {
        return scanner.nextLine();
    }

    public static void showError(String message) {
        System.out.println(message);
    }
}
