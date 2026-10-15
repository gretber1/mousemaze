package ru.spbstu.mousemaze;

import ru.spbstu.mousemaze.controller.AppController;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/** Точка входа в программу. */
public class Main {
    public static void main(String[] args) {
        // Swing-компоненты нужно создавать в специальном потоке интерфейса (EDT)
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // не получилось - останется стандартный вид
            }
            new AppController();
        });
    }
}
