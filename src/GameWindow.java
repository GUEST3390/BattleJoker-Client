import javafx.animation.AnimationTimer;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

public class GameWindow {
    public static GameWindow instance;

    @FXML
    MenuBar menuBar;

    @FXML
    Label nameLabel;

    @FXML
    Label scoreLabel;

    @FXML
    Label levelLabel;

    @FXML
    Label comboLabel;

    @FXML
    Label moveCountLabel;

    @FXML
    Label turnLabel;

    @FXML
    Button startBtn;

    @FXML
    Pane boardPane;

    @FXML
    Canvas canvas;

    Stage stage;
    AnimationTimer animationTimer;

    final String imagePath = "images/";
    final String[] symbols = {"bg", "A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "Joker"};
    final Image[] images = new Image[symbols.length];
    final GameEngine gameEngine = GameEngine.getInstance();

    public GameWindow(Stage stage) throws IOException {
        instance = this;
        loadImages();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("mainUI.fxml"));
        loader.setController(this);
        Parent root = loader.load();
        Scene scene = new Scene(root);

        scene.setOnKeyPressed(event -> {
            String code = event.getCode().toString();
            if (gameEngine.currentTurnPlayerId == gameEngine.myPlayerId &&
                    (code.equals("UP") || code.equals("DOWN") || code.equals("LEFT") || code.equals("RIGHT"))) {
                try {
                    BattleJoker.client.send(GameEngine.COMMAND, code);
                } catch (IOException ex) {
                    System.out.println("Disconnected");
                    System.exit(-1);
                }
            }
        });

        this.stage = stage;

        stage.setScene(scene);
        stage.setTitle("Battle Joker");
        stage.setMinWidth(scene.getWidth());
        stage.setMinHeight(scene.getHeight());

        stage.widthProperty().addListener(w -> onWidthChangedWindow(((ReadOnlyDoubleProperty) w).getValue()));
        stage.heightProperty().addListener(h -> onHeightChangedWindow(((ReadOnlyDoubleProperty) h).getValue()));
        stage.setOnCloseRequest(event -> quit());

        stage.show();
        initCanvas();

        gameStart();
    }

    private void gameStart() {
        animationTimer.start();
    }

    public void startGameTimer() {
        if (animationTimer != null) {
            animationTimer.start();
        }
    }

    private void loadImages() throws IOException {
        for (int i = 0; i < symbols.length; i++)
            images[i] = new Image(Files.newInputStream(Paths.get(imagePath + symbols[i] + ".png")));
    }

    private void initCanvas() {
        animationTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                render();
                if (gameEngine.isGameOver()) {
                    System.out.println("Game Over!");
                    animationTimer.stop();
                } else {
                    updateUI();
                }
            }
        };
        canvas.requestFocus();
    }

    private void updateUI() {
        scoreLabel.setText("Score: " + gameEngine.getScore());
        levelLabel.setText("Level: " + gameEngine.getLevel());

        if (gameEngine.myPlayerId == 1 && !gameEngine.isStarted) {
            turnLabel.setText("You are Player 1. Click Start or wait for 4 players.");
            turnLabel.setStyle("-fx-text-fill: blue;");
            startBtn.setVisible(true);
        } else if (gameEngine.isStarted && gameEngine.currentTurnPlayerId == gameEngine.myPlayerId) {
            turnLabel.setText("YOUR TURN! Moves left: " + gameEngine.movesLeft);
            turnLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
            startBtn.setVisible(false);
        } else if (gameEngine.isStarted) {
            turnLabel.setText("Waiting for Player " + gameEngine.currentTurnPlayerId);
            turnLabel.setStyle("-fx-text-fill: red;");
            startBtn.setVisible(false);
        } else {
            turnLabel.setText("Waiting for game to start...");
            turnLabel.setStyle("-fx-text-fill: orange;");
            startBtn.setVisible(false);
        }
    }

    private void render() {

        double w = canvas.getWidth();
        double h = canvas.getHeight();

        double sceneSize = Math.min(w, h);
        double blockSize = sceneSize / GameEngine.SIZE;
        double padding = blockSize * .05;
        double startX = (w - sceneSize) / 2;
        double startY = (h - sceneSize) / 2;
        double cardSize = blockSize - (padding * 2);

        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, w, h);

        double y = startY;
        int v;

        // Draw the background and cards from left to right, and top to bottom.
        for (int i = 0; i < GameEngine.SIZE; i++) {
            double x = startX;
            for (int j = 0; j < GameEngine.SIZE; j++) {
                gc.drawImage(images[0], x, y, blockSize, blockSize);  // Draw the background

                v = gameEngine.getValue(i, j);

                if (v > 0)  // if a card is in the place, draw it
                    gc.drawImage(images[v], x + padding, y + padding, cardSize, cardSize);

                x += blockSize;
            }
            y += blockSize;
        }
    }

    @FXML
    void onStartClicked() {
        try {
            BattleJoker.client.send(GameEngine.COMMAND, "START");
            canvas.requestFocus();
        } catch (IOException e) {}
    }

    @FXML
    void onSavePuzzle() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Puzzle");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            try (PrintWriter writer = new PrintWriter(file)) {
                for (int i = 0; i < GameEngine.SIZE * GameEngine.SIZE; i++) {
                    writer.print(gameEngine.board[i] + " ");
                }
            } catch (FileNotFoundException e) {}
        }
    }

    @FXML
    void onUploadPuzzle() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Upload Puzzle");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            try {
                Scanner scanner = new Scanner(file);
                int[] newBoard = new int[16];

                for (int i = 0; i < 16; i++) {
                    if (scanner.hasNextInt()) {
                        newBoard[i] = scanner.nextInt();
                    } else {
                        newBoard[i] = 0;
                    }
                }
                scanner.close();

                BattleJoker.client.sendUploadPuzzle(newBoard);
                gameEngine.setBoard(newBoard);

                System.out.println("Puzzle uploaded successfully!");
            } catch (Exception e) {
                System.out.println("Failed to upload puzzle");
                e.printStackTrace();
            }
        }
    }

    @FXML
    void onShowScoreboard() {
        try {
            BattleJoker.client.requestTop10();
        } catch (IOException e) {
            try {
                new ScoreboardWindow();
            } catch (Exception ex) {}
        }
    }

    void onWidthChangedWindow(double w) {
        double width = w - boardPane.getBoundsInParent().getMinX();
        boardPane.setMinWidth(width);
        canvas.setWidth(width);
        render();
    }

    void onHeightChangedWindow(double h) {
        double height = h - boardPane.getBoundsInParent().getMinY() - menuBar.getHeight();
        boardPane.setMinHeight(height);
        canvas.setHeight(height);
        render();
    }

    void quit() {
        System.out.println("Bye bye");
        stage.close();
        System.exit(0);
    }

    public void setName(String name) {
        nameLabel.setText(name);
        gameEngine.setPlayerName(name);
    }
}