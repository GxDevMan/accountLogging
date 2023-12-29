package contollers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import model.*;
import model.cellFactories.changeLogActionsCellFactory;
import model.interfaces.changeLogActionsInterface;
import model.objects.accountObject;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;

public class changeLogUIController implements Initializable, changeLogActionsInterface<accountObject> {
    private sqlLiteHandler handleSql;
    private SecretKey key;
    private String dbLoc;
    private ResultSet rs;
    private encryptdecryptHandler handleDecrypt;

    @FXML
    private TableView<accountObject> databaseTable;

    @FXML
    private TableColumn<accountObject, String> timeChange;
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

    public changeLogUIController(){
        this.handleDecrypt = new encryptdecryptHandler();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.timeChange.setCellValueFactory(new PropertyValueFactory<>("time"));
        this.userPlatformColumn.setCellValueFactory(new PropertyValueFactory<>("userPlatform"));
        this.userNameColumn.setCellValueFactory(new PropertyValueFactory<>("userName"));
        this.userEmailColumn.setCellValueFactory(new PropertyValueFactory<>("userEmail"));
        this.userPasswordColumn.setCellValueFactory(new PropertyValueFactory<>("userPassword"));

        this.actionColumn.setCellFactory(new changeLogActionsCellFactory<>(this));
    }

    public void setController(sqlLiteHandler handleSql, SecretKey key, String dbLoc) {
        this.handleSql = handleSql;
        this.key = key;
        this.dbLoc = dbLoc;
        this.rs = this.handleSql.selectAccountHistory();
        refreshTable();
    }

    @Override
    public void deleteRecord(accountObject rowData) {

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation Dialog");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to delete this record?");

        Image icon = new Image("/UIfiles/ico.png");
        Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
        alertStage.getIcons().add(icon);

        ButtonType confirmButton = new ButtonType("Confirm");
        ButtonType cancelButton = new ButtonType("Cancel");

        alert.getButtonTypes().setAll(confirmButton, cancelButton);

        alert.showAndWait().ifPresent(buttonType -> {
            if (buttonType == confirmButton) {
                if (this.handleSql.deleteAccountRecord(rowData)) {
                    rs = null;
                    this.rs = handleSql.selectAccountHistory();
                    refreshTable();
                }
            }
        });
    }

    public void changeScene(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/UIfiles/accountsUI.fxml"));
        Parent viewParent = loader.load();
        Scene viewScene = new Scene(viewParent);

        AccountsUIController controller = loader.getController();
        controller.setController(this.handleSql, this.key, this.dbLoc);

        Stage sourceWin = (Stage) ((Node) event.getSource()).getScene().getWindow();
        sourceWin.setScene(viewScene);
        sourceWin.show();
        return;
    }

    private void refreshTable() {
        databaseTable.getItems().clear();

        try {
            while (rs.next()){
                int changeLogId = rs.getInt(1);
                String userPlatform = rs.getString(2);
                String time = convertToReadableFormat(rs.getString(3));
                String name = this.handleDecrypt.decrypt(rs.getString(4),this.key);
                String email = this.handleDecrypt.decrypt(rs.getString(5),this.key);
                String password =this.handleDecrypt.decrypt(rs.getString(6),this.key);

                accountObject accountRecord = new accountObject(changeLogId,userPlatform,time,name,email,password);
                databaseTable.getItems().add(accountRecord);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @FXML
    private void deleteAllLogs(){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation Dialog");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to delete all Logs?");

        Image icon = new Image("/UIfiles/ico.png");
        Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
        alertStage.getIcons().add(icon);

        ButtonType confirmButton = new ButtonType("Confirm");
        ButtonType cancelButton = new ButtonType("Cancel");

        alert.getButtonTypes().setAll(confirmButton, cancelButton);

        alert.showAndWait().ifPresent(buttonType -> {
            if (buttonType == confirmButton) {
                if (this.handleSql.deleteAllAcountRecords()) {
                    rs = null;
                    refreshTable();
                }
            }
        });
    }

    private String convertToReadableFormat(String timeFormat) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            Date date = inputFormat.parse(timeFormat);

            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a");
            return outputFormat.format(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
