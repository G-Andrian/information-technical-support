package src.service;

import java.io.FileWriter;
import java.io.IOException;

public class ReportService {

    public void saveReport(double attendance,
                           double score,
                           double activity,
                           String filePath,
                           String reportText,
                           String prediction,
                           String risk) {

        try (FileWriter writer = new FileWriter(filePath, true)) {

            writer.write("=== ЗВІТ ===\n");
            writer.write("Відвідуваність: " + attendance + "\n");
            writer.write("Середній бал: " + score + "\n");
            writer.write("Активність: " + activity + "\n");
            writer.write("Прогноз: " + prediction + "\n");
            writer.write("Ризик: " + risk + "\n");
            writer.write("----------------------\n\n");

        } catch (IOException e) {
            System.out.println("Помилка запису: " + e.getMessage());
        }
    }
}