package data;

import model.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * DataStore acts as an in-memory database for the whole application.
 * Single instance shared across all GUI panels (Singleton pattern).
 */
public class DataStore {

    // ---- Singleton ----
    private static DataStore instance = null;

    public static DataStore getInstance() {
        if (instance == null) {
            instance = new DataStore();
        }
        return instance;
    }

    // ---- Data Lists ----
    private List<User>          users         = new ArrayList<>();
    private List<Equipment>     equipmentList = new ArrayList<>();
    private List<BorrowRequest> requests      = new ArrayList<>();
    private List<BorrowRecord>  records       = new ArrayList<>();
    private List<Fine>          fines         = new ArrayList<>();

    // ---- ID Counters ----
    private int reqCounter  = 1;
    private int recCounter  = 1;
    private int fineCounter = 1;

    // ---- Currently logged-in user ----
    private User currentUser = null;

    // ---- Constructor seeds demo data ----
    private DataStore() {
        seedUsers();
        seedEquipment();
    }

    private void seedUsers() {
        users.add(new Student("24p-0570", "Qazi Abdur Rahman", "std01",   "Computer Science"));
        users.add(new Student("24p-0518", "Abdul Rehman",      "std02",   "Electrical Eng."));
        users.add(new Student("24p-0668", "Yahya Bin Zia",     "std03",   "Mechanical Eng."));
        users.add(new Faculty("F001",     "Dr. Imran Shah", "imran00", "Associate Professor"));
        users.add(new SportsAdvisor("T-0011", "Coach Tariq", "coach1", "EMP-2024"));
    }

    private void seedEquipment() {
        equipmentList.add(new Equipment("E001", "Cricket Bat",      "Cricket",   5));
        equipmentList.add(new Equipment("E002", "Football",         "Football",  8));
        equipmentList.add(new Equipment("E003", "Badminton Racket", "Badminton", 6));
        equipmentList.add(new Equipment("E004", "Tennis Racket",    "Tennis",    4));
        equipmentList.add(new Equipment("E005", "Volleyball",       "Volleyball",3));
        equipmentList.add(new Equipment("E006", "Basketball",       "Basketball",4));
        equipmentList.add(new Equipment("E007", "Cricket Pads",     "Cricket",   3));
        equipmentList.add(new Equipment("E008", "Boxing Gloves",    "Boxing",    2));
    }

    // ---- Login ----
    public User login(String userId, String password) {
        if (userId == null || userId.trim().isEmpty()) return null;
        if (password == null || password.trim().isEmpty()) return null;
        for (User temp : users) {
            if (temp.getUserId().equalsIgnoreCase(userId.trim())
                    && temp.getPassword().equals(password.trim())) {
                currentUser = temp;
                return temp;
            }
        }
        return null;
    }

    /** Returns true if a user with the given ID exists (regardless of password). */
    public boolean userExists(String userId) {
        if (userId == null || userId.trim().isEmpty()) return false;
        for (User temp : users) {
            if (temp.getUserId().equalsIgnoreCase(userId.trim())) return true;
        }
        return false;
    }

    public void logout() { currentUser = null; }
    public User getCurrentUser() { return currentUser; }

    // ---- Users ----
    public List<User> getUsers() { return users; }

    public User findUser(String userId) {
        if (userId == null || userId.trim().isEmpty()) return null;
        for (User temp : users) {
            if (temp.getUserId().equalsIgnoreCase(userId.trim())) return temp;
        }
        return null;
    }

    // ---- Equipment ----
    public List<Equipment> getEquipmentList() { return equipmentList; }

    public Equipment findEquipment(String equipmentId) {
        if (equipmentId == null || equipmentId.trim().isEmpty()) return null;
        for (Equipment temp : equipmentList) {
            if (temp.getEquipmentId().equalsIgnoreCase(equipmentId.trim())) return temp;
        }
        return null;
    }

    // ---- Borrow Requests ----
    public List<BorrowRequest> getRequests() { return requests; }

