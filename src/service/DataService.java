package src.service;

import src.model.StudentRecord;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class DataService {

    public static List<StudentRecord> loadFromCsv(String filePath) throws Exception {
        List<StudentRecord> data = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean firstLine = true;

            while ((line = br.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }

                String[] parts = line.split(",");
                if (parts.length != 4) {
                    continue;
                }

                double attendance = Double.parseDouble(parts[0].trim());
                double score = Double.parseDouble(parts[1].trim());
                double activity = Double.parseDouble(parts[2].trim());
                int passed = Integer.parseInt(parts[3].trim());

                data.add(new StudentRecord(attendance, score, activity, passed));
            }
        }

        return data;
    }
}