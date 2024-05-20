package contollers;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.encryptdecryptHandler;
import model.sqlLiteHandler;
import javafx.fxml.Initializable;
import javax.crypto.SecretKey;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class mainController implements Initializable {
    private sqlLiteHandler handleDefaultSqlConnect;
    private sqlLiteHandler handleSql;
    private encryptdecryptHandler handleEncrypt;
    private SecretKey loadedKey;
    private String dbLocation;

    @FXML
    private TextArea keyArea;

    @FXML
    private Button proceedBtn;
    @FXML
    private Button LoadDataBase;
    @FXML
    private Button useDefaultButton;
    @FXML
    private Button resetBtn;
    @FXML
    private Button createNewButton;
    @FXML
    private Button loadKeyButton;

    @FXML
    private CheckBox otherDbCheck;
    @FXML
    private CheckBox defaultCheck;

    public mainController() {
        this.handleSql = new sqlLiteHandler();
        this.handleEncrypt = new encryptdecryptHandler();
        this.handleDefaultSqlConnect = new sqlLiteHandler();
        this.defaultCheck = new CheckBox();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        String appLocation = System.getProperty("user.dir");
        String dataBaseLoc = appLocation + File.separator + "accountsLocal.db";

        if (handleDefaultSqlConnect.connecttoDB(dataBaseLoc)) {
            this.defaultCheck.setSelected(true);
            this.useDefaultButton.setDisable(false);
            this.dbLocation = dataBaseLoc;
        } else {
            this.defaultCheck.setSelected(false);
            this.useDefaultButton.setDisable(true);
        }
    }

    @FXML
    public void btnPresses(ActionEvent event) throws IOException {
        if (event.getSource().equals((createNewButton))) {
            if (this.handleDefaultSqlConnect.createNew()) {
                try {
                    String appLocation = System.getProperty("user.dir");
                    String keyBaseLoc = appLocation + File.separator + "defaultKey.key";
                    String dataBaseLoc = appLocation + File.separator + "accountsLocal.db";
                    this.loadedKey = handleEncrypt.loadKeyFromFile(keyBaseLoc);
                    this.handleDefaultSqlConnect = new sqlLiteHandler();
                    this.handleDefaultSqlConnect.connecttoDB(dataBaseLoc);

                    this.dbLocation = dataBaseLoc;

                    keyArea.setText(handleEncrypt.keyToBase64Text(loadedKey));
                    defaultCheck.setSelected(true);
                    this.useDefaultButton.setDisable(false);
                } catch (Exception e) {
                    keyArea.setText("Exception Occured");
                    e.printStackTrace();
                }
            } else {
                keyArea.setText("Database already exists");
            }
        }
        if (event.getSource().equals(loadKeyButton)) {
            LoadKeyExplorer();
        }
        if (event.getSource().equals(LoadDataBase)) {
            LoadDBExplorer();
        }
        if (event.getSource().equals(useDefaultButton)) {

        }
        if (event.getSource().equals(resetBtn)) {
            resetChoices();
        }

        if (event.getSource().equals(proceedBtn)) {
            proceedToNextScreen(event);
        }
    }

    private void resetChoices() {
        String appLocation = System.getProperty("user.dir");
        String dataBaseLoc = appLocation + File.separator + "accountsLocal.db";
        if (handleDefaultSqlConnect.connecttoDB(dataBaseLoc)) {
            this.dbLocation = dataBaseLoc;
            this.defaultCheck.setSelected(true);
            this.useDefaultButton.setDisable(false);
        } else {
            this.dbLocation = "";
            this.defaultCheck.setSelected(false);
            this.useDefaultButton.setDisable(true);
        }
        this.loadedKey = null;

        this.keyArea.setText("");
        this.keyArea.setDisable(false);

        this.otherDbCheck.setSelected(false);
    }

    private void LoadKeyExplorer() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load Key");

        String currentWorkingDirectory = System.getProperty("user.dir");
        fileChooser.setInitialDirectory(new File(currentWorkingDirectory));

        FileChooser.ExtensionFilter dataBaseFilter = new FileChooser.ExtensionFilter("Key File (*.key)", "*.key");
        fileChooser.getExtensionFilters().addAll(dataBaseFilter);

        // Show the file chooser dialog and get the selected file
        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            // Perform operations on the selected file
            String filePath = selectedFile.getAbsolutePath();
            try {
                SecretKey key = handleEncrypt.loadKeyFromFile(filePath);
                keyArea.setText(handleEncrypt.keyToBase64Text(key));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void LoadDBExplorer() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open DB File");

        String currentWorkingDirectory = System.getProperty("user.dir");
        fileChooser.setInitialDirectory(new File(currentWorkingDirectory));

        FileChooser.ExtensionFilter dataBaseFilter = new FileChooser.ExtensionFilter("Database File (*.db)", "*.db");
        fileChooser.getExtensionFilters().addAll(dataBaseFilter);

        // Show the file chooser dialog and get the selected file
        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            String filePath = selectedFile.getAbsolutePath();
            this.dbLocation = filePath;
            boolean connectionEst = this.handleSql.connecttoDB(filePath);
            if (connectionEst) {
                this.otherDbCheck.setSelected(true);
            } else {
                this.otherDbCheck.setSelected(false);
            }
        }
    }

    private void proceedToNextScreen(ActionEvent event) {
        try {
            this.loadedKey = handleEncrypt.convertStringToSecretKey(keyArea.getText().trim());

            boolean otherdbCheck = this.otherDbCheck.isSelected() && this.handleSql != null && this.loadedKey != null && this.dbLocation != null;
            boolean defaultdbCheck = this.handleDefaultSqlConnect != null && this.loadedKey != null && this.dbLocation != null;
            if (otherdbCheck) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/UIfiles/accountsUI.fxml"));
                Parent viewParent = loader.load();
                Scene viewScene = new Scene(viewParent);

                AccountsUIController controller = loader.getController();
                controller.setController(this.handleSql, this.loadedKey, this.dbLocation);


                Stage sourceWin = (Stage) ((Node) event.getSource()).getScene().getWindow();
                sourceWin.setScene(viewScene);

                String stageTitle = sourceWin.getTitle();
                sourceWin.setTitle(String.format("%s - %s.db", stageTitle, this.handleSql.getFileName()));

                sourceWin.show();
                return;
            }

            if (defaultdbCheck) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/UIfiles/accountsUI.fxml"));
                Parent viewParent = loader.load();
                Scene viewScene = new Scene(viewParent);

                AccountsUIController controller = loader.getController();
                controller.setController(this.handleDefaultSqlConnect, this.loadedKey, this.dbLocation);

                Stage sourceWin = (Stage) ((Node) event.getSource()).getScene().getWindow();
                sourceWin.setScene(viewScene);
                sourceWin.show();
                return;
            }

        } catch (Exception e) {

            e.printStackTrace();
        }
        keyArea.setText("No key was provided");
    }
}
