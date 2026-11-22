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

import java.io.IOException;
import java.util.List;

public class ScoreboardWindow {
    Stage stage;

    @FXML
    ListView<String> scoreList;

    // Constructor used when receiving scores from server
    public ScoreboardWindow(List<String> scores) throws IOException {
        initWindow();
        ObservableList<String> items = FXCollections.observableArrayList(scores);
        scoreList.setItems(items);
        stage.showAndWait();
    }

    // Fallback constructor (shows empty list)
    public ScoreboardWindow() throws IOException {
        initWindow();
        scoreList.setItems(FXCollections.observableArrayList("No scores available"));
        stage.showAndWait();
    }

    private void initWindow() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("scoreUI.fxml"));
        loader.setController(this);
        Parent root = loader.load();
        Scene scene = new Scene(root);

        stage = new Stage();
        stage.setScene(scene);
        stage.setTitle("Battle Joker - Top 10");
        stage.setMinWidth(scene.getWidth());
        stage.setMinHeight(scene.getHeight());

        setFont(14);
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
}