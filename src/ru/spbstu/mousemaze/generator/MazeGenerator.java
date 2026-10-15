package ru.spbstu.mousemaze.generator;

import ru.spbstu.mousemaze.model.Maze;

/** Интерфейс генератора лабиринтов: можно подставить любой алгоритм (полиморфизм). */
public interface MazeGenerator {
    Maze generate(GenerationParams params);
}
