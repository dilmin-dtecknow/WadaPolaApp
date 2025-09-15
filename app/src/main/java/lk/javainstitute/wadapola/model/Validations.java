package lk.javainstitute.wadapola.model;

public class Validations {
    public static boolean isEmailValide(String email) {
        return email.matches("^[a-zA-Z0-9_!#$%&’*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$");
    }

    public static boolean isPasswordValide(String password) {
        return password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=]).{8,}$");
    }

    public static boolean isDouble(String text) {
        return text.matches("^\\d+(\\.\\d{2})?$");
    }

    public static boolean isInteger(String text) {
        return text.matches("^\\d+$");
    }

    public static boolean isMobileNumberValide(String mobile) {
        return mobile.matches("^07[0,1,2,4,5,6,7,8]{1}[0-9]{7}$");
    }
}
