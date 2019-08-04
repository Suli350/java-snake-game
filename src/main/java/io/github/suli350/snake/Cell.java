package io.github.suli350.snake;

/** Immutable grid coordinate. */
public final class Cell {

    public final int x;
    public final int y;

    public Cell(int x, int y) {
        this.x = x;
        this.y = y;
    }

    Cell move(Direction d) {
        return new Cell(x + d.dx, y + d.dy);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Cell)) {
            return false;
        }
        Cell c = (Cell) o;
        return c.x == x && c.y == y;
    }

    @Override
    public int hashCode() {
        return 31 * x + y;
    }

    @Override
    public String toString() {
        return "(" + x + "," + y + ")";
    }
}
