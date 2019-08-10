package io.github.suli350.snake;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.time.LocalDate;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.InputMap;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.Timer;

/** Draws the model and turns key presses into moves. */
public class GamePanel extends JPanel {

    private static final int CELL = 24;
    private static final int HUD = 36;
    private static final Color BG = new Color(0x16213E);
    private static final Color GRID = new Color(0x1B2A4A);
    private static final Color SNAKE = new Color(0x4ADE80);
    private static final Color HEAD = new Color(0x22C55E);
    private static final Color FOOD = new Color(0xF43F5E);

    private final GameModel model;
    private final HighScores highScores;
    private final Timer timer;
    private boolean scoreRecorded;

    public GamePanel(GameModel model, HighScores highScores) {
        this.model = model;
        this.highScores = highScores;
        setPreferredSize(new Dimension(model.getWidth() * CELL, model.getHeight() * CELL + HUD));
        setBackground(BG);
        setFocusable(true);
        timer = new Timer(model.getTickDelay(), e -> tick());
        bindKeys();
        timer.start();
    }

    private void tick() {
        model.step();
        timer.setDelay(model.getTickDelay());
        if ((model.getState() == GameState.GAME_OVER || model.getState() == GameState.WON) && !scoreRecorded) {
            scoreRecorded = true;
            repaint();
            recordHighScore();
        }
        repaint();
    }

    private void recordHighScore() {
        int score = model.getScore();
        if (!highScores.qualifies(score)) {
            return;
        }
        String name = JOptionPane.showInputDialog(this,
                "New high score: " + score + "!\nEnter your name:", "High score",
                JOptionPane.PLAIN_MESSAGE);
        int rank = highScores.add(score, name, LocalDate.now());
        showHighScores("You placed #" + rank);
    }

    void showHighScores(String title) {
        StringBuilder sb = new StringBuilder();
        int i = 1;
        for (HighScores.Entry e : highScores.getEntries()) {
            sb.append(String.format("%2d.  %-16s %6d   %s%n", i++, e.name, e.score, e.date));
        }
        if (sb.length() == 0) {
            sb.append("No scores yet. Go play!");
        }
        javax.swing.JTextArea area = new javax.swing.JTextArea(sb.toString());
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        area.setEditable(false);
        JOptionPane.showMessageDialog(this, area, title, JOptionPane.PLAIN_MESSAGE);
    }

    private void restart() {
        model.reset();
        scoreRecorded = false;
        timer.setDelay(model.getTickDelay());
        repaint();
    }

    private void bindKeys() {
        bind(KeyEvent.VK_UP, "up", () -> model.turn(Direction.UP));
        bind(KeyEvent.VK_W, "up", null);
        bind(KeyEvent.VK_DOWN, "down", () -> model.turn(Direction.DOWN));
        bind(KeyEvent.VK_S, "down", null);
        bind(KeyEvent.VK_LEFT, "left", () -> model.turn(Direction.LEFT));
        bind(KeyEvent.VK_A, "left", null);
        bind(KeyEvent.VK_RIGHT, "right", () -> model.turn(Direction.RIGHT));
        bind(KeyEvent.VK_D, "right", null);
        bind(KeyEvent.VK_SPACE, "pause", () -> {
            if (model.getState() == GameState.READY) {
                model.start();
            } else {
                model.togglePause();
            }
            repaint();
        });
        bind(KeyEvent.VK_P, "pause", null);
        bind(KeyEvent.VK_ENTER, "restart", () -> {
            if (model.getState() == GameState.GAME_OVER || model.getState() == GameState.WON) {
                restart();
            }
        });
        bind(KeyEvent.VK_R, "restart", null);
        bind(KeyEvent.VK_M, "wrap", () -> {
            if (model.getState() == GameState.READY) {
                model.setWrapWalls(!model.isWrapWalls());
                repaint();
            }
        });
        bind(KeyEvent.VK_H, "scores", () -> {
            boolean wasRunning = model.getState() == GameState.RUNNING;
            if (wasRunning) {
                model.togglePause();
            }
            showHighScores("High scores");
        });
    }

