package model;

public class Faculty extends User {
    private String designation;

    public Faculty(String userId, String name, String password, String designation) {
        super(userId, name, password, "FACULTY");
        this.designation = designation;
    }

    public String getDesignation() { return designation; }
}
