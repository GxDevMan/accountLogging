package model.interfaces;

public interface accountActionsInterface<S> {
    void deleteAccount(S rowData);
    void updateAccount(S rowData);
}
