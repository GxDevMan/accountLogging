package model.cellFactories;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import model.interfaces.changeLogActionsInterface;

public class changeLogActionsCellFactory<S> implements Callback<TableColumn<S, Void>, TableCell<S, Void>> {

    private final changeLogActionsInterface<S> actionHandler;

    public changeLogActionsCellFactory(changeLogActionsInterface<S> actionHandler){
        this.actionHandler = actionHandler;
    }

    @Override
    public TableCell<S, Void> call(TableColumn<S, Void> param) {
        return new TableCell<S, Void>() {
            private final Button deleteButton = new Button("Delete");
            {
                deleteButton.setOnAction(event -> {
                    S rowData = getTableRow().getItem();
                    actionHandler.deleteRecord(rowData);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox buttonsBox = new HBox(deleteButton);
                    buttonsBox.setSpacing(10);
                    setGraphic(buttonsBox);
                }
            }
        };
    }
}

