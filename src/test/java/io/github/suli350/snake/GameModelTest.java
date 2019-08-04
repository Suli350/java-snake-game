package io.github.suli350.snake;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GameModelTest {

    private GameModel game;

    @BeforeEach
    void setUp() {
        game = new GameModel(10, 10, new Random(42));
        game.placeFood(new Cell(0, 0)); // out of the way
        game.start();
    }

    @Test
    void startsInTheMiddleMovingRight() {
        assertEquals(new Cell(5, 5), game.getHead());
        assertEquals(3, game.getLength());
        game.step();
        assertEquals(new Cell(6, 5), game.getHead());
        assertEquals(3, game.getLength());
    }

    @Test
    void cannotReverseIntoItself() {
        game.turn(Direction.LEFT);
        game.step();
        assertEquals(new Cell(6, 5), game.getHead());
        assertEquals(Direction.RIGHT, game.getDirection());
    }

    @Test
    void twoQuickTurnsAreBuffered() {
        game.turn(Direction.UP);
        game.turn(Direction.LEFT);
        game.step();
        assertEquals(new Cell(5, 4), game.getHead());
        game.step();
        assertEquals(new Cell(4, 4), game.getHead());
    }

    @Test
    void eatingGrowsAndScores() {
        game.placeFood(new Cell(6, 5));
        assertTrue(game.step());
        assertEquals(10, game.getScore());
        assertEquals(4, game.getLength());
        assertNotEquals(new Cell(6, 5), game.getFood());
        game.placeFood(new Cell(0, 0));
        game.step();
        assertEquals(4, game.getLength());
    }

    @Test
    void hittingAWallEndsTheGame() {
        for (int i = 0; i < 4; i++) {
            game.step();
        }
        assertEquals(GameState.RUNNING, game.getState());
        game.step();                        // x would be 10
        assertEquals(GameState.GAME_OVER, game.getState());
    }

    @Test
    void wrapModeGoesThroughWalls() {
        game.setWrapWalls(true);
        for (int i = 0; i < 5; i++) {
            game.step();
        }
        assertEquals(new Cell(0, 5), game.getHead());
        assertEquals(GameState.RUNNING, game.getState());
    }

    @Test
    void runningIntoYourBodyEndsTheGame() {
        // grow to length 5, then turn back on ourselves
        game.placeFood(new Cell(6, 5));
        game.step();
        game.placeFood(new Cell(7, 5));
        game.step();
        game.placeFood(new Cell(0, 0));
        game.step();
        game.turn(Direction.UP);
        game.step();
        game.turn(Direction.LEFT);
        game.step();
        game.turn(Direction.DOWN);
        game.step();
        assertEquals(GameState.GAME_OVER, game.getState());
    }

    @Test
    void chasingYourOwnTailIsAllowed() {
        // length 4 moving in a 2x2 square: head always enters the cell the tail just left
        game.placeFood(new Cell(6, 5));
        game.step();
        game.placeFood(new Cell(0, 0));
        game.step();
        Direction[] loop = {Direction.UP, Direction.LEFT, Direction.DOWN, Direction.RIGHT};
        for (int i = 0; i < 12; i++) {
            game.turn(loop[i % 4]);
            game.step();
            assertEquals(GameState.RUNNING, game.getState(), "step " + i);
        }
    }

    @Test
    void foodNeverSpawnsOnTheSnake() {
        GameModel g = new GameModel(8, 8, new Random(1));
        for (int i = 0; i < 200; i++) {
            g.reset();
            assertFalse(g.getSnake().contains(g.getFood()));
        }
    }

    @Test
    void levelAndSpeedIncrease() {
        assertEquals(1, game.getLevel());
        int slow = game.getTickDelay();
        game.setWrapWalls(true);
        for (int i = 0; i < GameModel.FOODS_PER_LEVEL; i++) {
            Cell ahead = new Cell(Math.floorMod(game.getHead().x + 1, 10), game.getHead().y);
            game.placeFood(ahead);
            game.step();
        }
        assertEquals(2, game.getLevel());
        assertTrue(game.getTickDelay() < slow);
    }

    @Test
    void pauseStopsMovement() {
        game.togglePause();
        game.step();
        assertEquals(new Cell(5, 5), game.getHead());
        game.togglePause();
        game.step();
        assertEquals(new Cell(6, 5), game.getHead());
    }
}
