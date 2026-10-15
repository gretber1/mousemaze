package ru.spbstu.mousemaze.rl;

import ru.spbstu.mousemaze.model.Direction;
import ru.spbstu.mousemaze.model.Maze;
import ru.spbstu.mousemaze.model.Position;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Связывает среду и мышь и ведёт счётчики эпох. Окно общается только с этим классом.
 * Эпоха = один проход мыши от старта до сыра (или до лимита шагов).
 */
public class Simulation {
    private final Maze maze;
    private final Environment env;
    private final QLearningAgent agent;
    private final Mouse mouse;
    private final List<Position> trail = new ArrayList<>();

    private boolean inEpoch;
    private int epochsDone;
    private int stepsInEpoch;
    private double lastReward;
    private double bestReward = Double.NEGATIVE_INFINITY;
    private boolean lastReachedCheese;

    public Simulation(Maze maze, RewardConfig rewards, LearningParams params) {
        this.maze = maze;
        this.env = new Environment(maze, rewards, Environment.defaultMaxSteps(maze));
        this.agent = new QLearningAgent(params);
        this.mouse = new Mouse(agent, env.reset());
        trail.add(maze.start());
    }

    /** Один шаг мыши с обучением. Если эпоха закончилась, следующий вызов начнёт новую. */
    public void step() {
        if (!inEpoch) {
            beginEpoch();
        }
        advance(true);
    }

    /** Довести текущую эпоху (или начать новую) до конца. */
    public void runEpoch() {
        if (!inEpoch) {
            beginEpoch();
        }
        while (inEpoch) {
            advance(true);
        }
    }

    public void runEpochs(int n) {
        for (int i = 0; i < n; i++) {
            runEpoch();
        }
    }

    /** Один проход по лучшему известному пути, без случайных ходов и без обучения. */
    public void runGreedy() {
        beginEpoch();
        while (inEpoch) {
            advance(false);
        }
    }

    private void beginEpoch() {
        mouse.placeAt(env.reset());
        trail.clear();
        trail.add(maze.start());
        stepsInEpoch = 0;
        inEpoch = true;
    }

    private void advance(boolean learn) {
        Direction action = learn ? mouse.chooseAction() : mouse.chooseBest();
        StepResult result = env.step(action);
        if (learn) {
            mouse.learnFrom(action, result);
        } else {
            mouse.observe(result);
        }
        stepsInEpoch++;
        trail.add(new Position(result.next().x(), result.next().y()));
        if (result.done()) {
            inEpoch = false;
            lastReward = mouse.totalReward();
            lastReachedCheese = result.cheeseReached();
            if (lastReward > bestReward) {
                bestReward = lastReward;
            }
            if (learn) {
                epochsDone++;
                agent.decayEpsilon();
            }
        }
    }

    public Maze maze() {
        return maze;
    }

    public Position mousePosition() {
        return new Position(mouse.state().x(), mouse.state().y());
    }

    public List<Position> trail() {
        return Collections.unmodifiableList(trail);
    }

    public int epochsDone() {
        return epochsDone;
    }

    public int stepsInEpoch() {
        return stepsInEpoch;
    }

    public double currentReward() {
        return mouse.totalReward();
    }

    public double lastReward() {
        return lastReward;
    }

    public boolean hasBestReward() {
        return bestReward != Double.NEGATIVE_INFINITY;
    }

    public double bestReward() {
        return bestReward;
    }

    public boolean lastReachedCheese() {
        return lastReachedCheese;
    }

    public double maxPossibleReward() {
        return env.maxPossibleReward();
    }

    public double epsilon() {
        return agent.epsilon();
    }

    public int knownStates() {
        return agent.knownStates();
    }
}
