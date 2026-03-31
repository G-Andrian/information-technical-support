package src.service;

import java.io.FileWriter;
import java.io.IOException;

public class ReportService {

    public static void saveReport(String filePath, String reportText) throws IOException {
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(reportText);
        }
    }
}