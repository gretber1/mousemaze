package ru.spbstu.mousemaze.model;

/** Что лежит в клетке. Стены сюда НЕ входят: они тонкие и живут между клетками. */
public enum CellType {
    EMPTY,
    WATER,
    SHOCK,
    CHEESE
}
