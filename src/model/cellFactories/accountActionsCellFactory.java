package model.cellFactories;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import model.interfaces.accountActionsInterface;

public class accountActionsCellFactory<S> implements Callback<TableColumn<S, Void>, TableCell<S, Void>> {

    private final accountActionsInterface<S> actionHandler;

    public accountActionsCellFactory(accountActionsInterface<S> actionHandler){
        this.actionHandler = actionHandler;
    }

    @Override
    public TableCell<S, Void> call(TableColumn<S, Void> param) {
        return new TableCell<S, Void>() {
            private final Button deleteButton = new Button("Delete");
            private final Button updateButton = new Button("Update");

            {
                deleteButton.setOnAction(event -> {
                    S rowData = getTableRow().getItem();
                    actionHandler.deleteAccount(rowData);
                });

                updateButton.setOnAction(event -> {
                    S rowData = getTableRow().getItem();
                    actionHandler.updateAccount(rowData);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox buttonsBox = new HBox(updateButton, deleteButton);
                    buttonsBox.setSpacing(10);
                    setGraphic(buttonsBox);
                }
            }
        };
    }
}
