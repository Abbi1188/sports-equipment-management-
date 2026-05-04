package model;

import java.util.Date;

public class BorrowRequest {
    private String requestId;
    private String studentId;
    private String studentName;
    private String equipmentId;
    private String equipmentName;
    private Date requestDate;
    private Date expectedReturnDate;
    private String status;   // "PENDING", "APPROVED", "REJECTED"
    private String purpose;

    public BorrowRequest(String requestId, String studentId, String studentName,
                         String equipmentId, String equipmentName,
                         Date expectedReturnDate, String purpose) {
        this.requestId        = requestId;
        this.studentId        = studentId;
        this.studentName      = studentName;
        this.equipmentId      = equipmentId;
        this.equipmentName    = equipmentName;
        this.requestDate      = new Date();
        this.expectedReturnDate = expectedReturnDate;
        this.status           = "PENDING";
        this.purpose          = purpose;
    }

    // ---- Getters ----
    public String getRequestId()          { return requestId; }
    public String getStudentId()          { return studentId; }
    public String getStudentName()        { return studentName; }
    public String getEquipmentId()        { return equipmentId; }
    public String getEquipmentName()      { return equipmentName; }
    public Date   getRequestDate()        { return requestDate; }
    public Date   getExpectedReturnDate() { return expectedReturnDate; }
    public String getStatus()             { return status; }
    public String getPurpose()            { return purpose; }

    // ---- Setter ----
    public void setStatus(String status) { this.status = status; }
}
