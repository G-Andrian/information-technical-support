package src.ui;

import src.ml.LogisticRegression;
import src.model.StudentRecord;
import src.service.ConfigService;
import src.service.DataService;
import src.service.LogService;
import src.service.ReportService;

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

        JPanel inputPanel = new JPanel(new GridLayout(4, 2, 8, 8));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Введення даних студента"));

        inputPanel.add(new JLabel("Відвідуваність (0-100):"));
        attendanceField = new JTextField();
        inputPanel.add(attendanceField);

        inputPanel.add(new JLabel("Середній бал (0-100):"));
        scoreField = new JTextField();
        inputPanel.add(scoreField);

        inputPanel.add(new JLabel("Активність (0-100):"));
        activityField = new JTextField();
        inputPanel.add(activityField);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        JButton loadButton = new JButton("Завантажити дані");
        JButton trainButton = new JButton("Навчити модель");
        JButton predictButton = new JButton("Спрогнозувати");
        JButton saveButton = new JButton("Зберегти звіт");

        buttonPanel.add(loadButton);
        buttonPanel.add(trainButton);
        buttonPanel.add(predictButton);
        buttonPanel.add(saveButton);

        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(outputArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Результат роботи програми"));

        mainPanel.add(inputPanel, BorderLayout.NORTH);
        mainPanel.add(buttonPanel, BorderLayout.CENTER);
        mainPanel.add(scrollPane, BorderLayout.SOUTH);

        add(mainPanel);

        loadButton.addActionListener(e -> loadData());
        trainButton.addActionListener(e -> trainModel());
        predictButton.addActionListener(e -> predict());
        saveButton.addActionListener(e -> saveReport());
    }

    private void loadData() {
        try {
            String filePath = ConfigService.get("data.file", "data/students.csv");
            dataset = DataService.loadFromCsv(filePath);

            outputArea.append("Дані успішно завантажено: " + dataset.size() + " записів\n");
            LogService.info("Завантажено " + dataset.size() + " записів із CSV.");
        } catch (Exception e) {
            LogService.error("Помилка завантаження даних: " + e.getMessage());
            JOptionPane.showMessageDialog(this,
                    "Не вдалося завантажити дані: " + e.getMessage(),
                    "Помилка",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void trainModel() {
        try {
            if (dataset == null || dataset.isEmpty()) {
                throw new IllegalStateException("Спочатку завантажте дані.");
            }

            model = new LogisticRegression(3);

            double lr = ConfigService.getDouble("model.learningRate", 0.1);
            int epochs = ConfigService.getInt("model.epochs", 5000);

            model.train(dataset, lr, epochs);
            double accuracy = model.evaluateAccuracy(dataset);

            outputArea.append("Модель навчена успішно.\n");
            outputArea.append(String.format("Точність моделі: %.2f%%\n", accuracy * 100));
            outputArea.append("Ваги моделі:\n");
            outputArea.append(String.format("w1 = %.4f, w2 = %.4f, w3 = %.4f, b = %.4f\n\n",
                    model.getWeights()[0], model.getWeights()[1], model.getWeights()[2], model.getBias()));

            LogService.info("Модель навчена. Точність: " + (accuracy * 100) + "%");
        } catch (Exception e) {
            LogService.error("Помилка навчання моделі: " + e.getMessage());
            JOptionPane.showMessageDialog(this,
                    "Не вдалося навчити модель: " + e.getMessage(),
                    "Помилка",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void predict() {
        try {
            if (model == null || !model.isTrained()) {
                throw new IllegalStateException("Спочатку потрібно навчити модель.");
            }

            double attendance = parseAndValidate(attendanceField.getText(), "Відвідуваність");
            double score = parseAndValidate(scoreField.getText(), "Середній бал");
            double activity = parseAndValidate(activityField.getText(), "Активність");

            lastProbability = model.predictProbability(attendance, score, activity);
            int prediction = model.predict(attendance, score, activity);

            lastPredictionText = prediction == 1 ? "СКЛАВ" : "НЕ СКЛАВ";

            outputArea.append("=== Новий прогноз ===\n");
            outputArea.append("Відвідуваність: " + attendance + "\n");
            outputArea.append("Середній бал: " + score + "\n");
            outputArea.append("Активність: " + activity + "\n");
            outputArea.append(String.format("Ймовірність успішного складання: %.2f%%\n", lastProbability * 100));
            outputArea.append("Прогноз: " + lastPredictionText + "\n\n");

            LogService.info("Виконано прогноз: " + lastPredictionText + ", ймовірність=" + (lastProbability * 100) + "%");
        } catch (Exception e) {
            LogService.error("Помилка прогнозування: " + e.getMessage());
            JOptionPane.showMessageDialog(this,
                    e.getMessage(),
                    "Помилка введення",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void saveReport() {
        try {
            String reportPath = ConfigService.get("report.file", "report.txt");

            String report = buildReport();
            ReportService.saveReport(reportPath, report);

            outputArea.append("Звіт збережено у файл: " + reportPath + "\n");
            LogService.info("Звіт збережено у файл " + reportPath);
        } catch (Exception e) {
            LogService.error("Помилка збереження звіту: " + e.getMessage());
            JOptionPane.showMessageDialog(this,
                    "Не вдалося зберегти звіт: " + e.getMessage(),
                    "Помилка",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private String buildReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("ЗВІТ ПРО РОБОТУ ПРОГРАМИ\n");
        sb.append("====================================\n");
        sb.append("Назва: ").append(ConfigService.get("app.title", "Програма")).append("\n");
        sb.append("Версія: ").append(ConfigService.get("app.version", "1.0")).append("\n\n");

        sb.append("Кількість записів у наборі даних: ").append(dataset.size()).append("\n");

        if (model != null && model.isTrained()) {
            double accuracy = model.evaluateAccuracy(dataset);
            sb.append(String.format("Точність моделі: %.2f%%\n", accuracy * 100));
            sb.append(String.format("Ваги: w1=%.4f, w2=%.4f, w3=%.4f, b=%.4f\n",
                    model.getWeights()[0], model.getWeights()[1], model.getWeights()[2], model.getBias()));
        } else {
            sb.append("Модель ще не навчена.\n");
        }

        sb.append("\nОстанній прогноз:\n");
        sb.append("Результат: ").append(lastPredictionText).append("\n");
        if (lastProbability >= 0) {
            sb.append(String.format("Ймовірність: %.2f%%\n", lastProbability * 100));
        } else {
            sb.append("Прогноз ще не виконано.\n");
        }

        sb.append("\nВміст журналу роботи можна переглянути у файлі app.log\n");

        return sb.toString();
    }

    private double parseAndValidate(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Поле \"" + fieldName + "\" не може бути порожнім.");
        }

        double number;
        try {
            number = Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Поле \"" + fieldName + "\" повинно містити число.");
        }

        if (number < 0 || number > 100) {
            throw new IllegalArgumentException("Поле \"" + fieldName + "\" повинно бути в межах від 0 до 100.");
        }

        return number;
    }
}