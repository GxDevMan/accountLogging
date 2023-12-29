package model;
import model.objects.accountObject;

import javax.crypto.SecretKey;
import java.io.File;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class sqlLiteHandler {

    private Connection connection;
    private SecretKey standardKey;
    private encryptdecryptHandler handleEncrypt;
    private String dbUrl;


    public sqlLiteHandler() {
        this.handleEncrypt = new encryptdecryptHandler();
    }

    public void setKey(SecretKey incomingKey) {
        this.standardKey = incomingKey;
    }

    public void reset() {
        this.connection = null;
        this.standardKey = null;
    }

    public boolean connecttoDB(String location) {
        try {
            File databaseCheck = new File(location);
            if (databaseCheck.exists()) {
                this.dbUrl = "jdbc:sqlite:" + location;
                this.connection = DriverManager.getConnection(dbUrl);
                return true;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean connectionCheck() {
        return this.connection != null;
    }

    public boolean createNew() {
        this.connection = null;
        Statement statement = null;

        String appLocation = System.getProperty("user.dir");
        String dataBaseLoc = appLocation + File.separator + "accountsLocal.db";

        try {
            // Establish a connection to the database
            File databaseCheck = new File(dataBaseLoc);

            if (!databaseCheck.exists()) {
                connection = DriverManager.getConnection("jdbc:sqlite:" + dataBaseLoc);
                this.handleEncrypt.saveKeyToFile(this.handleEncrypt.generateAESKey(), "defaultKey.key");
                // Create a statement
                statement = connection.createStatement();

                // Create the accounts table
                String createAccountsTableQuery = "CREATE TABLE IF NOT EXISTS Accounts (" +
                        "accountId INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "userPlatform TEXT, " +
                        "userName TEXT, " +
                        "userEmail TEXT, " +
                        "userPassword TEXT" +
                        ")";
                statement.executeUpdate(createAccountsTableQuery);

                // Create the changeLog table
                String createChangeLogTableQuery = "CREATE TABLE IF NOT EXISTS changeLog (" +
                        "changeLogId INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "accountId INTEGER, " +
                        "time DATETIME, " +
                        "userName TEXT, " +
                        "userEmail TEXT, " +
                        "userPassword TEXT, " +
                        "FOREIGN KEY (accountId) REFERENCES accounts(accountId)" +
                        ")";
                statement.executeUpdate(createAccountsTableQuery);
                statement.executeUpdate(createChangeLogTableQuery);
                return true;
            } else {
                return false;
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (statement != null)
                    statement.close();
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public boolean updateAcc(accountObject updatedAccount) {
        String updateQuery = "UPDATE Accounts SET userPlatform = ?, userName = ?, userEmail = ?, userPassword = ? WHERE accountId = ?";
        try {
            String platform = updatedAccount.getUserPlatform();
            String name = handleEncrypt.encrypt(updatedAccount.getUserName(), this.standardKey);
            String email = handleEncrypt.encrypt(updatedAccount.getUserEmail(), this.standardKey);
            String password = handleEncrypt.encrypt(updatedAccount.getUserPassword(), this.standardKey);

            Connection conn = DriverManager.getConnection(dbUrl);
            PreparedStatement pstmt = conn.prepareStatement(updateQuery);
            pstmt.setString(1, platform);
            pstmt.setString(2, name);
            pstmt.setString(3, email);
            pstmt.setString(4, password);
            pstmt.setInt(5, updatedAccount.getAccountID());
            int affectedRows = pstmt.executeUpdate();

            Date now = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            String formattedDateTime = sdf.format(now);

            updateQuery = "INSERT INTO changeLog (accountId, time, userName, userEmail, userPassword) VALUES (?, ?, ?, ?, ?)";
            pstmt = conn.prepareStatement(updateQuery);
            pstmt.setInt(1, updatedAccount.getAccountID());
            pstmt.setString(2, formattedDateTime);
            pstmt.setString(3, name);
            pstmt.setString(4, email);
            pstmt.setString(5, password);
            pstmt.executeUpdate();

            pstmt.close();
            conn.close();

            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateAcc(accountObject updatedAccount, SecretKey newKey) {
        String updateQuery = "UPDATE Accounts SET userPlatform = ?, userName = ?, userEmail = ?, userPassword = ? WHERE accountId = ?";
        try {
            String platform = updatedAccount.getUserPlatform();
            String name = handleEncrypt.encrypt(updatedAccount.getUserName(), newKey);
            String email = handleEncrypt.encrypt(updatedAccount.getUserEmail(), newKey);
            String password = handleEncrypt.encrypt(updatedAccount.getUserPassword(), newKey);

            Connection conn = DriverManager.getConnection(dbUrl);
            PreparedStatement pstmt = conn.prepareStatement(updateQuery);
            pstmt.setString(1, platform);
            pstmt.setString(2, name);
            pstmt.setString(3, email);
            pstmt.setString(4, password);
            pstmt.setInt(5, updatedAccount.getAccountID());
            pstmt.executeUpdate();

            conn.close();
            pstmt.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean insertNewAcc(accountObject newAccount) {
        String insertQuery = "INSERT INTO Accounts (userPlatform, userName, userEmail, userPassword) VALUES (?, ?, ?, ?)";
        try {
            String platform = newAccount.getUserPlatform();
            String name = handleEncrypt.encrypt(newAccount.getUserName(), this.standardKey);
            String email = handleEncrypt.encrypt(newAccount.getUserEmail(), this.standardKey);
            String password = handleEncrypt.encrypt(newAccount.getUserPassword(), this.standardKey);

            Connection conn = DriverManager.getConnection(dbUrl);
            PreparedStatement pstmt = conn.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, platform);
            pstmt.setString(2, name);
            pstmt.setString(3, email);
            pstmt.setString(4, password);
            int affectedRows = pstmt.executeUpdate();
            int generatedKey = -1;
            if (affectedRows > 0) {
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    generatedKey = generatedKeys.getInt(1);
                }
            }

            Date now = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            String formattedDateTime = sdf.format(now);

            insertQuery = "INSERT INTO changeLog (accountId, time, userName, userEmail, userPassword) VALUES (?, ?, ?, ?, ?)";
            pstmt = conn.prepareStatement(insertQuery);
            pstmt.setInt(1, generatedKey);
            pstmt.setString(2, formattedDateTime);
            pstmt.setString(3, name);
            pstmt.setString(4, email);
            pstmt.setString(5, password);
            pstmt.executeUpdate();

            pstmt.close();
            conn.close();

            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteAccount(accountObject accountDeletion) {
        String deleteQuery = "DELETE FROM changeLog WHERE accountId = ?";
        try {
            Connection conn = DriverManager.getConnection(dbUrl);
            PreparedStatement pstmt = conn.prepareStatement(deleteQuery);
            pstmt.setInt(1,accountDeletion.getAccountID());
            pstmt.executeUpdate();

            deleteQuery = "DELETE FROM Accounts WHERE accountId = ?";
            pstmt = conn.prepareStatement(deleteQuery);
            pstmt.setInt(1,accountDeletion.getAccountID());
            pstmt.executeUpdate();

            conn.close();
            pstmt.close();

            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteAccountRecord(accountObject accountDeletion) {
        String deleteQuery = "DELETE FROM changeLog WHERE changeLogId = ?";
        try {
            Connection conn = DriverManager.getConnection(dbUrl);
            PreparedStatement pstmt = conn.prepareStatement(deleteQuery);
            pstmt.setInt(1,accountDeletion.getChangeLogID());
            pstmt.executeUpdate();

            conn.close();
            pstmt.close();

            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteAllAcountRecords() {
        String deleteQuery = "DELETE FROM changeLog";
        try {
            Connection conn = DriverManager.getConnection(dbUrl);
            PreparedStatement pstmt = conn.prepareStatement(deleteQuery);
            pstmt.executeUpdate();
            pstmt.close();
            conn.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public ResultSet selectAccounts() {
        ResultSet rs = null;
        if (!this.connectionCheck()) {
            System.out.println("No Connection");
            return rs;
        }

        try {
            Connection conn = DriverManager.getConnection(dbUrl);
            String selectQuery = "SELECT * FROM Accounts";
            PreparedStatement pstmt = conn.prepareStatement(selectQuery);
            rs = pstmt.executeQuery();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rs;
    }

    public ResultSet selectAccounts(String searchKey) {
        ResultSet rs = null;
        if (!this.connectionCheck()) {
            System.out.println("No Connection");
            return rs;
        }

        try {
            Connection conn = DriverManager.getConnection(dbUrl);
            String selectQuery = "SELECT * FROM Accounts WHERE userPlatform LIKE ?";
            PreparedStatement pstmt = conn.prepareStatement(selectQuery);
            pstmt.setString(1,"%" + searchKey + "%");
            rs = pstmt.executeQuery();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rs;
    }

    public ResultSet selectAccountHistory() {
        ResultSet rs = null;
        if (!this.connectionCheck()) {
            System.out.println("No Connection");
            return rs;
        }

        try {
            Connection conn = DriverManager.getConnection(dbUrl);
            String selectQuery = "SELECT changeLog.changeLogId, " +
                    "Accounts.userPlatform, " +
                    "changeLog.time, " +
                    "changeLog.userName, " +
                    "changeLog.userEmail, " +
                    "changeLog.userPassword " +
                    "FROM changeLog JOIN Accounts ON " +
                    "Accounts.accountId = changeLog.accountId";
            PreparedStatement pstmt = conn.prepareStatement(selectQuery);
            rs = pstmt.executeQuery();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rs;
    }
}
