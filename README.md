# Snake (Java / Swing)

The classic Snake game in pure Java: smooth keyboard controls, increasing
speed, an optional wrap-around mode and a saved top-10 high-score table.

## Controls

| Key | Action |
|---|---|
| Arrow keys / WASD | Move (the first key starts the game) |
| Space / P | Pause |
| Enter / R | Play again after game over |
| M | Toggle solid walls / wrap-around (before starting) |
| H | Show high scores |

## Features

- Level up every 5 foods; each level is faster and food is worth more
- Turn buffering: two quick key presses in one tick both count
- You can't reverse into yourself, and chasing your own tail is allowed
- Food never spawns on the snake; filling the board wins the game
- Top-10 high scores with names and dates in `~/.snake-game/highscores.txt`

## Design

`GameModel` holds all the rules and has no Swing code. `GamePanel` only draws the
model and calls `step()` from a `javax.swing.Timer`. This keeps the game logic
fully unit-testable (see `GameModelTest`).

```
Main ─ JFrame ─ GamePanel (Timer, key bindings, painting)
                   │
                   ├── GameModel (snake, food, collisions, levels)
                   └── HighScores (file-backed top 10)
```

## Build and run

```bash
mvn test
mvn package
java -jar target/snake-game-1.0.0.jar
```

## Ideas for extending it

- Obstacles / maze levels
- Bonus food that disappears after a few seconds
- Two-player mode on the same keyboard
