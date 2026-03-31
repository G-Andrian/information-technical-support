package src.service;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;

public class LogService {
    private static final String LOG_FILE = "app.log";

    public static void info(String message) {
        write("INFO", message);
    }

    public static void error(String message) {
        write("ERROR", message);
    }

    private static void write(String level, String message) {
        try (FileWriter writer = new FileWriter(LOG_FILE, true)) {
            writer.write(LocalDateTime.now() + " [" + level + "] " + message + System.lineSeparator());
        } catch (IOException e) {
            System.out.println("Не вдалося записати лог: " + e.getMessage());
        }
    }
}