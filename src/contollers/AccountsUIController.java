package contollers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.*;
import model.cellFactories.CopyButtonCellFactory;
import model.cellFactories.accountActionsCellFactory;
import model.interfaces.accountActionsInterface;
import model.objects.accountObject;
import model.jsonActions;
import javax.crypto.SecretKey;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class AccountsUIController implements Initializable, accountActionsInterface<accountObject> {
    private sqlLiteHandler handleSql;
    private SecretKey key;
    private passwordGen generatePass;
    private encryptdecryptHandler decryptHandler;
    private String dbLoc;
    private ResultSet rs;
    private jsonActions actionJSON;

    @FXML
    private TextArea passwordTextArea;
    @FXML
    private TextArea searchArea;

    @FXML
    private TableView<accountObject> databaseTable;

    @FXML
    private TableColumn<accountObject, String> userPlatformColumn;

    @FXML
    private TableColumn<accountObject, String> userNameColumn;

    @FXML
    private TableColumn<accountObject, String> userEmailColumn;

    @FXML
    private TableColumn<accountObject, String> userPasswordColumn;

    @FXML
    private TableColumn<accountObject, Void> actionColumn;


    @FXML
    private Button generateRandomPass;
    @FXML
    private Button copyGenPassword;
    @FXML
    private Button addAccountRecord;
    @FXML
    private Button changeLogBtn;
    @FXML
    private Button reEncrypt;
    @FXML
    private Button backToMain;
    @FXML
    private Button searchBtn;
    @FXML
    private Button refreshTblButton;
    @FXML
    private Button clearSearchBtn;
    @FXML
    private Button exportToText;
    @FXML
    private Button exportToRawJsonText;
    @FXML
    private Button importJsonText;

    @FXML
    private Spinner<Integer> passwordLength;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        userPlatformColumn.setCellFactory(new CopyButtonCellFactory<>());
        userPlatformColumn.setCellValueFactory(new PropertyValueFactory<>("userPlatform"));

        userNameColumn.setCellFactory(new CopyButtonCellFactory<>());
        userNameColumn.setCellValueFactory(new PropertyValueFactory<>("userName"));

        userEmailColumn.setCellFactory(new CopyButtonCellFactory<>());
        userEmailColumn.setCellValueFactory(new PropertyValueFactory<>("userEmail"));

        userPasswordColumn.setCellFactory(new CopyButtonCellFactory<>());
        userPasswordColumn.setCellValueFactory(new PropertyValueFactory<>("userPassword"));

        actionColumn.setCellFactory(new accountActionsCellFactory<>(this));

    }

    public AccountsUIController() {
        this.generatePass = new passwordGen();
        this.decryptHandler = new encryptdecryptHandler();
        this.dbLoc = "";
    }

    public void buttonEvent(ActionEvent event) {
        if (event.getSource().equals(generateRandomPass)) {
            int passwordLength = this.passwordLength.getValue().intValue();
            String generatedPassword = this.generatePass.generatePassword(passwordLength);
            this.passwordTextArea.setText(generatedPassword);
        }
        if (event.getSource().equals(copyGenPassword)) {
            String genPassword = passwordTextArea.getText();
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(genPassword);
            clipboard.setContent(content);
        }
        if (event.getSource().equals(addAccountRecord)) {
            addAccount();
        }
        if (event.getSource().equals(changeLogBtn)) {
            try {
                changeSceen(event);
            } catch (Exception e) {
                e.printStackTrace();
                passwordTextArea.setText("Exception");
            }
        }
        if (event.getSource().equals(reEncrypt)) {
            reEncryptAllFields();
        }
        if (event.getSource().equals(backToMain)) {
            try {
                gobacktoMain(event);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (event.getSource().equals(searchBtn)) {
            this.rs = handleSql.selectAccounts(searchArea.getText().trim());
            refreshTable();
        }
        if (event.getSource().equals(refreshTblButton)) {
            this.rs = handleSql.selectAccounts();
            refreshTable();
        }
        if (event.getSource().equals(clearSearchBtn)) {
            this.searchArea.setText("");
        }
        if (event.getSource().equals(exportToText)) {
            exportToTextFile();
        }

        if(event.getSource().equals(exportToRawJsonText)){
            showExportDialog();
        }

        if(event.getSource().equals(importJsonText)){
            importJson();
        }
    }

    private void changeSceen(ActionEvent event) throws IOException {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/UIfiles/changeLogUI.fxml"));
        Parent viewParent = loader.load();
        Scene viewScene = new Scene(viewParent);

        changeLogUIController controller = loader.getController();
        controller.setController(this.handleSql, this.key, this.dbLoc);

        Stage sourceWin = (Stage) ((Node) event.getSource()).getScene().getWindow();
        sourceWin.setScene(viewScene);
        sourceWin.show();
        return;
    }

    private void gobacktoMain(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/UIfiles/main.fxml"));
        Parent viewParent = loader.load();
        Scene viewScene = new Scene(viewParent);
        Stage sourceWin = (Stage) ((Node) event.getSource()).getScene().getWindow();

        String stageTitle[] = sourceWin.getTitle().split("-");
        sourceWin.setTitle(String.format("%s", stageTitle[0].strip()));

        sourceWin.setScene(viewScene);
        sourceWin.show();
        return;
    }

    public void setController(sqlLiteHandler handleSql, SecretKey key, String dbLoc) {
        this.key = key;
        this.handleSql = handleSql;
        this.dbLoc = dbLoc;
        this.handleSql.setKey(this.key);
        this.actionJSON = new jsonActions();


        rs = handleSql.selectAccounts();
        refreshTable();
    }

    private void addAccount() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("User Details");

        Image icon = new Image("/UIfiles/ico.png");
        dialog.getIcons().add(icon);

        GridPane dialogPane = new GridPane();
        dialogPane.setAlignment(Pos.CENTER);
        dialogPane.setPadding(new Insets(10));
        dialogPane.setHgap(10);
        dialogPane.setVgap(10);

        TextField platformTextField = new TextField();
        TextField nameTextField = new TextField();
        TextField emailTextField = new TextField();
        TextField passwordField = new TextField();

        dialogPane.addRow(0, new Label("Platform:"), platformTextField);
        dialogPane.addRow(1, new Label("Name:"), nameTextField);
        dialogPane.addRow(2, new Label("Email:"), emailTextField);
        dialogPane.addRow(3, new Label("Password:"), passwordField);

        Button submitButton = new Button("Add");
        submitButton.setOnAction(event -> {
            String platform = platformTextField.getText().trim();
            String name = nameTextField.getText().trim();
            String email = emailTextField.getText().trim();
            String password = passwordField.getText().trim();

            accountObject newAccount = new accountObject(platform, name, email, password);

            if (handleSql.insertNewAcc(newAccount)) {
                rs = null;
                this.rs = handleSql.selectAccounts();
                refreshTable();
            }
            dialog.close();
        });

        dialogPane.addRow(4, submitButton);

        Scene dialogScene = new Scene(dialogPane, 300, 200);
        dialog.setScene(dialogScene);
        dialog.showAndWait();
    }

    public void deleteAccount(accountObject rowData) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation Dialog");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to delete this account?");

        Image icon = new Image("/UIfiles/ico.png");
        Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
        alertStage.getIcons().add(icon);


        ButtonType confirmButton = new ButtonType("Confirm");
        ButtonType cancelButton = new ButtonType("Cancel");

        alert.getButtonTypes().setAll(confirmButton, cancelButton);

        alert.showAndWait().ifPresent(buttonType -> {
            if (buttonType == confirmButton) {
                if (this.handleSql.deleteAccount(rowData)) {
                    rs = null;
                    this.rs = handleSql.selectAccounts();
                    refreshTable();
                }
            }
        });
    }

    public void updateAccount(accountObject updateAccount) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Update Details");

        Image icon = new Image("/UIfiles/ico.png");
        dialog.getIcons().add(icon);

        GridPane dialogPane = new GridPane();
        dialogPane.setAlignment(Pos.CENTER);
        dialogPane.setPadding(new Insets(10));
        dialogPane.setHgap(10);
        dialogPane.setVgap(10);

        TextField platformTextField = new TextField();
        TextField nameTextField = new TextField();
        TextField emailTextField = new TextField();
        TextField passwordField = new TextField();


        platformTextField.setText(updateAccount.getUserPlatform());
        nameTextField.setText(updateAccount.getUserName());
        emailTextField.setText(updateAccount.getUserEmail());
        passwordField.setText(updateAccount.getUserPassword());

        dialogPane.addRow(0, new Label("Platform:"), platformTextField);
        dialogPane.addRow(1, new Label("Name:"), nameTextField);
        dialogPane.addRow(2, new Label("Email:"), emailTextField);
        dialogPane.addRow(3, new Label("Password:"), passwordField);

        Button submitButton = new Button("Update");
        submitButton.setOnAction(event -> {
            String platform = platformTextField.getText().trim();
            String name = nameTextField.getText().trim();
            String email = emailTextField.getText().trim();
            String password = passwordField.getText().trim();

            updateAccount.setUserPlatform(platform);
            updateAccount.setUserName(name);
            updateAccount.setUserEmail(email);
            updateAccount.setUserPassword(password);

            if (handleSql.updateAcc(updateAccount)) {
                rs = null;
                this.rs = handleSql.selectAccounts();
                refreshTable();
            }
            dialog.close();
        });

        dialogPane.addRow(4, submitButton);

        Scene dialogScene = new Scene(dialogPane, 300, 200);
        dialog.setScene(dialogScene);
        dialog.showAndWait();
    }

    private void reEncryptAllFields() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation Dialog");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to Re-encrypt? (note: this will delete all account history records)");

        Image icon = new Image("/UIfiles/ico.png");
        Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
        alertStage.getIcons().add(icon);

        ButtonType confirmButton = new ButtonType("Confirm");
        ButtonType cancelButton = new ButtonType("Cancel");

        alert.getButtonTypes().setAll(confirmButton, cancelButton);

        alert.showAndWait().ifPresent(buttonType -> {
            if (buttonType == confirmButton) {
                if (this.handleSql.deleteAllAcountRecords()) {
                    try {
                        this.rs = this.handleSql.selectAccounts();
                        SecretKey newKey = this.decryptHandler.generateAESKey();
                        this.decryptHandler.saveKeyToFile(newKey, "newKey.key");
                        this.decryptHandler.saveKeyTextToFile(this.decryptHandler.keyToBase64Text(newKey), "newKey.txt");

                        List<accountObject> reEncryptList = new ArrayList<>();

                        while (rs.next()) {
                            int accountId = rs.getInt(1);
                            String platform = rs.getString(2);
                            String name = this.decryptHandler.decrypt(rs.getString(3), this.key);
                            String email = this.decryptHandler.decrypt(rs.getString(4), this.key);
                            String password = this.decryptHandler.decrypt(rs.getString(5), this.key);
                            accountObject account = new accountObject(accountId, platform, name, email, password);
                            reEncryptList.add(account);
                        }
                        rs.close();

                        for (accountObject account : reEncryptList) {
                            this.handleSql.updateAcc(account, newKey);
                        }

                        this.key = newKey;
                        this.handleSql.setKey(newKey);
                        this.rs = this.handleSql.selectAccounts();
                        refreshTable();
                        passwordTextArea.setText("New key: " + this.decryptHandler.keyToBase64Text(this.key));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

    }

    private void refreshTable() {
        databaseTable.getItems().clear(); // Clear existing items
        try {
            while (rs.next()) {
                int accountId = rs.getInt(1);
                String platform = rs.getString(2);
                String name = this.decryptHandler.decrypt(rs.getString(3), this.key);
                String email = this.decryptHandler.decrypt(rs.getString(4), this.key);
                String password = this.decryptHandler.decrypt(rs.getString(5), this.key);

                accountObject account = new accountObject(accountId, platform, name, email, password);
                databaseTable.getItems().add(account);
            }
        } catch (Exception e) {
            this.passwordTextArea.setText("Decryption Error, the key provided is incorrect");
            e.printStackTrace();
        }
    }

    private void exportToJsonFile(boolean decrypted) {
        String appLocation = System.getProperty("user.dir");

        String formString = String.format("%s.json", this.handleSql.getFileName());
        String jsonFileLoc = appLocation + File.separator + formString;

        try {
            File newJsonFile = new File(jsonFileLoc);

            FileWriter fileWriter = new FileWriter(jsonFileLoc);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            ResultSet rawResult = this.handleSql.selectAccounts();

            ObservableList<accountObject> listOfAccounts = FXCollections.observableArrayList();

            try {
                while (rawResult.next()) {
                    if(decrypted){
                        int accountId = rawResult.getInt(1);
                        String platform = rawResult.getString(2);
                        String name = this.decryptHandler.decrypt(rawResult.getString(3),this.key);
                        String email = this.decryptHandler.decrypt(rawResult.getString(4), this.key);
                        String password = this.decryptHandler.decrypt(rawResult.getString(5), this.key);
                        accountObject account = new accountObject(accountId, platform, name, email, password);
                        listOfAccounts.add(account);

                    }
                    else{
                        int accountId = rawResult.getInt(1);
                        String platform = rawResult.getString(2);
                        String name = rawResult.getString(3);
                        String email = rawResult.getString(4);
                        String password = rawResult.getString(5);
                        accountObject account = new accountObject(accountId, platform, name, email, password);
                        listOfAccounts.add(account);
                    }
                }
            } catch (Exception e) {
                this.passwordTextArea.setText("ERROR GETTING ACCOUNTS");
                e.printStackTrace();
            }

            // Start of JSON array
            bufferedWriter.write("[\n");

            for (int i = 0; i < listOfAccounts.size(); i++) {
                accountObject eachAccount = listOfAccounts.get(i);
                String jsonAccount = String.format(
                        "  {\n" +
                                "    \"userPlatform\": \"%s\",\n" +
                                "    \"userName\": \"%s\",\n" +
                                "    \"userEmail\": \"%s\",\n" +
                                "    \"userPassword\": \"%s\"\n" +
                                "  }",
                        escapeJson(eachAccount.getUserPlatform()),
                        escapeJson(eachAccount.getUserName()),
                        escapeJson(eachAccount.getUserEmail()),
                        escapeJson(eachAccount.getUserPassword())
                );

                // Add comma after each JSON object except the last one
                if (i < listOfAccounts.size() - 1) {
                    jsonAccount += ",";
                }
                jsonAccount += "\n";

                bufferedWriter.write(jsonAccount);
            }

            // End of JSON array
            bufferedWriter.write("]\n");

            bufferedWriter.close();
            fileWriter.close();

            passwordTextArea.setText("Accounts Exported to JSON");
        } catch (IOException e) {
            passwordTextArea.setText("Accounts Failed to Export");
            e.printStackTrace();
        }
    }

    private void exportToTextFile() {
        String appLocation = System.getProperty("user.dir");

        String formString = String.format("%s.txt", this.handleSql.getFileName());
        String textFileLoc = appLocation + File.separator + formString;

        try {
            File newTxtFile = new File(textFileLoc);

            FileWriter fileWriter = new FileWriter(textFileLoc);

            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            ObservableList<accountObject> listOfAccounts = databaseTable.getItems();

            for (accountObject eachAccount : listOfAccounts) {
                String compilation = "User Platform: %s \n" +
                        "User Name: %s \n" +
                        "User Email: %s \n" +
                        "User Password: %s \n";

                bufferedWriter.write(String.format(compilation
                        , eachAccount.getUserPlatform()
                        , eachAccount.getUserName()
                        , eachAccount.getUserEmail()
                        , eachAccount.getUserPassword()));

                bufferedWriter.write("\n");
            }
            bufferedWriter.close();
            fileWriter.close();

            passwordTextArea.setText("Accounts Exported");
        } catch (Exception e) {
            passwordTextArea.setText("Accounts Failed to Export");
            e.printStackTrace();
        }
    }

    private void showExportDialog() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Export JSON");

        Image icon = new Image("/UIfiles/ico.png");
        dialog.getIcons().add(icon);


        GridPane dialogPane = new GridPane();
        dialogPane.setAlignment(Pos.CENTER);
        dialogPane.setPadding(new Insets(10));
        dialogPane.setHgap(10);
        dialogPane.setVgap(10);

        Label messageLabel = new Label("Do you want to export as Encrypted or Raw JSON?");
        Button encryptedButton = new Button("Encrypted");
        Button decryptedButton = new Button("Decrypted");

        encryptedButton.setOnAction(event -> {
            exportToJsonFile(false);
            dialog.close();
        });

        decryptedButton.setOnAction(event -> {
            exportToJsonFile(true);
            dialog.close();
        });

        dialogPane.add(messageLabel, 0, 0, 2, 1);
        dialogPane.add(encryptedButton, 0, 1);
        dialogPane.add(decryptedButton, 1, 1);

        Scene dialogScene = new Scene(dialogPane, 300, 150);
        dialog.setScene(dialogScene);
        dialog.showAndWait();
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\"", "\\\"")
                .replace("\\", "\\\\")
                .replace("/", "\\/")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private void importJson(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import Accounts via JSON");

        String currentWorkingDirectory = System.getProperty("user.dir");
        fileChooser.setInitialDirectory(new File(currentWorkingDirectory));

        FileChooser.ExtensionFilter jsonFilter = new FileChooser.ExtensionFilter("Key File (*.json)", "*.json");
        fileChooser.getExtensionFilters().addAll(jsonFilter);

        // Show the file chooser dialog and get the selected file
        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            // Perform operations on the selected file
            String filePath = selectedFile.getAbsolutePath();
            try {
               this.actionJSON.importDataJson(filePath,this.handleSql);
               this.rs = this.handleSql.selectAccounts();
                refreshTable();
                passwordTextArea.setText("ACCOUNTS LOADED");

            } catch (Exception e) {
                e.printStackTrace();
                passwordTextArea.setText("Error Loading Accounts via JSON");
            }


        }
    }

}
