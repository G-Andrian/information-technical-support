package src;

import src.service.ConfigService;
import src.service.LogService;
import src.ui.MainFrame;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        try {
            ConfigService.loadConfig("config.properties");
            LogService.info("Програма запущена.");
        } catch (Exception e) {
            System.out.println("Не вдалося завантажити config.properties. Будуть використані значення за замовчуванням.");
        }

        SwingUtilities.invokeLater(() -> {
            try {
                new MainFrame();
            } catch (Exception e) {
                LogService.error("Помилка запуску інтерфейсу: " + e.getMessage());
                JOptionPane.showMessageDialog(null,
                        "Помилка запуску програми: " + e.getMessage(),
                        "Помилка",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}