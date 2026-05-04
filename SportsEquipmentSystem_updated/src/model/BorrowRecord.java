package model;

import java.util.Date;

public class BorrowRecord {
    private String recordId;
    private String requestId;
    private String studentId;
    private String studentName;
    private String equipmentId;
    private String equipmentName;
    private Date   borrowDate;
    private Date   dueDate;
    private Date   returnDate;       // null until returned
    private int    conditionScore;   // 1-10, set on return
    private String recordStatus;     // "ACTIVE", "RETURNED"

    public BorrowRecord(String recordId, String requestId,
                        String studentId, String studentName,
                        String equipmentId, String equipmentName,
                        Date borrowDate, Date dueDate) {
        this.recordId      = recordId;
        this.requestId     = requestId;
        this.studentId     = studentId;
        this.studentName   = studentName;
        this.equipmentId   = equipmentId;
        this.equipmentName = equipmentName;
        this.borrowDate    = borrowDate;
        this.dueDate       = dueDate;
        this.returnDate    = null;
        this.conditionScore = 10;
        this.recordStatus  = "ACTIVE";
    }

    // ---- Getters ----
    public String getRecordId()      { return recordId; }
    public String getRequestId()     { return requestId; }
    public String getStudentId()     { return studentId; }
    public String getStudentName()   { return studentName; }
    public String getEquipmentId()   { return equipmentId; }
    public String getEquipmentName() { return equipmentName; }
    public Date   getBorrowDate()    { return borrowDate; }
    public Date   getDueDate()       { return dueDate; }
    public Date   getReturnDate()    { return returnDate; }
    public int    getConditionScore(){ return conditionScore; }
    public String getRecordStatus()  { return recordStatus; }

    // ---- Setters ----
    public void setReturnDate(Date returnDate)      { this.returnDate = returnDate; }
    public void setConditionScore(int score)        { this.conditionScore = score; }
    public void setRecordStatus(String recordStatus){ this.recordStatus = recordStatus; }
}
