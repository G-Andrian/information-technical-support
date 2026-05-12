package src.ui;

import src.ml.LogisticRegression;
import src.model.StudentRecord;
import src.service.*;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MainFrame extends JFrame {

    private JTextField attendanceField;
    private JTextField scoreField;
    private JTextField activityField;
    private JTextArea outputArea;

    private LogisticRegression model;
    private List<StudentRecord> dataset = new ArrayList<>();

    private double lastProbability = -1;
    private String lastPredictionText = "Немає";
    private String lastRisk = "Немає";

    public MainFrame() {
        setTitle(ConfigService.get("app.title", "Прогнозування успішності студентів"));
        setSize(
                ConfigService.getInt("window.width", 750),
                ConfigService.getInt("window.height", 550)
        );
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
        setVisible(true);
    }

    private void initComponents() {

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));

        // INPUT
        JPanel inputPanel = new JPanel(new GridLayout(3, 2, 8, 8));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Дані студента"));

        attendanceField = new JTextField();
        scoreField = new JTextField();
        activityField = new JTextField();

        inputPanel.add(new JLabel("Відвідуваність:"));
        inputPanel.add(attendanceField);

        inputPanel.add(new JLabel("Середній бал:"));
        inputPanel.add(scoreField);

        inputPanel.add(new JLabel("Активність:"));
        inputPanel.add(activityField);

        // BUTTONS
        JPanel buttonPanel = new JPanel(new FlowLayout());

        JButton loadButton = new JButton("Завантажити дані");
        JButton trainButton = new JButton("Навчити модель");
        JButton predictButton = new JButton("Спрогнозувати");
        JButton riskButton = new JButton("Оцінити ризик");
        JButton saveButton = new JButton("Зберегти звіт");

        buttonPanel.add(loadButton);
        buttonPanel.add(trainButton);
        buttonPanel.add(predictButton);
        buttonPanel.add(riskButton);
        buttonPanel.add(saveButton);

        // OUTPUT
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 13));

        JScrollPane scrollPane = new JScrollPane(outputArea);

        scrollPane.setBorder(BorderFactory.createTitledBorder("Результат"));

        mainPanel.add(inputPanel, BorderLayout.NORTH);
        mainPanel.add(buttonPanel, BorderLayout.CENTER);
        mainPanel.add(scrollPane, BorderLayout.SOUTH);

        add(mainPanel);

        // EVENTS
        loadButton.addActionListener(e -> loadData());
        trainButton.addActionListener(e -> trainModel());
        predictButton.addActionListener(e -> predict());
        riskButton.addActionListener(e -> calculateRisk());
        saveButton.addActionListener(e -> saveReport());
    }

    // LOAD
    private void loadData() {
        try {
            String path = ConfigService.get("data.file", "data/students.csv");
            dataset = DataService.loadFromCsv(path);

            outputArea.append("Завантажено: " + dataset.size() + " записів\n");
            LogService.info("CSV loaded: " + dataset.size());

        } catch (Exception e) {
            showError("Помилка завантаження: " + e.getMessage());
        }
    }

    // TRAIN
    private void trainModel() {
        try {
            if (dataset.isEmpty()) {
                throw new IllegalStateException("Спочатку завантаж дані");
            }

            model = new LogisticRegression(3);
            model.train(dataset,
                    ConfigService.getDouble("model.learningRate", 0.1),
                    ConfigService.getInt("model.epochs", 5000));

            double acc = model.evaluateAccuracy(dataset);

            outputArea.append("Модель навчена\nТочність: " + (acc * 100) + "%\n\n");

        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    // PREDICT
    private void predict() {
        try {
            validateModel();

            double a = parse(attendanceField.getText(), "Відвідуваність");
            double s = parse(scoreField.getText(), "Середній бал");
            double ac = parse(activityField.getText(), "Активність");

            lastProbability = model.predictProbability(a, s, ac);
            int pred = model.predict(a, s, ac);

            lastPredictionText = (pred == 1) ? "УСПІШНИЙ" : "НЕУСПІШНИЙ";

            outputArea.append("=== ПРОГНОЗ ===\n");
            outputArea.append("Ймовірність: " + (lastProbability * 100) + "%\n");
            outputArea.append("Результат: " + lastPredictionText + "\n\n");

        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    // RISK
    private void calculateRisk() {
        try {
            double a = parse(attendanceField.getText(), "Відвідуваність");
            double s = parse(scoreField.getText(), "Середній бал");
            double ac = parse(activityField.getText(), "Активність");

            lastRisk = new RiskAnalysisService().calculateRisk(a, s, ac);

            outputArea.append("=== РИЗИК ===\n");
            outputArea.append("Рівень: " + lastRisk + "\n\n");

        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    private void saveReport() {
    try {
        String reportPath = ConfigService.get("report.file", "report.txt");

        double attendance = parse(attendanceField.getText(), "Відвідуваність");
        double score = parse(scoreField.getText(), "Середній бал");
        double activity = parse(activityField.getText(), "Активність");

        String risk = new src.service.RiskAnalysisService()
                .calculateRisk(attendance, score, activity);

        new ReportService().saveReport(
                attendance,
                score,
                activity,
                reportPath,
                buildReport(),
                lastPredictionText,
                risk
        );

        outputArea.append("Звіт збережено у файл: " + reportPath + "\n");

    } catch (Exception e) {
        JOptionPane.showMessageDialog(this,
                "Помилка: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
    }
}

    // REPORT
    private String buildReport() {
        return "ЗВІТ\n"
                + "Прогноз: " + lastPredictionText + "\n"
                + "Ймовірність: " + lastProbability + "\n"
                + "Ризик: " + lastRisk + "\n";
    }

    // VALIDATION
    private double parse(String v, String name) {
        if (v == null || v.trim().isEmpty())
            throw new IllegalArgumentException(name + " порожній");

        double val = Double.parseDouble(v);

        if (val < 0 || val > 100)
            throw new IllegalArgumentException(name + " має бути 0-100");

        return val;
    }

    private void validateModel() {
        if (model == null || !model.isTrained())
            throw new IllegalStateException("Спочатку навчи модель");
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Помилка", JOptionPane.ERROR_MESSAGE);
    }
}