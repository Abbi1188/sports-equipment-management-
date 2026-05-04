package gui;

import data.DataStore;
import model.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

/**
 * Sports Advisor Admin Panel.
 * - Approve / Reject pending borrow requests
 * - Process equipment returns (condition score + fine)
 * - Flag equipment for repair
 * - Manage student blacklist
 * - View all fines
 */
public class AdvisorPanel extends JFrame {

    private DataStore db = DataStore.getInstance();

    // Requests tab
    private DefaultTableModel reqTableModel;

    // Return tab
    private JTextField tfRecordId;
    private JSpinner   spinCondition;
    private JTextArea  taReturnResult;

    // Equipment tab
    private DefaultTableModel equipTableModel;
    private JTextField        tfRepairId;

    // Blacklist tab
    private DefaultTableModel studentTableModel;
    private JTextField        tfBlacklistId;

    // Fines tab
    private DefaultTableModel fineTableModel;

    public AdvisorPanel() {
        setTitle("Sports Advisor Panel — " + db.getCurrentUser().getName());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 680);
        setLocationRelativeTo(null);
        buildUI();
    }

    private void buildUI() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(UITheme.BG_PANEL);
        topBar.setBorder(new EmptyBorder(12, 18, 12, 18));

        JLabel title = UITheme.makeLabel("🛡 Sports Advisor Panel", UITheme.FONT_H2, UITheme.WARNING);
        JLabel info  = UITheme.makeLabel(
                db.getCurrentUser().getName() + "  |  " + db.getCurrentUser().getUserId(),
                UITheme.FONT_SMALL, UITheme.TEXT_DIM);

        JButton btnLogout = UITheme.makeButton("Logout", UITheme.DANGER, Color.WHITE);
        btnLogout.addActionListener(e -> logout());

        JPanel infoBox = new JPanel();
        infoBox.setBackground(UITheme.BG_PANEL);
        infoBox.setLayout(new BoxLayout(infoBox, BoxLayout.Y_AXIS));
        infoBox.add(title);
        infoBox.add(info);

        topBar.add(infoBox,   BorderLayout.WEST);
        topBar.add(btnLogout, BorderLayout.EAST);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UITheme.FONT_BODY);
        tabs.setBackground(UITheme.BG_DARK);
        tabs.setForeground(UITheme.TEXT_MAIN);

        tabs.addTab("📋 Requests",     buildRequestsTab());
        tabs.addTab("🔄 Process Return", buildReturnTab());
        tabs.addTab("🔧 Equipment",     buildEquipTab());
        tabs.addTab("⛔ Blacklist",     buildBlacklistTab());
        tabs.addTab("💸 All Fines",     buildFinesTab());

        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(UITheme.BG_DARK);
        main.add(topBar, BorderLayout.NORTH);
        main.add(tabs,   BorderLayout.CENTER);
        setContentPane(main);
    }

    // ---- Requests Tab ----
    private JPanel buildRequestsTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(UITheme.BG_DARK);
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        String[] cols = {"Req ID", "Student", "Student ID", "Equipment", "Equip ID", "Due Date", "Purpose", "Status"};
        reqTableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(reqTableModel);
        styleTable(table);

        // action buttons
        JTextField tfReqId     = UITheme.makeTextField(10);
        JButton    btnApprove  = UITheme.makeButton("✓ Approve", UITheme.ACCENT2, UITheme.BG_DARK);
        JButton    btnReject   = UITheme.makeButton("✗ Reject",  UITheme.DANGER,   Color.WHITE);
        JButton    btnRefresh  = UITheme.makeButton("↻ Refresh", UITheme.ACCENT,   UITheme.BG_DARK);
        JLabel     lblStatus   = UITheme.makeLabel("", UITheme.FONT_BODY, UITheme.ACCENT2);

        btnApprove.addActionListener(e -> {
            String id = tfReqId.getText().trim().toUpperCase();
            if (id.isEmpty()) { lblStatus.setForeground(UITheme.DANGER);
                lblStatus.setText("Enter a Request ID."); return; }
            String res = db.approveRequest(id);
            if (res.startsWith("SUCCESS:")) {
                lblStatus.setForeground(UITheme.ACCENT2);
                lblStatus.setText("✓ Approved. Record created: " + res.split(":")[1]);
            } else {
                lblStatus.setForeground(UITheme.DANGER);
                lblStatus.setText("✗ " + res);
            }
            refreshReqTable();
        });

        btnReject.addActionListener(e -> {
            String id = tfReqId.getText().trim().toUpperCase();
            if (id.isEmpty()) { lblStatus.setForeground(UITheme.DANGER);
                lblStatus.setText("Enter a Request ID."); return; }
            String res = db.rejectRequest(id);
            if (res.startsWith("SUCCESS")) {
                lblStatus.setForeground(UITheme.WARNING);
                lblStatus.setText("Request rejected.");
            } else {
                lblStatus.setForeground(UITheme.DANGER);
                lblStatus.setText("✗ " + res);
            }
            refreshReqTable();
        });

        btnRefresh.addActionListener(e -> refreshReqTable());

        // Auto-fill Request ID from table row selection
        table.getSelectionModel().addListSelectionListener(ev -> {
            if (!ev.getValueIsAdjusting() && table.getSelectedRow() >= 0) {
                tfReqId.setText(table.getValueAt(table.getSelectedRow(), 0).toString());
            }
        });

        JPanel actionBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actionBar.setBackground(UITheme.BG_DARK);
        actionBar.add(UITheme.makeLabel("Request ID:", UITheme.FONT_BODY, UITheme.TEXT_DIM));
        actionBar.add(tfReqId);
        actionBar.add(btnApprove);
        actionBar.add(btnReject);
        actionBar.add(btnRefresh);
        actionBar.add(lblStatus);

        panel.add(UITheme.makeLabel("Pending & All Requests", UITheme.FONT_H2, UITheme.TEXT_MAIN), BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(actionBar,              BorderLayout.SOUTH);

        refreshReqTable();
        return panel;
    }

    private void refreshReqTable() {
        reqTableModel.setRowCount(0);
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
        for (BorrowRequest req : db.getRequests()) {
            reqTableModel.addRow(new Object[]{
                    req.getRequestId(), req.getStudentName(), req.getStudentId(),
                    req.getEquipmentName(), req.getEquipmentId(),
                    sdf.format(req.getExpectedReturnDate()),
                    req.getPurpose(), req.getStatus()
            });
        }
    }

    // ---- Return Tab ----
    private JPanel buildReturnTab() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UITheme.BG_DARK);
        panel.setBorder(new EmptyBorder(20, 40, 20, 40));

        GridBagConstraints gc = new GridBagConstraints();
        gc.fill   = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(8, 6, 8, 6);
        gc.anchor = GridBagConstraints.WEST;

        int row = 0;

        gc.gridx = 0; gc.gridy = row; gc.gridwidth = 2;
        panel.add(UITheme.makeLabel("Process Equipment Return", UITheme.FONT_H2, UITheme.WARNING), gc);
        gc.gridwidth = 1;

        row++;
        gc.gridx = 0; gc.gridy = row;
        panel.add(UITheme.makeLabel("Record ID:", UITheme.FONT_BODY, UITheme.TEXT_DIM), gc);
        gc.gridx = 1;
        tfRecordId = UITheme.makeTextField(16);
        panel.add(tfRecordId, gc);

        row++;
        gc.gridx = 0; gc.gridy = row;
        panel.add(UITheme.makeLabel("Condition Score (1–10):", UITheme.FONT_BODY, UITheme.TEXT_DIM), gc);
        gc.gridx = 1;
        spinCondition = new JSpinner(new SpinnerNumberModel(8, 1, 10, 1));
        spinCondition.setFont(UITheme.FONT_BODY);
        JSpinner.NumberEditor condEditor = new JSpinner.NumberEditor(spinCondition, "#");
        spinCondition.setEditor(condEditor);
        ((AbstractDocument) condEditor.getTextField().getDocument()).setDocumentFilter(new PositiveIntFilter(1, 10));
        panel.add(spinCondition, gc);

        row++;
        gc.gridx = 0; gc.gridy = row;
        panel.add(UITheme.makeLabel("Note: Score ≤ 3 flags item for REPAIR", UITheme.FONT_SMALL, UITheme.WARNING), gc);

        row++;
        gc.gridx = 0; gc.gridy = row; gc.gridwidth = 2;
        JButton btnReturn = UITheme.makeButton("  Process Return  ", UITheme.WARNING, UITheme.BG_DARK);
        btnReturn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnReturn.addActionListener(e -> processReturn());
        panel.add(btnReturn, gc);

        row++;
        gc.gridy = row;
        taReturnResult = UITheme.makeTextArea(5, 40);
        taReturnResult.setEditable(false);
        JScrollPane sp = new JScrollPane(taReturnResult);
        sp.setPreferredSize(new Dimension(420, 120));
        panel.add(sp, gc);

        // active records quick-view
        row++;
        gc.gridy = row;
        panel.add(UITheme.makeLabel("Active Records (all students):", UITheme.FONT_SMALL, UITheme.TEXT_DIM), gc);
        row++;
        gc.gridy = row;
        JTextArea taRecords = UITheme.makeTextArea(6, 40);
        taRecords.setEditable(false);
        StringBuilder sb = new StringBuilder();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
        for (BorrowRecord rec : db.getRecords()) {
            if (rec.getRecordStatus().equals("ACTIVE")) {
                sb.append(rec.getRecordId()).append("  |  ")
                        .append(rec.getStudentName()).append("  |  ")
                        .append(rec.getEquipmentName()).append("  |  Due: ")
                        .append(sdf.format(rec.getDueDate())).append("\n");
            }
        }
        taRecords.setText(sb.toString().isEmpty() ? "No active records." : sb.toString());
        panel.add(new JScrollPane(taRecords), gc);

        return panel;
    }

    private void processReturn() {
        String recordId   = tfRecordId.getText().trim().toUpperCase();
        String scoreText  = ((JSpinner.NumberEditor) spinCondition.getEditor()).getTextField().getText().trim();
        int    score;

        if (recordId.isEmpty()) {
            taReturnResult.setForeground(UITheme.DANGER);
            taReturnResult.setText("Record ID cannot be empty. Please enter a valid Record ID."); return;
        }

        try {
            score = Integer.parseInt(scoreText);
        } catch (NumberFormatException ex) {
            taReturnResult.setForeground(UITheme.DANGER);
            taReturnResult.setText("Invalid condition score! Please enter a whole number between 1 and 10 (no decimals or special characters)."); return;
        }
        if (score < 1 || score > 10) {
            taReturnResult.setForeground(UITheme.DANGER);
            taReturnResult.setText("Condition score must be between 1 and 10. Negative values and zero are not allowed. Please enter a valid number."); return;
        }

        String result = db.processReturn(recordId, score);

        if (result.startsWith("SUCCESS")) {
            taReturnResult.setForeground(UITheme.ACCENT2);
            taReturnResult.setText("✓ Return processed successfully.\n" + result.split(":", 2)[1]);

        } else if (result.startsWith("FINE_BOTH:")) {
            // Both damage fine AND overdue fine issued
            String[] parts = result.split(":", 5);
            taReturnResult.setForeground(UITheme.DANGER);
            taReturnResult.setText(
                "⚠ TWO FINES ISSUED!\n" +
                "──────────────────────────────\n" +
                "Damage Fine ID  : " + parts[1] + "\n" +
                "Overdue Fine ID : " + parts[2] + "\n" +
                "Details         : " + parts[3] + "\n" +
                "Item flagged for REPAIR due to poor condition."
            );

        } else if (result.startsWith("FINE_DAMAGE:")) {
            // Only damage fine
            String[] parts = result.split(":", 3);
            taReturnResult.setForeground(UITheme.WARNING);
            taReturnResult.setText(
                "⚠ DAMAGE FINE ISSUED!\n" +
                "──────────────────────────────\n" +
                "Fine ID  : " + parts[1] + "\n" +
                "Details  : " + parts[2]
            );

        } else if (result.startsWith("FINE_OVERDUE:")) {
            // Only overdue fine
            String[] parts = result.split(":", 3);
            taReturnResult.setForeground(UITheme.WARNING);
            taReturnResult.setText(
                "⚠ OVERDUE FINE ISSUED!\n" +
                "──────────────────────────────\n" +
                "Fine ID  : " + parts[1] + "\n" +
                "Details  : " + parts[2]
            );

        } else {
            taReturnResult.setForeground(UITheme.DANGER);
            taReturnResult.setText("✗ " + result);
        }
        tfRecordId.setText("");
        refreshFineTable();
    }

    // ---- Equipment Management Tab ----
    private JPanel buildEquipTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(UITheme.BG_DARK);
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        String[] cols = {"ID", "Name", "Category", "Available", "Total", "Status"};
        equipTableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(equipTableModel);
        styleTable(table);

        tfRepairId = UITheme.makeTextField(10);
        JButton btnFlag    = UITheme.makeButton("🔧 Flag for Repair", UITheme.WARNING, UITheme.BG_DARK);
        JButton btnRefresh = UITheme.makeButton("↻ Refresh", UITheme.ACCENT,  UITheme.BG_DARK);
        JLabel  lblStatus  = UITheme.makeLabel("", UITheme.FONT_BODY, UITheme.ACCENT2);

        btnFlag.addActionListener(e -> {
            String id = tfRepairId.getText().trim().toUpperCase();
            if (id.isEmpty()) { lblStatus.setForeground(UITheme.DANGER);
                lblStatus.setText("Enter Equipment ID."); return; }
            String res = db.flagForRepair(id);
            if (res.startsWith("SUCCESS")) {
                lblStatus.setForeground(UITheme.WARNING);
                lblStatus.setText("Equipment flagged for repair.");
            } else {
                lblStatus.setForeground(UITheme.DANGER);
                lblStatus.setText("✗ " + res);
            }
            refreshEquipTable();
        });

        btnRefresh.addActionListener(e -> refreshEquipTable());

        table.getSelectionModel().addListSelectionListener(ev -> {
            if (!ev.getValueIsAdjusting() && table.getSelectedRow() >= 0)
                tfRepairId.setText(table.getValueAt(table.getSelectedRow(), 0).toString());
        });

        JPanel actionBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actionBar.setBackground(UITheme.BG_DARK);
        actionBar.add(UITheme.makeLabel("Equipment ID:", UITheme.FONT_BODY, UITheme.TEXT_DIM));
        actionBar.add(tfRepairId);
        actionBar.add(btnFlag);
        actionBar.add(btnRefresh);
        actionBar.add(lblStatus);

        panel.add(UITheme.makeLabel("Equipment Inventory", UITheme.FONT_H2, UITheme.TEXT_MAIN), BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(actionBar,              BorderLayout.SOUTH);

        refreshEquipTable();
        return panel;
    }

    private void refreshEquipTable() {
        equipTableModel.setRowCount(0);
        for (Equipment eq : db.getEquipmentList()) {
            equipTableModel.addRow(new Object[]{
                    eq.getEquipmentId(), eq.getName(), eq.getCategory(),
                    eq.getAvailableQuantity(), eq.getTotalQuantity(), eq.getStatus()
            });
        }
    }

    // ---- Blacklist Tab ----
    private JPanel buildBlacklistTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(UITheme.BG_DARK);
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        String[] cols = {"Student ID", "Name", "Department", "Blacklisted"};
        studentTableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(studentTableModel);
        styleTable(table);

        tfBlacklistId = UITheme.makeTextField(10);
        JButton btnBlacklist = UITheme.makeButton("⛔ Blacklist",   UITheme.DANGER,  Color.WHITE);
        JButton btnRemove    = UITheme.makeButton("✓ Remove Ban",   UITheme.ACCENT2, UITheme.BG_DARK);
        JButton btnRefresh   = UITheme.makeButton("↻ Refresh",      UITheme.ACCENT,  UITheme.BG_DARK);
        JLabel  lblStatus    = UITheme.makeLabel("", UITheme.FONT_BODY, UITheme.ACCENT2);

        btnBlacklist.addActionListener(e -> {
            String id = tfBlacklistId.getText().trim();
            if (id.isEmpty()) {
                lblStatus.setForeground(UITheme.DANGER);
                lblStatus.setText("\u2717 Please enter a Student ID.");
                return;
            }
            String res = db.blacklistStudent(id);
            if (res.startsWith("SUCCESS")) {
                lblStatus.setForeground(UITheme.DANGER);
                lblStatus.setText("Student blacklisted.");
            } else {
                lblStatus.setForeground(UITheme.DANGER);
                lblStatus.setText("✗ " + res);
            }
            refreshStudentTable();
        });

        btnRemove.addActionListener(e -> {
            String id = tfBlacklistId.getText().trim();
            if (id.isEmpty()) {
                lblStatus.setForeground(UITheme.DANGER);
                lblStatus.setText("\u2717 Please enter a Student ID.");
                return;
            }
            String res = db.removeBlacklist(id);
            if (res.startsWith("SUCCESS")) {
                lblStatus.setForeground(UITheme.ACCENT2);
                lblStatus.setText("Blacklist removed.");
            } else {
                lblStatus.setForeground(UITheme.DANGER);
                lblStatus.setText("✗ " + res);
            }
            refreshStudentTable();
        });

        btnRefresh.addActionListener(e -> refreshStudentTable());

        table.getSelectionModel().addListSelectionListener(ev -> {
            if (!ev.getValueIsAdjusting() && table.getSelectedRow() >= 0)
                tfBlacklistId.setText(table.getValueAt(table.getSelectedRow(), 0).toString());
        });

        JPanel actionBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actionBar.setBackground(UITheme.BG_DARK);
        actionBar.add(UITheme.makeLabel("Student ID:", UITheme.FONT_BODY, UITheme.TEXT_DIM));
        actionBar.add(tfBlacklistId);
        actionBar.add(btnBlacklist);
        actionBar.add(btnRemove);
        actionBar.add(btnRefresh);
        actionBar.add(lblStatus);

        panel.add(UITheme.makeLabel("Student Management & Blacklist", UITheme.FONT_H2, UITheme.TEXT_MAIN), BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(actionBar,              BorderLayout.SOUTH);

        refreshStudentTable();
        return panel;
    }

    private void refreshStudentTable() {
        studentTableModel.setRowCount(0);
        for (User temp : db.getUsers()) {
            if (temp instanceof Student) {
                Student s = (Student) temp;
                studentTableModel.addRow(new Object[]{
                        s.getUserId(), s.getName(), s.getDepartment(),
                        s.isBlacklisted() ? "YES ⛔" : "No"
                });
            }
        }
    }

    // ---- Fines Tab ----
    private JPanel buildFinesTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(UITheme.BG_DARK);
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        String[] cols = {"Fine ID", "Record ID", "Student", "Reason", "Overdue Days", "Amount (PKR)", "Status", "Issued On"};
        fineTableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(fineTableModel);
        styleTable(table);

        JButton btnRefresh = UITheme.makeButton("↻ Refresh", UITheme.ACCENT, UITheme.BG_DARK);
        btnRefresh.addActionListener(e -> refreshFineTable());

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.setBackground(UITheme.BG_DARK);
        top.add(UITheme.makeLabel("All Fines", UITheme.FONT_H2, UITheme.TEXT_MAIN));
        top.add(btnRefresh);

        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        refreshFineTable();
        return panel;
    }

    private void refreshFineTable() {
        fineTableModel.setRowCount(0);
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
        for (Fine fine : db.getFines()) {
            fineTableModel.addRow(new Object[]{
                    fine.getFineId(), fine.getRecordId(), fine.getStudentName(),
                    fine.getFineReason(), fine.getOverdueDays(), fine.getFineAmount(),
                    fine.getFineStatus(), sdf.format(fine.getIssuedDate())
            });
        }
    }

    // ---- Helpers ----
    private void styleTable(JTable table) {
        table.setBackground(UITheme.BG_CARD);
        table.setForeground(UITheme.TEXT_MAIN);
        table.setFont(UITheme.FONT_BODY);
        table.setRowHeight(26);
        table.setGridColor(UITheme.BORDER_COL);
        table.getTableHeader().setBackground(UITheme.BG_PANEL);
        table.getTableHeader().setForeground(UITheme.ACCENT);
        table.getTableHeader().setFont(UITheme.FONT_BODY);
        table.setSelectionBackground(UITheme.WARNING);
        table.setSelectionForeground(UITheme.BG_DARK);
    }

    private void logout() {
        db.logout();
        dispose();
        SwingUtilities.invokeLater(() -> new LoginScreen().setVisible(true));
    }

    /** Allows only positive whole numbers within [min, max] in a spinner text field. */
    private static class PositiveIntFilter extends DocumentFilter {
        private final int min, max;
        PositiveIntFilter(int min, int max) { this.min = min; this.max = max; }

        private boolean isValid(String text) {
            if (text == null || text.trim().isEmpty()) return true;
            if (!text.matches("\\d+")) return false;
            try {
                int v = Integer.parseInt(text.trim());
                return v >= min && v <= max;
            } catch (NumberFormatException e) { return false; }
        }

        @Override
        public void insertString(FilterBypass fb, int off, String str, AttributeSet a)
                throws BadLocationException {
            String result = fb.getDocument().getText(0, fb.getDocument().getLength()) + str;
            if (isValid(result)) super.insertString(fb, off, str, a);
            else showError();
        }

        @Override
        public void replace(FilterBypass fb, int off, int len, String str, AttributeSet a)
                throws BadLocationException {
            String current = fb.getDocument().getText(0, fb.getDocument().getLength());
            String result  = current.substring(0, off) + (str == null ? "" : str) + current.substring(off + len);
            if (isValid(result)) super.replace(fb, off, len, str, a);
            else showError();
        }

        private void showError() {
            JOptionPane.showMessageDialog(null,
                "Please enter a positive whole number between " + min + " and " + max + ".\n" +
                "Negative values, decimals, and special characters are not allowed.",
                "Invalid Input", JOptionPane.WARNING_MESSAGE);
        }
    }
}
