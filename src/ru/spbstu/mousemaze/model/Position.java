package ru.spbstu.mousemaze.model;

/** Координаты клетки. record - неизменяемый объект-значение (Java 16+). */
public record Position(int x, int y) {
}
