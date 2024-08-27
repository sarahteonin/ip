import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class FileWriting {
    private static final String SEPARATOR = " | ";
    protected static void saveTasks(String filePath, ArrayList<Task> updatedList) throws IOException {
        FileWriter fw = new FileWriter(filePath);
        for (Task t : updatedList) {
            fw.write(formatTasks(t) + "\n");
        }
        fw.close();
    }

    protected static String formatTasks(Task t) {
        String type = t.getType();
        int status = t.isDone? 1 : 0;
        String description = t.getDescription();
        String formatted = type + SEPARATOR + status + SEPARATOR + description;

        if (type.equals("D")) {
            String by = t.getBy();
            formatted = formatted + SEPARATOR + by;
        }

        if (type.equals("E")) {
            String from = t.getFrom();
            String to = t.getTo();
            formatted = formatted + SEPARATOR + from + SEPARATOR + to;
        }
        return formatted;
    }
}
