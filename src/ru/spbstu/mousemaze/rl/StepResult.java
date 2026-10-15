package ru.spbstu.mousemaze.rl;

/**
 * Ответ среды на действие мыши: куда попала мышь, какую награду получила
 * и закончилась ли эпоха.
 */
public record StepResult(State next, double reward, boolean cheeseReached, boolean timeUp) {

    public boolean done() {
        return cheeseReached || timeUp;
    }
}
