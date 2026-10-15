package ru.spbstu.mousemaze.view;

import ru.spbstu.mousemaze.model.CellType;
import ru.spbstu.mousemaze.model.Direction;
import ru.spbstu.mousemaze.model.Maze;
import ru.spbstu.mousemaze.model.Position;
import ru.spbstu.mousemaze.rl.Simulation;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.util.List;

/**
 * Панель, которая рисует лабиринт (метод paintComponent) и принимает клики для редактирования.
 * Рисует только то, что ей дали: сама ничего не считает и не знает про Q-learning.
 */
public class MazePanel extends JPanel {

    /** Чем щёлкаем по лабиринту. */
    public enum Tool { WALL, WATER, SHOCK, CLEAR }

    private static final Color BACKGROUND = new Color(30, 26, 60);
    private static final Color WALL_COLOR = new Color(150, 70, 230);

    private Maze maze;
    private Simulation simulation;
    private Tool tool = Tool.WALL;
    private Runnable onMazeEdited = () -> { };

    public MazePanel() {
        setPreferredSize(new Dimension(660, 660));
        setBackground(BACKGROUND);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                handleClick(e.getX(), e.getY());
            }
        });
    }

    public void setModel(Maze maze, Simulation simulation) {
        this.maze = maze;
        this.simulation = simulation;
        repaint();
    }

    public void setTool(Tool tool) {
        this.tool = tool;
    }

    public void setOnMazeEdited(Runnable onMazeEdited) {
        this.onMazeEdited = onMazeEdited;
    }

    /** Размер клетки и отступы так, чтобы лабиринт любого размера помещался в панель. */
    private record Geometry(int originX, int originY, int cell) { }

    private Geometry geometry() {
        int margin = 12;
        int cell = Math.min((getWidth() - 2 * margin) / maze.width(),
                            (getHeight() - 2 * margin) / maze.height());
        cell = Math.max(cell, 4);
        int ox = (getWidth() - cell * maze.width()) / 2;
        int oy = (getHeight() - cell * maze.height()) / 2;
        return new Geometry(ox, oy, cell);
    }

    // ---------------------------------------------------------------- рисование

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        if (maze == null) {
            return;
        }
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Geometry geo = geometry();

        drawTrail(g, geo);
        drawItems(g, geo);
        drawWalls(g, geo);
        drawMouse(g, geo);
        g.dispose();
    }

    private int left(Geometry geo, int x) {
        return geo.originX() + x * geo.cell();
    }

    /** Ось Y в модели направлена вверх, а на экране вниз, поэтому строку переворачиваем. */
    private int top(Geometry geo, int y) {
        return geo.originY() + (maze.height() - 1 - y) * geo.cell();
    }

    private void drawTrail(Graphics2D g, Geometry geo) {
        List<Position> trail = simulation.trail();
        if (trail.size() < 2) {
            return;
        }
        int c = geo.cell();
        g.setStroke(new BasicStroke(Math.max(2f, c / 9f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(new Color(255, 255, 255, 70));
        for (int i = 1; i < trail.size(); i++) {
            Position a = trail.get(i - 1);
            Position b = trail.get(i);
            g.drawLine(left(geo, a.x()) + c / 2, top(geo, a.y()) + c / 2,
                       left(geo, b.x()) + c / 2, top(geo, b.y()) + c / 2);
        }
    }

    private void drawItems(Graphics2D g, Geometry geo) {
        for (int x = 0; x < maze.width(); x++) {
            for (int y = 0; y < maze.height(); y++) {
                CellType type = maze.getItem(x, y);
                int l = left(geo, x);
                int t = top(geo, y);
                int c = geo.cell();
                switch (type) {
                    case WATER -> drawWater(g, l, t, c);
                    case SHOCK -> drawShock(g, l, t, c);
                    case CHEESE -> drawCheese(g, l, t, c);
                    case EMPTY -> { }
                }
            }
        }
    }

    private void drawWater(Graphics2D g, int l, int t, int c) {
        g.setColor(new Color(60, 190, 255));
        int r = (int) (c * 0.22);
        int cx = l + c / 2;
        int cy = t + (int) (c * 0.62);
        g.fillOval(cx - r, cy - r, 2 * r, 2 * r);
        g.fillPolygon(new int[]{cx, cx - r, cx + r},
                      new int[]{t + (int) (c * 0.16), cy - (int) (r * 0.5), cy - (int) (r * 0.5)}, 3);
    }

    private void drawShock(Graphics2D g, int l, int t, int c) {
        double[][] p = {{0.58, 0.08}, {0.26, 0.54}, {0.47, 0.54}, {0.38, 0.92}, {0.76, 0.42}, {0.54, 0.42}, {0.68, 0.08}};
        int[] xs = new int[p.length];
        int[] ys = new int[p.length];
        for (int i = 0; i < p.length; i++) {
            xs[i] = l + (int) (c * p[i][0]);
            ys[i] = t + (int) (c * p[i][1]);
        }
        g.setColor(new Color(255, 215, 0));
        g.fillPolygon(xs, ys, p.length);
    }

    private void drawCheese(Graphics2D g, int l, int t, int c) {
        int[] xs = {l + (int) (c * 0.1), l + (int) (c * 0.9), l + (int) (c * 0.9), l + (int) (c * 0.1)};
        int[] ys = {t + (int) (c * 0.8), t + (int) (c * 0.8), t + (int) (c * 0.45), t + (int) (c * 0.2)};
        g.setColor(new Color(255, 190, 40));
        g.fillPolygon(xs, ys, 4);
        g.setColor(new Color(200, 130, 10));
        int r = Math.max(2, c / 12);
        g.fillOval(l + (int) (c * 0.3), t + (int) (c * 0.55), r, r);
        g.fillOval(l + (int) (c * 0.6), t + (int) (c * 0.62), r, r);
    }

    private void drawWalls(Graphics2D g, Geometry geo) {
        g.setStroke(new BasicStroke(Math.max(2f, geo.cell() / 12f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(WALL_COLOR);
        int c = geo.cell();
        for (int x = 0; x < maze.width(); x++) {
            for (int y = 0; y < maze.height(); y++) {
                int l = left(geo, x);
                int t = top(geo, y);
                if (maze.hasWall(x, y, Direction.UP)) {
                    g.drawLine(l, t, l + c, t);
                }
                if (maze.hasWall(x, y, Direction.RIGHT)) {
                    g.drawLine(l + c, t, l + c, t + c);
                }
                if (x == 0) {
                    g.drawLine(l, t, l, t + c);
                }
                if (y == 0) {
                    g.drawLine(l, t + c, l + c, t + c);
                }
            }
        }
    }

    private void drawMouse(Graphics2D g, Geometry geo) {
        Position p = simulation.mousePosition();
        int c = geo.cell();
        double cx = left(geo, p.x()) + c / 2.0;
        double cy = top(geo, p.y()) + c / 2.0;
        double r = c * 0.30;
        g.setColor(new Color(215, 215, 225));
        g.fill(new Ellipse2D.Double(cx - r * 1.1, cy - r * 1.35, r * 0.8, r * 0.8));   // уши
        g.fill(new Ellipse2D.Double(cx + r * 0.3, cy - r * 1.35, r * 0.8, r * 0.8));
        g.fill(new Ellipse2D.Double(cx - r, cy - r * 0.8, 2 * r, 1.8 * r));            // тело
        g.setColor(new Color(40, 30, 50));
        double e = Math.max(2, r * 0.22);
        g.fill(new Ellipse2D.Double(cx - r * 0.45 - e / 2, cy - r * 0.2, e, e));        // глаза
        g.fill(new Ellipse2D.Double(cx + r * 0.45 - e / 2, cy - r * 0.2, e, e));
    }

    // ------------------------------------------------------------- редактирование

    private void handleClick(int px, int py) {
        if (maze == null) {
            return;
        }
        Geometry geo = geometry();
        double fx = (px - geo.originX()) / (double) geo.cell();
        double fy = (py - geo.originY()) / (double) geo.cell();
        if (fx < 0 || fy < 0) {
            return;
        }
        int x = (int) fx;
        int row = (int) fy;
        int y = maze.height() - 1 - row;
        if (!maze.isInside(x, y)) {
            return;
        }
        try {
            switch (tool) {
                case WALL -> toggleNearestWall(x, y, fx - x, fy - row);
                case WATER -> maze.setItem(x, y, CellType.WATER);
                case SHOCK -> maze.setItem(x, y, CellType.SHOCK);
                case CLEAR -> maze.setItem(x, y, CellType.EMPTY);
            }
        } catch (IllegalArgumentException ex) {
            Toolkit.getDefaultToolkit().beep();       // старт, сыр и границу менять нельзя
            return;
        }
        onMazeEdited.run();
        repaint();
    }

    /** Переключает ту стену клетки, ближе к которой щёлкнули. */
    private void toggleNearestWall(int x, int y, double fx, double fy) {
        double dLeft = fx;
        double dRight = 1 - fx;
        double dTop = fy;
        double dBottom = 1 - fy;
        double min = Math.min(Math.min(dLeft, dRight), Math.min(dTop, dBottom));
        Direction d;
        if (min == dTop) {
            d = Direction.UP;
        } else if (min == dBottom) {
            d = Direction.DOWN;
        } else if (min == dLeft) {
            d = Direction.LEFT;
        } else {
            d = Direction.RIGHT;
        }
        maze.setWall(x, y, d, !maze.hasWall(x, y, d));
    }
}