    public List<BorrowRequest> getRequestsByStudent(String studentId) {
        List<BorrowRequest> result = new ArrayList<>();
        if (studentId == null || studentId.trim().isEmpty()) return result;
        for (BorrowRequest temp : requests) {
            if (temp.getStudentId().equalsIgnoreCase(studentId.trim())) result.add(temp);
        }
        return result;
    }

    public List<BorrowRequest> getPendingRequests() {
        List<BorrowRequest> result = new ArrayList<>();
        for (BorrowRequest temp : requests) {
            if (temp.getStatus().equals("PENDING")) result.add(temp);
        }
        return result;
    }

    public String submitRequest(String studentId, String equipmentId, Date returnDate, String purpose) {
        // --- Input Validation ---
        if (studentId == null || studentId.trim().isEmpty())
            return "Student ID cannot be empty.";
        if (equipmentId == null || equipmentId.trim().isEmpty())
            return "Equipment ID cannot be empty.";
        if (purpose == null || purpose.trim().isEmpty())
            return "Purpose cannot be empty.";
        if (purpose.trim().length() < 3)
            return "Purpose is too short. Please provide a meaningful description.";
        if (purpose.trim().length() > 200)
            return "Purpose is too long. Maximum 200 characters allowed.";
        if (returnDate == null)
            return "Return date is invalid.";
        if (!returnDate.after(new Date()))
            return "Return date must be in the future.";

        User user = findUser(studentId);
        if (user == null || !(user instanceof Student))
            return "Student ID '" + studentId.trim() + "' not found in the system.";

        Student student = (Student) user;
        if (student.isBlacklisted())
            return "You are blacklisted and cannot submit requests. Contact the Sports Advisor.";

        // Duplicate pending request check
        for (BorrowRequest req : requests) {
            if (req.getStudentId().equalsIgnoreCase(studentId.trim())
                    && req.getEquipmentId().equalsIgnoreCase(equipmentId.trim())
                    && req.getStatus().equals("PENDING")) {
                return "You already have a pending request for this equipment (ID: " + req.getRequestId() + ").";
            }
        }

        // Already has active record for same equipment
        for (BorrowRecord rec : records) {
            if (rec.getStudentId().equalsIgnoreCase(studentId.trim())
                    && rec.getEquipmentId().equalsIgnoreCase(equipmentId.trim())
                    && rec.getRecordStatus().equals("ACTIVE")) {
                return "You already have this equipment checked out (Record: " + rec.getRecordId() + "). Return it first.";
            }
        }

        Equipment eq = findEquipment(equipmentId);
        if (eq == null)
            return "Equipment ID '" + equipmentId.trim() + "' does not exist. Check the Equipment tab for valid IDs.";
        if (eq.getStatus().equals("REPAIR"))
            return "'" + eq.getName() + "' is currently under repair and unavailable.";
        if (!eq.isAvailable())
            return "'" + eq.getName() + "' is not available. All units are currently checked out.";

        String newRequestId = "REQ" + String.format("%03d", reqCounter++);
        BorrowRequest newReq = new BorrowRequest(
                newRequestId, studentId.trim(), user.getName(),
                equipmentId.trim().toUpperCase(), eq.getName(), returnDate, purpose.trim());
        requests.add(newReq);
        return "SUCCESS:" + newRequestId;
    }

    // ---- Advisor Actions ----
    public String approveRequest(String requestId) {
        if (requestId == null || requestId.trim().isEmpty())
            return "Request ID cannot be empty.";

        BorrowRequest req = findRequest(requestId.trim().toUpperCase());
        if (req == null)
            return "Request ID '" + requestId.trim() + "' not found.";
        if (req.getStatus().equals("APPROVED"))
            return "This request is already approved.";
        if (req.getStatus().equals("REJECTED"))
            return "This request was already rejected and cannot be approved.";
        if (!req.getStatus().equals("PENDING"))
            return "Only PENDING requests can be approved.";

        Equipment eq = findEquipment(req.getEquipmentId());
        if (eq == null)
            return "Equipment no longer exists in the system.";
        if (eq.getStatus().equals("REPAIR"))
            return "'" + eq.getName() + "' is under repair and cannot be approved.";
        if (!eq.isAvailable())
            return "'" + eq.getName() + "' is no longer available.";

        req.setStatus("APPROVED");
        eq.decreaseAvailable();

        String newRecordId = "REC" + String.format("%03d", recCounter++);
        BorrowRecord newRecord = new BorrowRecord(
                newRecordId, requestId.trim().toUpperCase(),
                req.getStudentId(), req.getStudentName(),
                req.getEquipmentId(), req.getEquipmentName(),
                new Date(), req.getExpectedReturnDate());
        records.add(newRecord);
        return "SUCCESS:" + newRecordId;
    }

