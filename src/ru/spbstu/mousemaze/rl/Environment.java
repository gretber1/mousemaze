package ru.spbstu.mousemaze.rl;

import ru.spbstu.mousemaze.model.CellType;
import ru.spbstu.mousemaze.model.Direction;
import ru.spbstu.mousemaze.model.Maze;

/**
 * Среда: знает правила лабиринта и по действию мыши возвращает награду (подкрепление).
 * Мышь не видит содержимое среды, она получает только StepResult.
 */
public class Environment {
    private final Maze maze;
    private final RewardConfig rewards;
    private final int maxSteps;
    /** waterIndex[x][y] - номер капли воды в клетке или -1, если воды нет. */
    private final int[][] waterIndex;
    private final int waterCount;

    private int x;
    private int y;
    private long waterMask;
    private int steps;

    public Environment(Maze maze, RewardConfig rewards, int maxSteps) {
        if (maxSteps <= 0) {
            throw new IllegalArgumentException("Лимит шагов должен быть положительным");
        }
        this.maze = maze;
        this.rewards = rewards;
        this.maxSteps = maxSteps;
        this.waterIndex = new int[maze.width()][maze.height()];
        int n = 0;
        for (int cx = 0; cx < maze.width(); cx++) {
            for (int cy = 0; cy < maze.height(); cy++) {
                waterIndex[cx][cy] = -1;
                if (maze.getItem(cx, cy) == CellType.WATER) {
                    waterIndex[cx][cy] = n++;
                }
            }
        }
        if (n > 62) {
            throw new IllegalArgumentException("Слишком много воды (максимум 62 капли)");
        }
        this.waterCount = n;
        reset();
    }

    /** Лимит шагов в эпохе по умолчанию: пропорционален размеру лабиринта. */
    public static int defaultMaxSteps(Maze maze) {
        return 6 * maze.width() * maze.height();
    }

    /** Начало новой эпохи: мышь возвращается в (0, 0), вся вода снова на месте. */
    public State reset() {
        x = 0;
        y = 0;
        waterMask = 0;
        steps = 0;
        return state();
    }

    public StepResult step(Direction action) {
        steps++;
        double reward = rewards.step();
        boolean cheese = false;

        if (maze.hasWall(x, y, action)) {
            reward += rewards.wall();           // стена: остаёмся на месте
        } else {
            x += action.dx();
            y += action.dy();
            switch (maze.getItem(x, y)) {
                case WATER -> {
                    long bit = 1L << waterIndex[x][y];
                    if ((waterMask & bit) == 0) {  // вода одноразовая
                        waterMask |= bit;
                        reward += rewards.water();
                    }
                }
                case SHOCK -> reward += rewards.shock();
                case CHEESE -> {
                    reward += rewards.cheese();
                    cheese = true;
                }
                case EMPTY -> { }
            }
        }
        boolean timeUp = !cheese && steps >= maxSteps;
        return new StepResult(state(), reward, cheese, timeUp);
    }

    /** Теоретический максимум без учёта штрафов за шаги: Z + (число капель) * x. */
    public double maxPossibleReward() {
        return rewards.cheese() + waterCount * rewards.water();
    }

    public int waterCount() {
        return waterCount;
    }

    private State state() {
        return new State(x, y, waterMask);
    }
}
