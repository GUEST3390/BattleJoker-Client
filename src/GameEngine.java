import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class GameEngine {
    public static final int NAME = 0;
    public static final int COMMAND = 1;
    public static final int MESSAGE = 2;
    public static final int SETTING = 3;
    public static final int PUZZLE = 4;
    public static final int GAMEOVER = 5;
    public static final int WAIT_OR_LEAVE = 6;
    public static final int WAIT_LEAVE_RESPONSE = 7;
    public static final int SCORES = 8;
    public static final int UPLOAD_PUZZLE = 9;
    public static final int START_GAME = 10;
    // you can add more constants

    public static final int LIMIT = 14;
    public static final int SIZE = 4;
    int[] board = new int[SIZE * SIZE];
    Random random = new Random(0);

    private static GameEngine instance;    // singleton
    public boolean gameOver;

    public int myPlayerId = -1;
    public int currentTurnPlayerId = -1;
    public int movesLeft = 4;
    public boolean isStarted = false;

    private String playerName;
    private int level = 1;
    private int score;
    private int combo;
    private int totalMoveCount;
    private int numOfTilesMoved;

    private final Map<String, Runnable> actionMap = new HashMap<>();

    private GameEngine() {
        // define a hash map to contain the links from the actions to the corresponding methods
        actionMap.put("UP", this::moveUp);
        actionMap.put("DOWN", this::moveDown);
        actionMap.put("LEFT", this::moveLeft);
        actionMap.put("RIGHT", this::moveRight);

        // start the first round
        // nextRound();
    }

    public static GameEngine getInstance() {    // singleton
        if (instance == null)
            instance = new GameEngine();
        return instance;
    }

    public void setBoard(int[] board) {
        this.board = board;
    }

    /**
     * Generate a new random value and determine the game status.
     * @return true if the next round can be started, otherwise false.
     */
    private boolean nextRound() {
        if (isFull()) return false;
        int i;

        // randomly find an empty place
        do {
            i = random.nextInt(SIZE * SIZE);
        } while (board[i] > 0);

        // randomly generate a card based on the existing level, and assign it to the select place
        board[i] = random.nextInt(level) / 4 + 1;
        return true;
    }

    /**
     * @return true if all blocks are occupied.
     */
    private boolean isFull() {
        for (int v : board)
            if (v == 0) return false;
        return true;
    }

    /**
     * Move and combine the cards based on the input direction
     * @param dir
     */
    public void moveMerge(String dir) {
        if (actionMap.containsKey(dir)) {
            actionMap.get(dir).run();
        }
    }

    /**
     * move the values downward and merge them.
     */
    private void moveDown() {
        try {
            BattleJoker.client.send(COMMAND, "DOWN");
        } catch (IOException ex) {
            System.out.println("Unable to send action to the server...");
            BattleJoker.endProgram();
        }
    }

    /**
     * move the values upward and merge them.
     */
    private void moveUp() {
        try {
            BattleJoker.client.send(COMMAND, "UP");
        } catch (IOException ex) {
            System.out.println("Unable to send action to the server...");
            BattleJoker.endProgram();
        }
    }

    /**
     * move the values rightward and merge them.
     */
    private void moveRight() {
        try {
            BattleJoker.client.send(COMMAND, "RIGHT");
        } catch (IOException ex) {
            System.out.println("Unable to send action to the server...");
            BattleJoker.endProgram();
        }
    }

    /**
     * move the values leftward and merge them.
     */
    private void moveLeft() {
        try {
            BattleJoker.client.send(COMMAND, "LEFT");
        } catch (IOException ex) {
            System.out.println("Unable to send action to the server...");
            BattleJoker.endProgram();
        }
    }

    public int getValue(int r, int c) {
        return board[r * SIZE + c];
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public void setPlayerName(String name) {
        playerName = name;
    }

    public int getScore() { return score; }
    public int getCombo() { return combo; }
    public int getLevel() { return level; }
    public int getMoveCount() { return totalMoveCount; }

    public void setScore(int score) { this.score = score; }
    public void setLevel(int level) { this.level = level; }
    public void setCombo(int combo) { this.combo = combo; }
}
