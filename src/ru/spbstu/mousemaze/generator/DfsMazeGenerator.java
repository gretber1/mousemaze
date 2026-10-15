package ru.spbstu.mousemaze.generator;

import ru.spbstu.mousemaze.model.CellType;
import ru.spbstu.mousemaze.model.Direction;
import ru.spbstu.mousemaze.model.Maze;
import ru.spbstu.mousemaze.model.Position;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Random;

/**
 * Генерация лабиринта поиском в глубину (DFS с откатом).
 * 1) все стены стоят; 2) идём из (0,0) в случайного непосещённого соседа, убирая стену;
 * 3) если соседей нет, откатываемся назад; 4) так проходим все клетки: получается
 * "идеальный" лабиринт, где от любой клетки до любой ведёт ровно один путь.
 * Затем пробиваем несколько лишних стен, чтобы появились петли и обходные пути,
 * и раскладываем воду и молнии.
 *
 * Вместо рекурсии используется явный стек, поэтому большие лабиринты не вызовут StackOverflowError.
 */
public class DfsMazeGenerator implements MazeGenerator {
    private final Random random;

    public DfsMazeGenerator() {
        this(new Random());
    }

    public DfsMazeGenerator(Random random) {
        this.random = random;
    }

    @Override
    public Maze generate(GenerationParams p) {
        if (p.extraOpenings() < 0 || p.waterCount() < 0 || p.shockCount() < 0) {
            throw new IllegalArgumentException("Количества не могут быть отрицательными");
        }
        Maze maze = new Maze(p.width(), p.height());   // сам проверит размер >= 2x2
        int freeCells = p.width() * p.height() - 2;    // без старта и сыра
        if (p.waterCount() + p.shockCount() > freeCells) {
            throw new IllegalArgumentException(
                    "Воды и молний слишком много: в лабиринте только " + freeCells + " свободных клеток");
        }
        if (p.waterCount() > 62) {
            throw new IllegalArgumentException("Воды слишком много (максимум 62 капли)");
        }
        carvePassages(maze);
        openExtraWalls(maze, p.extraOpenings());
        placeItems(maze, p.waterCount(), p.shockCount());
        return maze;
    }

    private void carvePassages(Maze maze) {
        boolean[][] visited = new boolean[maze.width()][maze.height()];
        Deque<Position> stack = new ArrayDeque<>();
        stack.push(maze.start());
        visited[0][0] = true;
        while (!stack.isEmpty()) {
            Position cur = stack.peek();
            List<Direction> options = new ArrayList<>();
            for (Direction d : Direction.values()) {
                int nx = cur.x() + d.dx();
                int ny = cur.y() + d.dy();
                if (maze.isInside(nx, ny) && !visited[nx][ny]) {
                    options.add(d);
                }
            }
            if (options.isEmpty()) {
                stack.pop();                              // откат
            } else {
                Direction d = options.get(random.nextInt(options.size()));
                maze.setWall(cur.x(), cur.y(), d, false); // прорубаем проход
                int nx = cur.x() + d.dx();
                int ny = cur.y() + d.dy();
                visited[nx][ny] = true;
                stack.push(new Position(nx, ny));
            }
        }
    }

    private void openExtraWalls(Maze maze, int count) {
        List<int[]> walls = new ArrayList<>();            // {x, y, номер направления}
        for (int x = 0; x < maze.width(); x++) {
            for (int y = 0; y < maze.height(); y++) {
                if (x + 1 < maze.width() && maze.hasWall(x, y, Direction.RIGHT)) {
                    walls.add(new int[]{x, y, Direction.RIGHT.ordinal()});
                }
                if (y + 1 < maze.height() && maze.hasWall(x, y, Direction.UP)) {
                    walls.add(new int[]{x, y, Direction.UP.ordinal()});
                }
            }
        }
        Collections.shuffle(walls, random);
        for (int i = 0; i < Math.min(count, walls.size()); i++) {
            int[] w = walls.get(i);
            maze.setWall(w[0], w[1], Direction.values()[w[2]], false);
        }
    }

    private void placeItems(Maze maze, int water, int shocks) {
        List<Position> cells = new ArrayList<>();
        for (int x = 0; x < maze.width(); x++) {
            for (int y = 0; y < maze.height(); y++) {
                if (!maze.isStart(x, y) && !maze.isCheese(x, y)) {
                    cells.add(new Position(x, y));
                }
            }
        }
        Collections.shuffle(cells, random);
        for (int i = 0; i < water + shocks; i++) {
            Position c = cells.get(i);
            maze.setItem(c.x(), c.y(), i < water ? CellType.WATER : CellType.SHOCK);
        }
    }
}
