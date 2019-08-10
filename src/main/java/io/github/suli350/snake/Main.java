package io.github.suli350.snake;

import java.nio.file.Paths;
import java.util.Random;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GameModel model = new GameModel(24, 20, new Random());
            HighScores scores = new HighScores(
                    Paths.get(System.getProperty("user.home"), ".snake-game", "highscores.txt"));
            JFrame frame = new JFrame("Snake");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);
            frame.add(new GamePanel(model, scores));
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
