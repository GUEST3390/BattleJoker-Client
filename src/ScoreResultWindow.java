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

public class ScoreResultWindow {
    Stage stage;

    @FXML
    ListView<String> scoreList;

    public ScoreResultWindow(List<String> scores) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("scoreUI.fxml"));
            loader.setController(this);
            Parent root = loader.load();
            Scene scene = new Scene(root);

            stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Game Over - Scores");
            stage.setMinWidth(scene.getWidth());
            stage.setMinHeight(scene.getHeight());

            setFont(14);
            ObservableList<String> items = FXCollections.observableArrayList(scores);
            scoreList.setItems(items);

            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