    private void bind(int key, String name, Runnable action) {
        InputMap map = getInputMap(WHEN_IN_FOCUSED_WINDOW);
        map.put(KeyStroke.getKeyStroke(key, 0), name);
        if (action != null) {
            getActionMap().put(name, new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    action.run();
                }
            });
        }
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawHud(g);
        g.translate(0, HUD);
        drawGrid(g);

        Cell food = model.getFood();
        if (food != null) {
            g.setColor(FOOD);
            g.fillOval(food.x * CELL + 4, food.y * CELL + 4, CELL - 8, CELL - 8);
        }
        List<Cell> snake = model.getSnake();
        for (int i = snake.size() - 1; i >= 0; i--) {
            Cell c = snake.get(i);
            g.setColor(i == 0 ? HEAD : SNAKE);
            g.fillRoundRect(c.x * CELL + 1, c.y * CELL + 1, CELL - 2, CELL - 2, 8, 8);
        }
        drawEyes(g, snake.get(0));
        if (!model.isWrapWalls()) {
            g.setColor(new Color(0x64748B));
            g.setStroke(new BasicStroke(2));
            g.drawRect(1, 1, model.getWidth() * CELL - 2, model.getHeight() * CELL - 2);
        }
        drawOverlay(g);
        g.translate(0, -HUD);
    }

    private void drawHud(Graphics2D g) {
        g.setColor(new Color(0x0F172A));
        g.fillRect(0, 0, getWidth(), HUD);
        g.setColor(Color.WHITE);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        g.drawString("Score " + model.getScore(), 12, 24);
        g.drawString("Level " + model.getLevel(), 150, 24);
        g.drawString("Length " + model.getLength(), 260, 24);
        String best = "Best " + Math.max(highScores.best(), model.getScore());
        g.drawString(best, getWidth() - g.getFontMetrics().stringWidth(best) - 12, 24);
    }

    private void drawGrid(Graphics2D g) {
        g.setColor(GRID);
        for (int x = 0; x <= model.getWidth(); x++) {
            g.drawLine(x * CELL, 0, x * CELL, model.getHeight() * CELL);
        }
        for (int y = 0; y <= model.getHeight(); y++) {
            g.drawLine(0, y * CELL, model.getWidth() * CELL, y * CELL);
        }
    }

    private void drawEyes(Graphics2D g, Cell head) {
        g.setColor(Color.BLACK);
        int cx = head.x * CELL + CELL / 2;
        int cy = head.y * CELL + CELL / 2;
        Direction d = model.getDirection();
        int ox = d.dy != 0 ? 5 : 0;
        int oy = d.dx != 0 ? 5 : 0;
        int fx = d.dx * 4;
        int fy = d.dy * 4;
        g.fillOval(cx + fx - ox - 2, cy + fy - oy - 2, 5, 5);
        g.fillOval(cx + fx + ox - 2, cy + fy + oy - 2, 5, 5);
    }

    private void drawOverlay(Graphics2D g) {
        String title;
        String hint;
        switch (model.getState()) {
            case READY:
                title = "SNAKE";
                hint = "Arrows/WASD to start · M: walls " + (model.isWrapWalls() ? "wrap" : "solid")
                        + " · H: high scores";
                break;
            case PAUSED:
                title = "Paused";
                hint = "Space to continue";
                break;
            case GAME_OVER:
                title = "Game over";
                hint = "Score " + model.getScore() + " · Enter to play again";
                break;
            case WON:
                title = "You filled the board!";
                hint = "Enter to play again";
                break;
            default:
                return;
        }
        int w = model.getWidth() * CELL;
        int h = model.getHeight() * CELL;
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, w, h);
        g.setColor(Color.WHITE);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 40));
        FontMetrics fm = g.getFontMetrics();
        g.drawString(title, (w - fm.stringWidth(title)) / 2, h / 2 - 10);
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15));
        fm = g.getFontMetrics();
        g.drawString(hint, (w - fm.stringWidth(hint)) / 2, h / 2 + 24);
    }
}
