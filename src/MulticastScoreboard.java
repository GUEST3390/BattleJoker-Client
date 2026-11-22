import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Timer;
import java.util.TimerTask;

public class MulticastScoreboard extends Application {
    @FXML
    ListView<String> scoreList;

    Stage stage;
    ObservableList<String> items = FXCollections.observableArrayList();

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("scoreUI.fxml"));
        loader.setController(this);
        Parent root = loader.load();
        Scene scene = new Scene(root);

        stage = primaryStage;
        stage.setScene(scene);
        stage.setTitle("Battle Joker - Multicast Scoreboard");
        stage.setMinWidth(scene.getWidth());
        stage.setMinHeight(scene.getHeight());

        setFont(14);
        scoreList.setItems(items);

        stage.show();

        startReceiver();
        startRefreshTimer();
    }

    private void setFont(int fontSize) {
        scoreList.setCellFactory(param -> {
            TextFieldListCell<String> cell = new TextFieldListCell<>();
            String osName = System.getProperty("os.name").toLowerCase();
            if (osName.contains("win")) {
                cell.setFont(Font.font("Courier New", fontSize));
            } else if (osName.contains("mac")) {
                cell.setFont(Font.font("Menlo", fontSize));
            } else {
                cell.setFont(Font.font("Monospaced", fontSize));
            }
            return cell;
        });
    }

    private void startReceiver() {
        Thread t = new Thread(() -> {
            try (MulticastSocket socket = new MulticastSocket(8899)) {
                InetAddress group = InetAddress.getByName("230.0.0.1");
                socket.joinGroup(group);
                byte[] buf = new byte[4096];
                while (true) {
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    socket.receive(packet);
                    String data = new String(packet.getData(), 0, packet.getLength()).trim();
                    Platform.runLater(() -> {
                        items.clear();
                        if (!data.isEmpty()) {
                            for (String line : data.split("\n")) {
                                if (!line.trim().isEmpty()) items.add(line.trim());
                            }
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        t.setDaemon(true);
        t.start();
    }

    private void startRefreshTimer() {
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> scoreList.refresh());
            }
        }, 1000, 2000);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
