package ru.spbstu.mousemaze.view;

import ru.spbstu.mousemaze.controller.AppController;
import ru.spbstu.mousemaze.generator.GenerationParams;
import ru.spbstu.mousemaze.model.Maze;
import ru.spbstu.mousemaze.rl.LearningParams;
import ru.spbstu.mousemaze.rl.RewardConfig;
import ru.spbstu.mousemaze.rl.Simulation;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Rectangle;
import javax.swing.Scrollable;
import javax.swing.ScrollPaneConstants;

/**
 * Главное окно: поле с лабиринтом, настройки, кнопки и статистика.
 * Окно ничего не решает само: на нажатия кнопок вызывает методы AppController.
 */
public class MainFrame extends JFrame {
    private final MazePanel mazePanel = new MazePanel();

    private final JSpinner widthSp = intSpinner(10, 2, 80);
    private final JSpinner heightSp = intSpinner(10, 2, 80);
    private final JSpinner loopsSp = intSpinner(6, 0, 1000);
    private final JSpinner waterSp = intSpinner(4, 0, 12);
    private final JSpinner shockSp = intSpinner(5, 0, 200);

    private final JSpinner zSp = doubleSpinner(200, 1, 100000, 10);
    private final JSpinner xSp = doubleSpinner(10, 0, 100000, 1);
    private final JSpinner ySp = doubleSpinner(20, 0, 100000, 1);
    private final JSpinner stepSp = doubleSpinner(1, 0, 1000, 0.5);
    private final JSpinner wallSp = doubleSpinner(5, 0, 1000, 0.5);
    private final JSpinner alphaSp = doubleSpinner(0.3, 0.01, 1, 0.05);
    private final JSpinner gammaSp = doubleSpinner(0.97, 0, 1, 0.01);

    private final JToggleButton runButton = new JToggleButton("Старт");
    private final JSpinner epochsSp = intSpinner(100, 1, 1_000_000);

    private final JLabel epochsLabel = new JLabel();
    private final JLabel stepsLabel = new JLabel();
    private final JLabel currentLabel = new JLabel();
    private final JLabel lastLabel = new JLabel();
    private final JLabel bestLabel = new JLabel();
    private final JLabel maxLabel = new JLabel();
    private final JLabel epsilonLabel = new JLabel();
    private final JLabel statesLabel = new JLabel();
    private final JLabel statusLabel = new JLabel(" ");

