package ru.spbstu.mousemaze.rl;

import ru.spbstu.mousemaze.model.Direction;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * "Мозг" мыши: Q-таблица и правило обновления
 * Q(s,a) <- Q(s,a) + alpha * (reward + gamma * max Q(s',a') - Q(s,a)).
 * Таблица хранит только те состояния, в которых мышь уже побывала.
 */
public class QLearningAgent {
    private static final Direction[] ACTIONS = Direction.values();

    private final Map<State, double[]> table = new HashMap<>();
    private final LearningParams params;
    private final Random random;
    private double epsilon;

    public QLearningAgent(LearningParams params) {
        this(params, new Random());
    }

    public QLearningAgent(LearningParams params, Random random) {
        this.params = params;
        this.random = random;
        this.epsilon = params.epsilonStart();
    }

    /** Эпсилон-жадный выбор: иногда пробуем случайно, иначе берём лучшее из известного. */
    public Direction chooseAction(State s) {
        if (random.nextDouble() < epsilon) {
            return ACTIONS[random.nextInt(ACTIONS.length)];
        }
        return bestAction(s);
    }

    /** Лучшее известное действие; при равенстве оценок выбираем случайное из лучших. */
    public Direction bestAction(State s) {
        double[] q = table.get(s);
        if (q == null) {
            return ACTIONS[random.nextInt(ACTIONS.length)];
        }
        double best = max(q);
        int count = 0;
        int chosen = 0;
        for (int i = 0; i < q.length; i++) {
            if (q[i] == best) {
                count++;
                if (random.nextInt(count) == 0) {
                    chosen = i;
                }
            }
        }
        return ACTIONS[chosen];
    }

    /** Один шаг обучения по формуле Беллмана. */
    public void update(State s, Direction action, double reward, State next, boolean terminal) {
        double[] q = table.computeIfAbsent(s, k -> new double[ACTIONS.length]);
        double future = 0;
        if (!terminal) {
            double[] nextQ = table.get(next);
            future = (nextQ == null) ? 0 : max(nextQ);
        }
        int a = action.ordinal();
        q[a] += params.alpha() * (reward + params.gamma() * future - q[a]);
    }

    /** Вызывается после каждой эпохи: мышь всё меньше пробует наугад. */
    public void decayEpsilon() {
        epsilon = Math.max(params.epsilonMin(), epsilon * params.epsilonDecay());
    }

    public double epsilon() {
        return epsilon;
    }

    public int knownStates() {
        return table.size();
    }

    private static double max(double[] values) {
        double m = values[0];
        for (double v : values) {
            if (v > m) {
                m = v;
            }
        }
        return m;
    }
}
