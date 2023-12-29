package Main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{

        Image icon = new Image("/UIfiles/ico.png");

        Parent root = FXMLLoader.load(getClass().getResource("/UIfiles/main.fxml"));
        primaryStage.getIcons().add(icon);
        primaryStage.setTitle("Account Manager");
        primaryStage.setScene(new Scene(root, 400, 275));
        primaryStage.show();

    }

    public static void main(String[] args) {
        launch(args);
    }
}
