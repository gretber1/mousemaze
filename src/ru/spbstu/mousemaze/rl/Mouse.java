package ru.spbstu.mousemaze.rl;

import ru.spbstu.mousemaze.model.Direction;

/**
 * Мышь (агент). Помнит, где она, и ведёт СУММУ выигрыша и проигрыша,
 * как требует задание. Решения принимает через QLearningAgent.
 */
public class Mouse {
    private final QLearningAgent brain;
    private State state;
    private double totalReward;

    public Mouse(QLearningAgent brain, State start) {
        this.brain = brain;
        placeAt(start);
    }

    /** Новая эпоха: мышь на старте, сумма награды обнуляется. */
    public void placeAt(State start) {
        this.state = start;
        this.totalReward = 0;
    }

    /** Выбор направления с исследованием (во время обучения). */
    public Direction chooseAction() {
        return brain.chooseAction(state);
    }

    /** Выбор лучшего известного направления (показ результата обучения). */
    public Direction chooseBest() {
        return brain.bestAction(state);
    }

    /** Получила подкрепление от среды: учимся и копим сумму. */
    public void learnFrom(Direction action, StepResult result) {
        brain.update(state, action, result.reward(), result.next(), result.cheeseReached());
        observe(result);
    }

    /** Просто переходим в новое состояние и копим сумму, без обучения. */
    public void observe(StepResult result) {
        totalReward += result.reward();
        state = result.next();
    }

    public State state() {
        return state;
    }

    public double totalReward() {
        return totalReward;
    }
}