    public MainFrame(AppController controller) {
        super("Мышь в лабиринте: обучение с подкреплением (Q-learning)");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        mazePanel.setOnMazeEdited(controller::onMazeEdited);

        JPanel side = new SidePanel();
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        // --- управление
        runButton.addActionListener(e -> {
            runButton.setText(runButton.isSelected() ? "Пауза" : "Старт");
            controller.onToggleRun(runButton.isSelected());
        });
        JButton stepBtn = new JButton("Шаг");
        stepBtn.addActionListener(e -> controller.onStep());
        JButton epochBtn = new JButton("Эпоха");
        epochBtn.addActionListener(e -> controller.onEpoch());
        JButton trainBtn = new JButton("Обучить N эпох");
        trainBtn.addActionListener(e -> controller.onTrain(((Number) epochsSp.getValue()).intValue()));
        JButton greedyBtn = new JButton("Показать лучший путь");
        greedyBtn.addActionListener(e -> controller.onGreedy());
        JSlider delaySlider = new JSlider(1, 500, 100);
        delaySlider.addChangeListener(e -> controller.onDelayChanged(delaySlider.getValue()));

        side.add(group("Управление",
                runButton, stepBtn,
                epochBtn, greedyBtn,
                new JLabel("N эпох:"), epochsSp,
                trainBtn, new JLabel("")));
        side.add(group("Задержка шага, мс (меньше = быстрее)", delaySlider));

        // --- статистика
        side.add(column("Статистика", epochsLabel, stepsLabel, currentLabel, lastLabel,
                bestLabel, maxLabel, epsilonLabel, statesLabel));

        // --- лабиринт
        JButton generateBtn = new JButton("Сгенерировать лабиринт");
        generateBtn.addActionListener(e -> controller.onGenerate());
        side.add(group("Лабиринт",
                new JLabel("Ширина"), widthSp,
                new JLabel("Высота"), heightSp,
                new JLabel("Лишних проходов (петли)"), loopsSp,
                new JLabel("Капель воды"), waterSp,
                new JLabel("Молний"), shockSp));
        side.add(wide(generateBtn));

        // --- редактор
        JComboBox<String> toolBox = new JComboBox<>(new String[]{
                "Стена (клик у края клетки)", "Вода", "Молния", "Очистить клетку"});
        toolBox.addActionListener(e -> mazePanel.setTool(MazePanel.Tool.values()[toolBox.getSelectedIndex()]));
        side.add(group("Редактор (клик по лабиринту)", toolBox));

        // --- награды и обучение
        JButton applyBtn = new JButton("Применить и сбросить обучение");
        applyBtn.addActionListener(e -> controller.onApplyParams());
        side.add(group("Награды и обучение",
                new JLabel("Сыр +Z"), zSp,
                new JLabel("Вода +x"), xSp,
                new JLabel("Молния -y"), ySp,
                new JLabel("Штраф за шаг"), stepSp,
                new JLabel("Штраф за стену"), wallSp,
                new JLabel("Скорость обучения alpha"), alphaSp,
                new JLabel("Дисконтирование gamma"), gammaSp));
        side.add(wide(applyBtn));

        statusLabel.setForeground(new Color(180, 40, 40));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        JScrollPane scroll = new JScrollPane(side);
        scroll.setPreferredSize(new Dimension(390, 700));
        scroll.setBorder(null);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        add(mazePanel, BorderLayout.CENTER);
        add(scroll, BorderLayout.EAST);
        add(statusLabel, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(null);
    }

    // ------------------------------------------------- чтение настроек из окна

    public GenerationParams readGeneration() {
        return new GenerationParams(intOf(widthSp), intOf(heightSp), intOf(loopsSp), intOf(waterSp), intOf(shockSp));
    }

    /** Штрафы вводятся положительными числами, а в RewardConfig уходят со знаком минус. */
    public RewardConfig readRewards() {
        return new RewardConfig(doubleOf(zSp), doubleOf(xSp), -doubleOf(ySp), -doubleOf(stepSp), -doubleOf(wallSp));
    }

    public LearningParams readLearning() {
        LearningParams def = LearningParams.defaults();
        return new LearningParams(doubleOf(alphaSp), doubleOf(gammaSp),
                def.epsilonStart(), def.epsilonMin(), def.epsilonDecay());
    }

    // ------------------------------------------------------- обновление экрана

    public void setModel(Maze maze, Simulation sim) {
        mazePanel.setModel(maze, sim);
        refresh(sim);
    }

    public void refresh(Simulation s) {
        epochsLabel.setText("Завершено эпох: " + s.epochsDone());
        stepsLabel.setText("Шагов в эпохе: " + s.stepsInEpoch());
        currentLabel.setText(String.format("Сумма награды: %.0f", s.currentReward()));
        lastLabel.setText(s.epochsDone() == 0 && s.stepsInEpoch() == 0
                ? "Последняя эпоха: -"
                : String.format("Последняя эпоха: %.0f (%s)", s.lastReward(),
                s.lastReachedCheese() ? "сыр" : "без сыра"));
        bestLabel.setText(s.hasBestReward()
                ? String.format("Лучшая награда: %.0f", s.bestReward()) : "Лучшая награда: -");
        maxLabel.setText(String.format("Максимум Z + N*x: %.0f", s.maxPossibleReward()));
        epsilonLabel.setText(String.format("Случайные ходы eps: %.3f", s.epsilon()));
        statesLabel.setText("Состояний в Q: " + s.knownStates());
        mazePanel.repaint();
    }

    public void setRunning(boolean running) {
        runButton.setSelected(running);
        runButton.setText(running ? "Пауза" : "Старт");
    }

    public void setStatus(String text) {
        statusLabel.setText(text.isEmpty() ? " " : text);
    }

    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Ошибка", JOptionPane.ERROR_MESSAGE);
    }

    // ------------------------------------------------------------ мелочи сборки

    private static JSpinner intSpinner(int value, int min, int max) {
        return new JSpinner(new SpinnerNumberModel(value, min, max, 1));
    }

    private static JSpinner doubleSpinner(double value, double min, double max, double step) {
        return new JSpinner(new SpinnerNumberModel(value, min, max, step));
    }

    private static int intOf(JSpinner s) {
        return ((Number) s.getValue()).intValue();
    }

    private static double doubleOf(JSpinner s) {
        return ((Number) s.getValue()).doubleValue();
    }

    private static JPanel group(String title, Component... parts) {
        JPanel panel = new JPanel(new GridLayout(0, parts.length == 1 ? 1 : 2, 6, 3));
        panel.setBorder(BorderFactory.createTitledBorder(title));
        for (Component part : parts) {
            panel.add(part);
        }
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        return panel;
    }

    /** Группа элементов в одну колонку (для длинных надписей). */
    private static JPanel column(String title, Component... parts) {
        JPanel panel = new JPanel(new GridLayout(0, 1, 6, 3));
        panel.setBorder(BorderFactory.createTitledBorder(title));
        for (Component part : parts) {
            panel.add(part);
        }
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        return panel;
    }

    /**
     * Боковая панель, ширина которой всегда равна ширине окна прокрутки.
     * Без этого длинные подписи расширяли панель, и справа появлялась прокрутка.
     */
    private static class SidePanel extends JPanel implements Scrollable {
        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visible, int orientation, int direction) {
            return 16;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visible, int orientation, int direction) {
            return visible.height - 16;
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }

    private static JPanel wide(JButton button) {
        JPanel panel = new JPanel(new GridLayout(1, 1));
        panel.add(button);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        return panel;
    }
}
