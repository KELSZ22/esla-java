package com.kelsz.esla;

/**
 * Singleton class to store current user session.
 */
public class UserSession {
    private static UserSession instance;
    private String name;
    private String email;

    private UserSession() {}

    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    public void setUser(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public String getName() {
        return name != null ? name : "Guest";
    }

    public String getEmail() {
        return email;
    }

    public void logout() {
        name = null;
        email = null;
    }
}
