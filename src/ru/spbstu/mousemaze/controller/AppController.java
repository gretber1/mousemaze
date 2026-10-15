package ru.spbstu.mousemaze.controller;

import ru.spbstu.mousemaze.generator.DfsMazeGenerator;
import ru.spbstu.mousemaze.generator.MazeGenerator;
import ru.spbstu.mousemaze.model.Maze;
import ru.spbstu.mousemaze.rl.Simulation;
import ru.spbstu.mousemaze.view.MainFrame;

import javax.swing.Timer;

/**
 * Контроллер: связывает окно (view) с логикой (model, rl, generator).
 * Окно сообщает "нажали кнопку", контроллер решает, что делать, и просит окно обновиться.
 */
public class AppController {
    private final MazeGenerator generator = new DfsMazeGenerator();
    private final MainFrame view;
    private final Timer timer;
    private Maze maze;
    private Simulation sim;

    public AppController() {
        view = new MainFrame(this);
        timer = new Timer(100, e -> onTick());
        onGenerate();
        view.setVisible(true);
    }

    /** Новый случайный лабиринт по настройкам из окна. Обучение начинается заново. */
    public void onGenerate() {
        stopRun();
        try {
            maze = generator.generate(view.readGeneration());
        } catch (IllegalArgumentException ex) {
            view.showError(ex.getMessage());
            return;
        }
        rebuildSimulation();
    }

    /** Применить награды и параметры обучения: Q-таблица обнуляется. */
    public void onApplyParams() {
        stopRun();
        rebuildSimulation();
    }

    /** Пользователь изменил лабиринт кликом: старые знания мыши недействительны. */
    public void onMazeEdited() {
        stopRun();
        rebuildSimulation();
    }

    public void onToggleRun(boolean on) {
        if (sim == null) {
            view.setRunning(false);
            return;
        }
        if (on) {
            timer.start();
        } else {
            timer.stop();
        }
    }

    public void onDelayChanged(int millis) {
        timer.setDelay(millis);
    }

    public void onStep() {
        if (sim == null) {
            return;
        }
        stopRun();
        sim.step();
        view.refresh(sim);
    }

    public void onEpoch() {
        if (sim == null) {
            return;
        }
        stopRun();
        sim.runEpoch();
        view.refresh(sim);
    }

    /** Быстрое обучение без анимации: прогоняет n эпох подряд. */
    public void onTrain(int epochs) {
        if (sim == null) {
            return;
        }
        stopRun();
        sim.runEpochs(epochs);
        view.refresh(sim);
    }

    public void onGreedy() {
        if (sim == null) {
            return;
        }
        stopRun();
        sim.runGreedy();
        view.refresh(sim);
    }

    private void onTick() {
        sim.step();
        view.refresh(sim);
    }

    private void stopRun() {
        timer.stop();
        view.setRunning(false);
    }

    private void rebuildSimulation() {
        try {
            sim = new Simulation(maze, view.readRewards(), view.readLearning());
        } catch (IllegalArgumentException ex) {
            view.showError(ex.getMessage());
            return;
        }
        view.setModel(maze, sim);
        view.setStatus(maze.isCheeseReachable()
                ? "" : "Внимание: сыр недостижим. Уберите лишние стены кликом.");
    }
}
