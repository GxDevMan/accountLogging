package model.objects;

public class accountObject {

    private int accountID;
    private int changeLogID;
    private String time;
    private String userPlatform;
    private String userName;
    private String userEmail;
    private String userPassword;

    public accountObject(int accountID, String userPlatform, String userName, String userEmail, String userPassword){
        this.accountID = accountID;
        this.userPlatform = userPlatform;
        this.userName = userName;
        this.userEmail = userEmail;
        this.userPassword = userPassword;
    }

    public accountObject(String userPlatform, String userName, String userEmail, String userPassword){
        this.userPlatform = userPlatform;
        this.userName = userName;
        this.userEmail = userEmail;
        this.userPassword = userPassword;
    }

    public accountObject(int changeLogID, String userPlatform, String time, String userName, String userEmail, String userPassword){
        this.changeLogID = changeLogID;
        this.time = time;
        this.userPlatform = userPlatform;
        this.userName = userName;
        this.userEmail = userEmail;
        this.userPassword = userPassword;
    }

    public int getAccountID() {
        return accountID;
    }

    public int getChangeLogID() {
        return changeLogID;
    }

    public String getTime(){
        return time;
    }

    public String getUserPlatform() {
        return userPlatform;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPlatform(String userPlatform) {
        this.userPlatform = userPlatform;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

}
