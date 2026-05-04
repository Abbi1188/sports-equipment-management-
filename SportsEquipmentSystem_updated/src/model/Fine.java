package model;

import java.util.Date;

public class Fine {
    private String fineId;
    private String recordId;
    private String studentId;
    private String studentName;
    private int    overdueDays;
    private double fineAmount;
    private String fineStatus;        // "UNPAID", "PAID"
    private String fineReason;        // "OVERDUE" or "DAMAGE"
    private Date   issuedDate;

    public static final double RATE_PER_DAY    = 100.0;  // PKR per overdue day
    public static final double DAMAGE_FINE_AMT = 500.0;  // PKR flat damage fine

    /** Constructor for OVERDUE fines. */
    public Fine(String fineId, String recordId,
                String studentId, String studentName, int overdueDays) {
        this.fineId      = fineId;
        this.recordId    = recordId;
        this.studentId   = studentId;
        this.studentName = studentName;
        this.overdueDays = overdueDays;
        this.fineAmount  = overdueDays * RATE_PER_DAY;
        this.fineStatus  = "UNPAID";
        this.fineReason  = "OVERDUE";
        this.issuedDate  = new Date();
    }

    /** Constructor for DAMAGE fines (condition score <= 3). */
    public Fine(String fineId, String recordId,
                String studentId, String studentName,
                int conditionScore, String reason) {
        this.fineId      = fineId;
        this.recordId    = recordId;
        this.studentId   = studentId;
        this.studentName = studentName;
        this.overdueDays = 0;
        this.fineAmount  = DAMAGE_FINE_AMT;
        this.fineStatus  = "UNPAID";
        this.fineReason  = reason;  // "DAMAGE"
        this.issuedDate  = new Date();
    }

    // ---- Getters ----
    public String getFineId()      { return fineId; }
    public String getRecordId()    { return recordId; }
    public String getStudentId()   { return studentId; }
    public String getStudentName() { return studentName; }
    public int    getOverdueDays() { return overdueDays; }
    public double getFineAmount()  { return fineAmount; }
    public String getFineStatus()  { return fineStatus; }
    public String getFineReason()  { return fineReason; }
    public Date   getIssuedDate()  { return issuedDate; }

    // ---- Setter ----
    public void setFineStatus(String status) { this.fineStatus = status; }

    @Override
    public String toString() {
        return "Fine [" + fineId + "] - " + studentName
                + " | Reason: " + fineReason
                + " | Days: " + overdueDays
                + " | Amount: PKR " + fineAmount
                + " | " + fineStatus;
    }
}
