package ru.spbstu.mousemaze.model;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Лабиринт: сетка клеток width x height с ТОНКИМИ стенами между клетками.
 * Старт всегда в (0, 0), сыр всегда в (width-1, height-1).
 *
 * Инкапсуляция: массивы со стенами и предметами приватные. Снаружи с лабиринтом
 * работают только через методы, которые проверяют корректность.
 */
public class Maze {
    private final int width;
    private final int height;
    private final CellType[][] items;
    /** eastWall[x][y] == true: есть стена между клетками (x, y) и (x+1, y). */
    private final boolean[][] eastWall;
    /** northWall[x][y] == true: есть стена между клетками (x, y) и (x, y+1). */
    private final boolean[][] northWall;

    /** Создаёт лабиринт, где все стены на месте (пока ни одного прохода). */
    public Maze(int width, int height) {
        if (width < 2 || height < 2) {
            throw new IllegalArgumentException("Размер лабиринта должен быть не меньше 2x2");
        }
        this.width = width;
        this.height = height;
        this.items = new CellType[width][height];
        this.eastWall = new boolean[width][height];
        this.northWall = new boolean[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                items[x][y] = CellType.EMPTY;
                eastWall[x][y] = true;
                northWall[x][y] = true;
            }
        }
        items[width - 1][height - 1] = CellType.CHEESE;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public boolean isInside(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    public Position start() {
        return new Position(0, 0);
    }

    public Position cheese() {
        return new Position(width - 1, height - 1);
    }

    public boolean isStart(int x, int y) {
        return x == 0 && y == 0;
    }

    public boolean isCheese(int x, int y) {
        return x == width - 1 && y == height - 1;
    }

    public CellType getItem(int x, int y) {
        checkInside(x, y);
        return items[x][y];
    }

    /** Кладёт в клетку воду, молнию или очищает её. Старт и сыр менять нельзя. */
    public void setItem(int x, int y, CellType type) {
        checkInside(x, y);
        if (isStart(x, y) || isCheese(x, y)) {
            throw new IllegalArgumentException("Стартовую клетку и клетку с сыром менять нельзя");
        }
        if (type == CellType.CHEESE) {
            throw new IllegalArgumentException("Сыр бывает только один и лежит в правом верхнем углу");
        }
        items[x][y] = type;
    }

    /** Есть ли стена на пути из клетки (x, y) в направлении dir. Граница поля - всегда стена. */
    public boolean hasWall(int x, int y, Direction dir) {
        checkInside(x, y);
        int nx = x + dir.dx();
        int ny = y + dir.dy();
        if (!isInside(nx, ny)) {
            return true;
        }
        return switch (dir) {
            case RIGHT -> eastWall[x][y];
            case LEFT -> eastWall[nx][ny];
            case UP -> northWall[x][y];
            case DOWN -> northWall[nx][ny];
        };
    }

    /** Ставит или убирает стену. Одна стена хранится один раз, поэтому соседи не могут разойтись. */
    public void setWall(int x, int y, Direction dir, boolean present) {
        checkInside(x, y);
        int nx = x + dir.dx();
        int ny = y + dir.dy();
        if (!isInside(nx, ny)) {
            throw new IllegalArgumentException("Внешнюю границу лабиринта менять нельзя");
        }
        switch (dir) {
            case RIGHT -> eastWall[x][y] = present;
            case LEFT -> eastWall[nx][ny] = present;
            case UP -> northWall[x][y] = present;
            case DOWN -> northWall[nx][ny] = present;
        }
    }

    public int count(CellType type) {
        int n = 0;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (items[x][y] == type) {
                    n++;
                }
            }
        }
        return n;
    }

    /** Поиск в ширину: можно ли дойти от старта до сыра, не ломая стен. */
    public boolean isCheeseReachable() {
        boolean[][] seen = new boolean[width][height];
        Deque<Position> queue = new ArrayDeque<>();
        queue.add(start());
        seen[0][0] = true;
        while (!queue.isEmpty()) {
            Position p = queue.poll();
            if (isCheese(p.x(), p.y())) {
                return true;
            }
            for (Direction d : Direction.values()) {
                if (!hasWall(p.x(), p.y(), d)) {
                    int nx = p.x() + d.dx();
                    int ny = p.y() + d.dy();
                    if (!seen[nx][ny]) {
                        seen[nx][ny] = true;
                        queue.add(new Position(nx, ny));
                    }
                }
            }
        }
        return false;
    }

    private void checkInside(int x, int y) {
        if (!isInside(x, y)) {
            throw new IllegalArgumentException("Клетка (" + x + ", " + y + ") вне лабиринта");
        }
    }
}
