package ru.spbstu.mousemaze.generator;

/**
 * Что просит пользователь: размер лабиринта, число лишних проходов (петель),
 * число капель воды и число молний.
 */
public record GenerationParams(int width, int height, int extraOpenings, int waterCount, int shockCount) {
}
