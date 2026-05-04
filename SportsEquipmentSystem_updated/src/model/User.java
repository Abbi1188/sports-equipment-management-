package model;

public abstract class User {
    protected String userId;
    protected String name;
    protected String password;
    protected String role; // "STUDENT", "FACULTY", "ADVISOR"

    public User(String userId, String name, String password, String role) {
        this.userId = userId;
        this.name = name;
        this.password = password;
        this.role = role;
    }

    public String getUserId()   { return userId; }
    public String getName()     { return name; }
    public String getPassword() { return password; }
    public String getRole()     { return role; }

    @Override
    public String toString() {
        return name + " [" + role + "]";
    }
}
