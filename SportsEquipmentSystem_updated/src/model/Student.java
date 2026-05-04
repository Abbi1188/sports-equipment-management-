package model;

public class Student extends User {
    private String department;
    private boolean isBlacklisted;

    public Student(String userId, String name, String password, String department) {
        super(userId, name, password, "STUDENT");
        this.department = department;
        this.isBlacklisted = false;
    }

    public String getDepartment()       { return department; }
    public boolean isBlacklisted()      { return isBlacklisted; }
    public void setBlacklisted(boolean b) { this.isBlacklisted = b; }
}
