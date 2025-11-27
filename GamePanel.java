import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Random;

public class GamePanel extends JPanel implements ActionListener {
    private final String[] easyWords = {"dog", "cat", "pen", "box", "sun"};
    private final String[] mediumWords = {"apple", "banana", "school", "laptop", "orange"};
    private final String[] hardWords = {"inheritance", "polymorphism", "abstraction", "encapsulation", "multithreading"};

    private final JLabel wordLabel = new JLabel();
    private final JTextField inputField = new JTextField(20);
    private final JLabel timerLabel = new JLabel("Time: 30");
    private final JLabel scoreLabel = new JLabel("Score: 0");
    private final JLabel highScoreLabel = new JLabel("High Score: 0");
    private final JProgressBar timerBar = new JProgressBar(0, 30);
    private final JComboBox<String> levelBox = new JComboBox<>(new String[]{"Easy", "Medium", "Hard"});
    private final JButton pauseButton = new JButton("Pause");
    private final JButton restartButton = new JButton("Restart");

    private final Timer timer;
    private boolean isPaused = false;

    private int score = 0;
    private int totalAttempts = 0;
    private int timeLeft = 30;
    private int highScore = 0;
    private String currentWord;
    private final Random random = new Random();
    private final String HIGH_SCORE_FILE = "highscore.txt";

    public GamePanel() {
        this.setPreferredSize(new Dimension(500, 400));
        this.setLayout(new GridLayout(9, 1, 10, 10));
        this.setBackground(new Color(240, 248, 255));  // Light blue background

        wordLabel.setFont(new Font("Verdana", Font.BOLD, 28));
        wordLabel.setHorizontalAlignment(SwingConstants.CENTER);
        timerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        scoreLabel.setHorizontalAlignment(SwingConstants.CENTER);
        highScoreLabel.setHorizontalAlignment(SwingConstants.CENTER);
        levelBox.setSelectedIndex(0);

        inputField.setFont(new Font("Arial", Font.PLAIN, 22));
        inputField.setHorizontalAlignment(JTextField.CENTER);
        inputField.addActionListener(e -> checkWord());

        timerBar.setValue(30);
        timerBar.setForeground(Color.BLUE);

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout());
        controlPanel.add(pauseButton);
        controlPanel.add(restartButton);

        pauseButton.addActionListener(e -> togglePause());
        restartButton.addActionListener(e -> restartGame());

        this.add(new JLabel("Select Difficulty:", SwingConstants.CENTER));
        this.add(levelBox);
        this.add(timerLabel);
        this.add(scoreLabel);
        this.add(highScoreLabel);
        this.add(wordLabel);
        this.add(inputField);
        this.add(timerBar);
        this.add(controlPanel);

        loadHighScore();
        setNewWord();

        timer = new Timer(1000, this);
        timer.start();
    }

    private void setNewWord() {
        String level = (String) levelBox.getSelectedItem();
        String[] words = switch (level) {
            case "Medium" -> mediumWords;
            case "Hard" -> hardWords;
            default -> easyWords;
        };
        currentWord = words[random.nextInt(words.length)];
        wordLabel.setText(currentWord);
        inputField.setText("");
        this.setBackground(new Color(240, 248, 255));
    }

    private void checkWord() {
        if (isPaused) return;

        totalAttempts++;
        String typed = inputField.getText().trim();
        if (typed.equals(currentWord)) {
            score++;
            scoreLabel.setText("Score: " + score);
            setBackground(new Color(198, 239, 206));
        } else {
            setBackground(new Color(255, 204, 204));
        }
        setNewWord();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (isPaused) return;

        timeLeft--;
        timerLabel.setText("Time: " + timeLeft);
        timerBar.setValue(timeLeft);

        if (timeLeft <= 0) {
            timer.stop();
            inputField.setEnabled(false);
            levelBox.setEnabled(false);
            pauseButton.setEnabled(false);
            updateHighScore();
            showFinalScore();
        }
    }

    private void togglePause() {
        isPaused = !isPaused;
        pauseButton.setText(isPaused ? "Resume" : "Pause");
    }

    private void restartGame() {
        score = 0;
        totalAttempts = 0;
        timeLeft = 30;
        isPaused = false;
        pauseButton.setText("Pause");
        inputField.setEnabled(true);
        levelBox.setEnabled(true);
        pauseButton.setEnabled(true);
        scoreLabel.setText("Score: 0");
        timerLabel.setText("Time: 30");
        timerBar.setValue(30);
        setNewWord();
        timer.restart();
    }

    private void loadHighScore() {
        try (BufferedReader br = new BufferedReader(new FileReader(HIGH_SCORE_FILE))) {
            String line = br.readLine();
            if (line != null) {
                highScore = Integer.parseInt(line.trim());
                highScoreLabel.setText("High Score: " + highScore);
            }
        } catch (IOException | NumberFormatException ignored) {}
    }

    private void updateHighScore() {
        if (score > highScore) {
            highScore = score;
            highScoreLabel.setText("High Score: " + highScore);
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(HIGH_SCORE_FILE))) {
                bw.write(String.valueOf(highScore));
            } catch (IOException ignored) {}
        }
    }

    private void showFinalScore() {
        double accuracy = (totalAttempts == 0) ? 0.0 : (score * 100.0 / totalAttempts);
        String message = "Final Score: " + score + "\\n" +
                "Accuracy: " + String.format("%.2f", accuracy) + "%\\n" +
                "High Score: " + highScore;
        JOptionPane.showMessageDialog(this, message, "Game Over", JOptionPane.INFORMATION_MESSAGE);
    }
}