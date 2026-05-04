# Sports Equipment Management System
### Java Swing GUI Application — SDA Assignment

---

## 📁 Project Structure

```
SportsEquipmentSystem/
└── src/
    ├── Main.java                    ← Entry point
    ├── model/
    │   ├── User.java
    │   ├── Student.java
    │   ├── Faculty.java
    │   ├── SportsAdvisor.java
    │   ├── Equipment.java
    │   ├── BorrowRequest.java
    │   ├── BorrowRecord.java
    │   └── Fine.java
    ├── data/
    │   └── DataStore.java           ← In-memory database (Singleton)
    └── gui/
        ├── UITheme.java             ← Colours, fonts, factory methods
        ├── LoginScreen.java         ← Role-based login
        ├── StudentDashboard.java    ← Student use cases
        └── AdvisorPanel.java        ← Admin / Advisor use cases
```

---

## ⚙️ Compile & Run

### Prerequisites
- **Java JDK 8 or higher** — no external libraries needed (pure Swing).

### Step 1 — Compile from project root

```bash
cd SportsEquipmentSystem

# Windows (PowerShell / CMD)
javac -d out -sourcepath src src/Main.java src/model/*.java src/data/*.java src/gui/*.java

# macOS / Linux
javac -d out -sourcepath src $(find src -name "*.java")
```

### Step 2 — Run

```bash
# Windows
java -cp out Main

# macOS / Linux
java -cp out Main
```

---

## 🔑 Demo Login Credentials

| Role           | User ID | Password |
|----------------|---------|----------|
| Student        | S001    | ali123   |
| Student        | S002    | sara456  |
| Student        | S003    | bil789   |
| Faculty        | F001    | imran00  |
| Sports Advisor | A001    | coach1   |

---

## 🎮 How to Test the Full Workflow

1. **Login as Student (S001 / ali123)**
   - Go to **Equipment** tab → note an Equipment ID (e.g. `E001`)
   - Go to **Borrow Now** → enter ID, choose days, add purpose → Submit
   - Note the **Request ID** shown (e.g. `REQ001`)

2. **Logout → Login as Advisor (A001 / coach1)**
   - Go to **Requests** tab → click the row or type Request ID → click **✓ Approve**
   - Note the **Record ID** shown (e.g. `REC001`)

3. **Process Return (same Advisor session)**
   - Go to **Process Return** tab
   - Enter the Record ID, set condition score (1–10) → click **Process Return**
   - A fine is auto-calculated if overdue (100 PKR / day)
   - Score ≤ 3 flags the equipment for **REPAIR**

4. **Blacklist a Student**
   - Go to **Blacklist** tab → select student → click **⛔ Blacklist**
   - That student can no longer submit requests

5. **Flag Equipment for Repair manually**
   - Go to **Equipment** tab → select row or enter ID → click **🔧 Flag for Repair**

---

## 🏗 Design Patterns Used

| Pattern     | Where Applied             | Purpose                                    |
|-------------|---------------------------|--------------------------------------------|
| Singleton   | `DataStore`               | One shared in-memory DB across all panels  |
| Template Method (via Inheritance) | `User → Student/Faculty/SportsAdvisor` | Role-specific behaviour |
| Factory (informal) | `UITheme` static methods | Consistent styled component creation |

---

## 📌 Class-Diagram Mapping

| UML Class       | Java File            | Notes                               |
|-----------------|----------------------|-------------------------------------|
| `User`          | `model/User.java`    | Abstract base class                 |
| `Student`       | `model/Student.java` | Extends User, adds blacklist flag   |
| `Faculty`       | `model/Faculty.java` | Extends User                        |
| `SportsAdvisor` | `model/SportsAdvisor.java` | Extends User, can approve/reject |
| `Equipment`     | `model/Equipment.java` | Tracks quantity and status        |
| `BorrowRequest` | `model/BorrowRequest.java` | PENDING → APPROVED/REJECTED     |
| `BorrowRecord`  | `model/BorrowRecord.java`  | Created on approval, ACTIVE→RETURNED |
| `Fine`          | `model/Fine.java`    | Auto-generated on overdue return    |
