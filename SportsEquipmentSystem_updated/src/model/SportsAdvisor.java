package model;

public class SportsAdvisor extends User {
    private String employeeCode;

    public SportsAdvisor(String userId, String name, String password, String employeeCode) {
        super(userId, name, password, "ADVISOR");
        this.employeeCode = employeeCode;
    }

    public String getEmployeeCode() { return employeeCode; }
}
