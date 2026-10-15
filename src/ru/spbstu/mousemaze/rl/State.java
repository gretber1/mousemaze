package ru.spbstu.mousemaze.rl;

/**
 * Состояние для Q-таблицы: где мышь и какую воду она уже выпила.
 * Бит номер i в waterMask равен 1, если i-я капля воды выпита.
 * record сам создаёт equals/hashCode, поэтому State можно использовать как ключ HashMap.
 */
public record State(int x, int y, long waterMask) {
}
