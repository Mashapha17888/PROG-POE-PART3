package chatapp;

public class Login {
    private String registeredUsername;
    private String registeredPassword;
    private String registeredPhoneNumber;
    private String firstName;
    private String lastName;

    public boolean checkUserName(String username) {
        return username.contains("_") && username.length() <= 5;
    }

    public boolean checkPasswordComplexity(String password) {
        return password.length() >= 8 &&
               password.matches(".*[A-Z].*") &&
               password.matches(".*[0-9].*") &&
               password.matches(".*[!@#\\$%\\^&\\(\\)\\-+=<>?/{}~|].*");
    }

    public boolean checkCellPhoneNumber(String phoneNumber) {
        return phoneNumber.matches("^\\+27\\d{9}$");
    }

    public String registerUser(String username, String password, String phoneNumber, String firstName, String lastName) {
        if (!checkUserName(username)) {
            return "Username must contain underscore (_) and be at most 5 characters.";
        }
        if (!checkPasswordComplexity(password)) {
            return "Password must be at least 8 chars, include uppercase, number, and special char.";
        }
        if (!checkCellPhoneNumber(phoneNumber)) {
            return "Phone number must be in format +27XXXXXXXXX.";
        }
        this.registeredUsername = username;
        this.registeredPassword = password;
        this.registeredPhoneNumber = phoneNumber;
        this.firstName = firstName;
        this.lastName = lastName;
        return "OK";
    }

    public String getWelcomeMessage() {
        return "Welcome " + firstName + " " + lastName + ", it is great to see you.";
    }

    public boolean validateLogin(String username, String password) {
        return username.equals(registeredUsername) && password.equals(registeredPassword);
    }
}
