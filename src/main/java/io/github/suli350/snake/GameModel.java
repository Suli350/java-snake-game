package io.github.suli350.snake;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * All the game rules, with no Swing code, so the game can be unit tested
 * step by step. The panel calls {@link #step()} on every timer tick.
 */
public class GameModel {

    public static final int FOODS_PER_LEVEL = 5;
    public static final int START_LENGTH = 3;

    private final int width;
    private final int height;
    private final Random random;
    private boolean wrapWalls;

    private final Deque<Cell> snake = new ArrayDeque<>();   // head is first
    private final Set<Cell> occupied = new HashSet<>();
    private final Deque<Direction> pendingTurns = new ArrayDeque<>();
    private Direction direction;
    private Cell food;
    private int growth;
    private int score;
    private int foodsEaten;
    private GameState state;

    public GameModel(int width, int height, Random random) {
        if (width < 8 || height < 8) {
            throw new IllegalArgumentException("Board must be at least 8x8");
        }
        this.width = width;
        this.height = height;
        this.random = random;
        reset();
    }

    public final void reset() {
        snake.clear();
        occupied.clear();
        pendingTurns.clear();
        int cx = width / 2;
        int cy = height / 2;
        for (int i = 0; i < START_LENGTH; i++) {
            Cell c = new Cell(cx - i, cy);
            snake.addLast(c);
            occupied.add(c);
        }
        direction = Direction.RIGHT;
        growth = 0;
        score = 0;
        foodsEaten = 0;
        state = GameState.READY;
        spawnFood();
    }

    /**
     * Queue a turn. Up to two turns are buffered so quick key presses like
     * "up, left" within one tick both count. Reversing into yourself is ignored.
     */
    public void turn(Direction d) {
        if (state == GameState.READY) {
            state = GameState.RUNNING;
        }
        Direction last = pendingTurns.isEmpty() ? direction : pendingTurns.peekLast();
        if (d == last || d.isOpposite(last) || pendingTurns.size() >= 2) {
            return;
        }
        pendingTurns.addLast(d);
    }

    public void start() {
        if (state == GameState.READY) {
            state = GameState.RUNNING;
        }
    }

    public void togglePause() {
        if (state == GameState.RUNNING) {
            state = GameState.PAUSED;
        } else if (state == GameState.PAUSED) {
            state = GameState.RUNNING;
        }
    }

    /** Advance one tick. Returns true if food was eaten this step. */
    public boolean step() {
        if (state != GameState.RUNNING) {
            return false;
        }
        if (!pendingTurns.isEmpty()) {
            direction = pendingTurns.pollFirst();
        }
        Cell next = snake.peekFirst().move(direction);
        if (wrapWalls) {
            next = new Cell(Math.floorMod(next.x, width), Math.floorMod(next.y, height));
        } else if (next.x < 0 || next.y < 0 || next.x >= width || next.y >= height) {
            state = GameState.GAME_OVER;
            return false;
        }

        boolean eating = next.equals(food);
        boolean tailMoves = growth == 0 && !eating;
        // Moving into the cell the tail is leaving this tick is allowed.
        if (occupied.contains(next) && !(tailMoves && next.equals(snake.peekLast()))) {
            state = GameState.GAME_OVER;
            return false;
        }

        if (tailMoves) {
            occupied.remove(snake.pollLast());
        } else if (growth > 0) {
            growth--;
        }
        snake.addFirst(next);
        occupied.add(next);

        if (eating) {
            foodsEaten++;
            score += 10 * getLevel();
            spawnFood();
        }
        return eating;
    }

    private void spawnFood() {
        List<Cell> free = new ArrayList<>();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Cell c = new Cell(x, y);
                if (!occupied.contains(c)) {
                    free.add(c);
                }
            }
        }
        if (free.isEmpty()) {
            food = null;
            state = GameState.WON;
            return;
        }
        food = free.get(random.nextInt(free.size()));
    }

    /** Level goes up every {@link #FOODS_PER_LEVEL} foods. */
    public int getLevel() {
        return 1 + foodsEaten / FOODS_PER_LEVEL;
    }

    /** Milliseconds between steps: faster each level, capped at 50 ms. */
    public int getTickDelay() {
        return Math.max(50, 160 - (getLevel() - 1) * 15);
    }

    public List<Cell> getSnake() {
        return Collections.unmodifiableList(new ArrayList<>(snake));
    }

    public Cell getHead() { return snake.peekFirst(); }
    public Cell getFood() { return food; }
    public int getScore() { return score; }
    public int getLength() { return snake.size(); }
    public GameState getState() { return state; }
    public Direction getDirection() { return direction; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public boolean isWrapWalls() { return wrapWalls; }

    public void setWrapWalls(boolean wrapWalls) {
        this.wrapWalls = wrapWalls;
    }

    /** For tests: put the food somewhere specific. */
    void placeFood(Cell cell) {
        this.food = cell;
    }
}
