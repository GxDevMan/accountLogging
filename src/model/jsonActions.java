package model;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import model.objects.accountObject;

public class jsonActions {

    public boolean importDataJson(String jsonLoc, sqlLiteHandler handleSql){
        String filePath = jsonLoc;
        try (FileReader reader = new FileReader(filePath)) {
            JsonElement jsonElement = JsonParser.parseReader(reader);
            JsonArray jsonArray = jsonElement.getAsJsonArray();

            ArrayList<accountObject> newAccountList = new ArrayList<>();

            for (JsonElement element : jsonArray) {
                accountObject newAccount = getAccountObject(element);
                newAccountList.add(newAccount);
            }
            return handleSql.insertNewAccBatch(newAccountList);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static accountObject getAccountObject(JsonElement element) {
        JsonObject jsonObject = element.getAsJsonObject();

        // Extract values using keys
        String userPlatform = jsonObject.get("userPlatform").getAsString();
        String userName = jsonObject.get("userName").getAsString();
        String userEmail = jsonObject.get("userEmail").getAsString();
        String userPassword = jsonObject.get("userPassword").getAsString();
        accountObject newAccount = new accountObject(userPlatform, userName, userEmail,userPassword);
        return newAccount;
    }


}