    public String rejectRequest(String requestId) {
        if (requestId == null || requestId.trim().isEmpty())
            return "Request ID cannot be empty.";

        BorrowRequest req = findRequest(requestId.trim().toUpperCase());
        if (req == null)
            return "Request ID '" + requestId.trim() + "' not found.";
        if (req.getStatus().equals("REJECTED"))
            return "This request is already rejected.";
        if (req.getStatus().equals("APPROVED"))
            return "This request is already approved and cannot be rejected now.";
        if (!req.getStatus().equals("PENDING"))
            return "Only PENDING requests can be rejected.";

        req.setStatus("REJECTED");
        return "SUCCESS";
    }

    // ---- Return Processing ----
    public String processReturn(String recordId, int conditionScore) {
        if (recordId == null || recordId.trim().isEmpty())
            return "Record ID cannot be empty.";
        if (conditionScore < 1)
            return "Condition score cannot be negative or zero. Enter a value between 1 and 10.";
        if (conditionScore > 10)
            return "Condition score cannot exceed 10. Enter a value between 1 and 10.";

        BorrowRecord rec = findRecord(recordId.trim().toUpperCase());
        if (rec == null)
            return "Record ID '" + recordId.trim() + "' not found.";
        if (rec.getRecordStatus().equals("RETURNED"))
            return "This record has already been returned. Cannot process again.";
        if (!rec.getRecordStatus().equals("ACTIVE"))
            return "Only ACTIVE records can be returned.";

        Date today = new Date();
        rec.setReturnDate(today);
        rec.setConditionScore(conditionScore);
        rec.setRecordStatus("RETURNED");

        Equipment eq = findEquipment(rec.getEquipmentId());
        if (eq != null) {
            if (conditionScore <= 3) {
                eq.setStatus("REPAIR");
                // do NOT increase available — item is going to repair
            } else {
                eq.increaseAvailable();
            }
        }

        // ---- Check for DAMAGE fine (condition score <= 3) ----
        boolean damageFineIssued = false;
        String  damageFineId     = "";
        if (conditionScore <= 3) {
            damageFineId = "FIN" + String.format("%03d", fineCounter++);
            Fine damageFine = new Fine(damageFineId, recordId.trim().toUpperCase(),
                    rec.getStudentId(), rec.getStudentName(),
                    conditionScore, "DAMAGE");
            fines.add(damageFine);
            damageFineIssued = true;
        }

        // ---- Check for OVERDUE fine ----
        long   diffMs        = today.getTime() - rec.getDueDate().getTime();
        long   diffDays      = diffMs / (1000L * 60 * 60 * 24);
        boolean overdueFineIssued = false;
        String  overdueFineId     = "";
        if (diffDays > 0) {
            overdueFineId = "FIN" + String.format("%03d", fineCounter++);
            Fine overdueFine = new Fine(overdueFineId, recordId.trim().toUpperCase(),
                    rec.getStudentId(), rec.getStudentName(), (int) diffDays);
            fines.add(overdueFine);
            overdueFineIssued = true;
        }

        // ---- Build result message ----
        if (damageFineIssued && overdueFineIssued) {
            return "FINE_BOTH:" + damageFineId + ":" + overdueFineId
                    + ":PKR " + Fine.DAMAGE_FINE_AMT + " (damage)"
                    + " + PKR " + (diffDays * Fine.RATE_PER_DAY)
                    + " (" + diffDays + " overdue day(s))";
        } else if (damageFineIssued) {
            return "FINE_DAMAGE:" + damageFineId
                    + ":Equipment returned in poor condition (score=" + conditionScore + ")."
                    + " Damage fine: PKR " + Fine.DAMAGE_FINE_AMT
                    + ". Item flagged for REPAIR.";
        } else if (overdueFineIssued) {
            return "FINE_OVERDUE:" + overdueFineId
                    + ":PKR " + (diffDays * Fine.RATE_PER_DAY)
                    + " for " + diffDays + " overdue day(s).";
        }
        return "SUCCESS:Returned on time. No fine issued.";
    }

