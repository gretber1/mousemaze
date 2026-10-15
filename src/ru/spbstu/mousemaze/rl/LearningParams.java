package ru.spbstu.mousemaze.rl;

/**
 * Параметры Q-learning.
 *
 * @param alpha         скорость обучения (0; 1]
 * @param gamma         коэффициент дисконтирования [0; 1]
 * @param epsilonStart  начальная доля случайных ходов (исследование)
 * @param epsilonMin    нижняя граница доли случайных ходов
 * @param epsilonDecay  множитель, на который epsilon умножается после каждой эпохи
 */
public record LearningParams(double alpha, double gamma,
                             double epsilonStart, double epsilonMin, double epsilonDecay) {

    public LearningParams {
        if (alpha <= 0 || alpha > 1) {
            throw new IllegalArgumentException("alpha должна быть в диапазоне (0; 1]");
        }
        if (gamma < 0 || gamma > 1) {
            throw new IllegalArgumentException("gamma должна быть в диапазоне [0; 1]");
        }
        if (epsilonStart < 0 || epsilonStart > 1 || epsilonMin < 0 || epsilonMin > epsilonStart) {
            throw new IllegalArgumentException("Нужно: 0 <= epsilonMin <= epsilonStart <= 1");
        }
        if (epsilonDecay <= 0 || epsilonDecay > 1) {
            throw new IllegalArgumentException("epsilonDecay должна быть в диапазоне (0; 1]");
        }
    }

    public static LearningParams defaults() {
        return new LearningParams(0.3, 0.97, 1.0, 0.02, 0.995);
    }
}
