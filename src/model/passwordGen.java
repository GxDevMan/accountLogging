package model;
import java.security.SecureRandom;

public class passwordGen {
    private String Characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()";
    public passwordGen(){

    }

    public passwordGen(String charList){
        this.Characters = charList;
    }

    public String generatePassword(int length) {
        StringBuilder password = new StringBuilder();
        SecureRandom random = new SecureRandom();

        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(Characters.length());
            char randomChar = Characters.charAt(randomIndex);
            password.append(randomChar);
        }

        return password.toString();
    }
}
