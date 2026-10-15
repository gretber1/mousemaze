package ru.spbstu.mousemaze.rl;

/**
 * Награды среды. Все числа со знаком: награды положительные, штрафы отрицательные.
 *
 * @param cheese  +Z, сыр
 * @param water   +x, вода
 * @param shock   -y, электротравма
 * @param step    штраф за каждый шаг (подталкивает искать короткий путь)
 * @param wall    штраф за удар о стену (мышь остаётся на месте)
 */
public record RewardConfig(double cheese, double water, double shock, double step, double wall) {

    /** Компактный конструктор record: проверяет данные при создании объекта. */
    public RewardConfig {
        if (cheese <= 0) {
            throw new IllegalArgumentException("Награда за сыр Z должна быть положительной");
        }
        if (water < 0 || water >= cheese) {
            throw new IllegalArgumentException("Награда за воду x должна быть от 0 и меньше награды за сыр Z");
        }
        if (shock > 0 || step > 0 || wall > 0) {
            throw new IllegalArgumentException("Штрафы (y, шаг, стена) задаются неположительными числами");
        }
    }

    public static RewardConfig defaults() {
        return new RewardConfig(200, 10, -20, -1, -5);
    }
}
