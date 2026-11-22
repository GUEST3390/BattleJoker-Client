import javafx.application.Application;
import javafx.stage.Stage;

import java.io.OutputStream;
import java.io.PrintStream;

public class BattleJoker extends Application {
    public static Client client;

    public static void endProgram() {
        if (client != null) client.disconnect();
        System.exit(0);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            GetNameDialog dialog = new GetNameDialog();
            String username = dialog.getPlayername();
            String ip = dialog.getIp();
            int port = dialog.getPort();

            client = new Client(ip, port);
            client.connect();

            GameWindow win = new GameWindow(primaryStage);
            win.setName(username);

            client.send(GameEngine.NAME, username);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void stop() {
        if (client != null) {
            client.disconnect();
        }
    }

    public static void main(String[] args) {
        System.setErr(new FilteredStream(System.err));
        launch();
    }
}

class FilteredStream extends PrintStream {

    public FilteredStream(OutputStream out) {
        super(out);
    }

    @Override
    public void println(String x) {
        if (x != null && !x.contains("SLF4J: "))
            super.println(x);
    }

    @Override
    public void print(String x) {
        if (x != null && !x.contains("WARNING: Loading FXML document with JavaFX API of version 18"))
            super.print(x);
    }
}