import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import javafx.application.Platform;

public class Client {
    private String ipAddress;
    private int tcpPort;

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    public Client(String ipAddress, int tcpPort) {
        this.ipAddress = ipAddress;
        this.tcpPort = tcpPort;
    }

    public void disconnect() {
        try {
            if (socket != null) socket.close();
            socket = null;
            in = null;
            out = null;
        } catch (IOException ex) {
            System.out.println("Disconnect!");
        }
    }

    public void connect() throws IOException {
        socket = new Socket(ipAddress, tcpPort);
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());

        Thread receiveThread = new Thread(()->{
            try {
                while (true) {
                    receive();
                }
            } catch (IOException ex) {
                System.out.println("Disconnected. Stop the receiver thread!");
            }
        });
        receiveThread.setDaemon(true);
        receiveThread.start();
    }

    public void send(int dataType, String data) throws IOException {
        out.writeInt(dataType);
        out.writeInt(data.length());
        out.write(data.getBytes(), 0, data.length());
        out.flush();
    }

    public void sendUploadPuzzle(int[] board) throws IOException {
        out.writeInt(GameEngine.UPLOAD_PUZZLE);
        for (int v : board) {
            out.writeInt(v);
        }
        out.flush();
    }

    public void requestTop10() throws IOException {
        out.writeInt(11); // REQUEST_TOP10
        out.flush();
    }

    public void receive() throws IOException {
        int dataType = in.readInt();

        switch (dataType) {
            case GameEngine.PUZZLE:
                handleReceivePuzzle();
                break;
            case GameEngine.GAMEOVER:
                GameEngine.getInstance().gameOver = true;
                break;
            case GameEngine.SCORES:
                handleScores();
                break;
            case 12: // TOP10_SCORES
                handleTop10();
                break;
            case GameEngine.WAIT_OR_LEAVE:
                handleWaitOrLeave();
                break;
        }
    }

    public void handleReceivePuzzle() throws IOException {
        int rows = in.readInt();
        int cols = in.readInt();

        int[] puzzle = new int[rows * cols];

        for (int i=0; i<rows*cols; i++) {
            puzzle[i] = in.readInt();
        }

        int myId = in.readInt();
        int currentTurn = in.readInt();
        int movesLeft = in.readInt();
        int score = in.readInt();
        int level = in.readInt();
        int started = in.readInt();

        GameEngine engine = GameEngine.getInstance();
        engine.setBoard(puzzle);
        engine.myPlayerId = myId;
        engine.currentTurnPlayerId = currentTurn;
        engine.movesLeft = movesLeft;
        engine.setScore(score);
        engine.setLevel(level);
        engine.isStarted = (started == 1);

        engine.gameOver = false;

        if (GameWindow.instance != null) {
            GameWindow.instance.startGameTimer();
        }
    }

    private void handleScores() throws IOException {
        int count = in.readInt();
        List<String> scores = new ArrayList<>();
        byte[] buffer = new byte[1024];
        for (int i = 0; i < count; i++) {
            int len = in.readInt();
            in.readFully(buffer, 0, len);
            scores.add(new String(buffer, 0, len));
        }
        Platform.runLater(() -> {
            GameEngine.getInstance().gameOver = true;
            new ScoreResultWindow(scores);
        });
    }

    private void handleTop10() throws IOException {
        int count = in.readInt();
        List<String> scores = new ArrayList<>();
        byte[] buffer = new byte[1024];
        for (int i = 0; i < count; i++) {
            int len = in.readInt();
            in.readFully(buffer, 0, len);
            scores.add(new String(buffer, 0, len));
        }
        Platform.runLater(() -> {
            try {
                new ScoreboardWindow(scores);
            } catch (Exception e) {}
        });
    }

    private void handleWaitOrLeave() throws IOException {
        try {
            out.writeInt(GameEngine.WAIT_LEAVE_RESPONSE);
            out.writeInt(1);
            out.flush();
        } catch (IOException e) {}
    }
}