    // ---- Blacklist ----
    public String blacklistStudent(String studentId) {
        if (studentId == null || studentId.trim().isEmpty())
            return "Student ID cannot be empty.";

        User user = findUser(studentId.trim());
        if (user == null)
            return "ID '" + studentId.trim() + "' not found in the system.";
        if (!(user instanceof Student))
            return "'" + studentId.trim() + "' is not a Student account and cannot be blacklisted.";

        Student s = (Student) user;
        if (s.isBlacklisted())
            return "Student '" + s.getName() + "' is already blacklisted.";

        s.setBlacklisted(true);
        return "SUCCESS";
    }

    public String removeBlacklist(String studentId) {
        if (studentId == null || studentId.trim().isEmpty())
            return "Student ID cannot be empty.";

        User user = findUser(studentId.trim());
        if (user == null)
            return "ID '" + studentId.trim() + "' not found in the system.";
        if (!(user instanceof Student))
            return "'" + studentId.trim() + "' is not a Student account.";

        Student s = (Student) user;
        if (!s.isBlacklisted())
            return "Student '" + s.getName() + "' is not currently blacklisted.";

        s.setBlacklisted(false);
        return "SUCCESS";
    }

    // ---- Repair Flag ----
    public String flagForRepair(String equipmentId) {
        if (equipmentId == null || equipmentId.trim().isEmpty())
            return "Equipment ID cannot be empty.";

        Equipment eq = findEquipment(equipmentId.trim());
        if (eq == null)
            return "Equipment ID '" + equipmentId.trim() + "' not found.";
        if (eq.getStatus().equals("REPAIR"))
            return "'" + eq.getName() + "' is already flagged for repair.";

        eq.setStatus("REPAIR");
        return "SUCCESS";
    }

    // ---- Records & Fines ----
    public List<BorrowRecord> getRecords() { return records; }

    public List<BorrowRecord> getActiveRecordsByStudent(String studentId) {
        List<BorrowRecord> result = new ArrayList<>();
        if (studentId == null || studentId.trim().isEmpty()) return result;
        for (BorrowRecord temp : records) {
            if (temp.getStudentId().equalsIgnoreCase(studentId.trim())
                    && temp.getRecordStatus().equals("ACTIVE")) {
                result.add(temp);
            }
        }
        return result;
    }

    public List<Fine> getFines() { return fines; }

    public List<Fine> getFinesByStudent(String studentId) {
        List<Fine> result = new ArrayList<>();
        if (studentId == null || studentId.trim().isEmpty()) return result;
        for (Fine temp : fines) {
            if (temp.getStudentId().equalsIgnoreCase(studentId.trim())) result.add(temp);
        }
        return result;
    }

    // ---- Helpers ----
    private BorrowRequest findRequest(String requestId) {
        for (BorrowRequest temp : requests) {
            if (temp.getRequestId().equalsIgnoreCase(requestId)) return temp;
        }
        return null;
    }

    private BorrowRecord findRecord(String recordId) {
        for (BorrowRecord temp : records) {
            if (temp.getRecordId().equalsIgnoreCase(recordId)) return temp;
        }
        return null;
    }

    public static Date daysFromNow(int days) {
        if (days < 1) days = 1;
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, days);
        return cal.getTime();
    }
}
