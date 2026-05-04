package gui;

import data.DataStore;
import model.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

/**
 * Student Dashboard - view equipment, submit borrow requests, view records & fines.
 */
public class StudentDashboard extends JFrame {

    private DataStore db = DataStore.getInstance();
    private Student   currentStudent;

    // Equipment tab
    private DefaultTableModel equipTableModel;

    // My Requests tab
    private DefaultTableModel reqTableModel;

    // My Records tab
    private DefaultTableModel recTableModel;

    // My Fines tab
    private DefaultTableModel fineTableModel;

    // Borrow form fields
    private JTextField  tfEquipId;
    private JSpinner    spinDays;
    private JTextField  tfPurpose;
    private JLabel      lblBorrowStatus;

    public StudentDashboard() {
        User user = db.getCurrentUser();
        if (!(user instanceof Student)) {
            JOptionPane.showMessageDialog(null, "Access denied.");
            dispose();
            return;
        }
        currentStudent = (Student) user;

        setTitle("Student Dashboard — " + currentStudent.getName());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 620);
        setLocationRelativeTo(null);
        buildUI();
    }

    private void buildUI() {
        // ---- Top bar ----
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(UITheme.BG_PANEL);
        topBar.setBorder(new EmptyBorder(12, 18, 12, 18));

        JLabel title = UITheme.makeLabel("🎒 Student Dashboard", UITheme.FONT_H2, UITheme.ACCENT);
        JLabel info  = UITheme.makeLabel(
                currentStudent.getName() + "  |  " + currentStudent.getUserId()
                        + "  |  " + currentStudent.getDepartment(),
                UITheme.FONT_SMALL, UITheme.TEXT_DIM);

        JButton btnLogout = UITheme.makeButton("Logout", UITheme.DANGER, Color.WHITE);
        btnLogout.addActionListener(e -> logout());

        JPanel infoBox = new JPanel();
        infoBox.setBackground(UITheme.BG_PANEL);
        infoBox.setLayout(new BoxLayout(infoBox, BoxLayout.Y_AXIS));
        infoBox.add(title);
        infoBox.add(info);

        String blackTag = currentStudent.isBlacklisted() ? "  ⛔ BLACKLISTED" : "";
        if (!blackTag.isEmpty()) {
            infoBox.add(UITheme.makeLabel(blackTag, UITheme.FONT_SMALL, UITheme.DANGER));
        }

        topBar.add(infoBox,      BorderLayout.WEST);
        topBar.add(btnLogout,    BorderLayout.EAST);

        // ---- Tabbed Pane ----
        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(UITheme.BG_DARK);
        tabs.setForeground(UITheme.TEXT_MAIN);
        tabs.setFont(UITheme.FONT_BODY);

        tabs.addTab("📦 Equipment",    buildEquipTab());
        tabs.addTab("📋 Borrow Now",   buildBorrowTab());
        tabs.addTab("🗒 My Requests",  buildRequestsTab());
        tabs.addTab("📁 My Records",   buildRecordsTab());
        tabs.addTab("💸 My Fines",     buildFinesTab());

        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(UITheme.BG_DARK);
        main.add(topBar, BorderLayout.NORTH);
        main.add(tabs,   BorderLayout.CENTER);
        setContentPane(main);
    }

    // ---- Equipment Tab ----
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

        JButton btnRefresh = UITheme.makeButton("↻ Refresh", UITheme.ACCENT, UITheme.BG_DARK);
        btnRefresh.addActionListener(e -> refreshEquipTable());

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.setBackground(UITheme.BG_DARK);
        top.add(UITheme.makeLabel("Available Sports Equipment", UITheme.FONT_H2, UITheme.TEXT_MAIN));
        top.add(Box.createHorizontalStrut(16));
        top.add(btnRefresh);

        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

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

    // ---- Borrow Tab ----
    private JPanel buildBorrowTab() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UITheme.BG_DARK);
        panel.setBorder(new EmptyBorder(20, 40, 20, 40));

        GridBagConstraints gc = new GridBagConstraints();
        gc.fill   = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(7, 6, 7, 6);
        gc.anchor = GridBagConstraints.WEST;

        int row = 0;

        gc.gridx = 0; gc.gridy = row; gc.gridwidth = 2;
        panel.add(UITheme.makeLabel("Submit Borrow Request", UITheme.FONT_H2, UITheme.ACCENT), gc);
        gc.gridwidth = 1;

        row++;
        gc.gridx = 0; gc.gridy = row;
        panel.add(UITheme.makeLabel("Equipment ID:", UITheme.FONT_BODY, UITheme.TEXT_DIM), gc);
        gc.gridx = 1;
        tfEquipId = UITheme.makeTextField(18);
        panel.add(tfEquipId, gc);

        row++;
        gc.gridx = 0; gc.gridy = row;
        panel.add(UITheme.makeLabel("Return in (days):", UITheme.FONT_BODY, UITheme.TEXT_DIM), gc);
        gc.gridx = 1;
        spinDays = new JSpinner(new SpinnerNumberModel(3, 1, 30, 1));
        spinDays.setFont(UITheme.FONT_BODY);
        spinDays.setBackground(UITheme.BG_CARD);
        JSpinner.NumberEditor daysEditor = new JSpinner.NumberEditor(spinDays, "#");
        spinDays.setEditor(daysEditor);
        ((AbstractDocument) daysEditor.getTextField().getDocument()).setDocumentFilter(new PositiveIntFilter(1, 30));
        panel.add(spinDays, gc);

        row++;
        gc.gridx = 0; gc.gridy = row;
        panel.add(UITheme.makeLabel("Purpose:", UITheme.FONT_BODY, UITheme.TEXT_DIM), gc);
        gc.gridx = 1;
        tfPurpose = UITheme.makeTextField(18);
        panel.add(tfPurpose, gc);

        row++;
        gc.gridx = 0; gc.gridy = row; gc.gridwidth = 2;
        JButton btnSubmit = UITheme.makeButton("  Submit Request  ", UITheme.ACCENT2, UITheme.BG_DARK);
        btnSubmit.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnSubmit.addActionListener(e -> submitBorrow());
        panel.add(btnSubmit, gc);

        row++;
        gc.gridx = 0; gc.gridy = row; gc.gridwidth = 2;
        lblBorrowStatus = UITheme.makeLabel("", UITheme.FONT_BODY, UITheme.ACCENT2);
        panel.add(lblBorrowStatus, gc);

        // small hint table
        row++;
        gc.gridy = row; gc.gridwidth = 2;
        panel.add(UITheme.makeLabel("(Copy Equipment ID from the Equipment tab)", UITheme.FONT_SMALL, UITheme.TEXT_DIM), gc);

        return panel;
    }

    private void submitBorrow() {
        String equipId  = tfEquipId.getText().trim().toUpperCase();
        String daysText = ((JSpinner.NumberEditor) spinDays.getEditor()).getTextField().getText().trim();
        String purpose  = tfPurpose.getText().trim();
        int    days;

        if (equipId.isEmpty()) {
            lblBorrowStatus.setForeground(UITheme.DANGER);
            lblBorrowStatus.setText("Equipment ID cannot be empty."); return;
        }

        // Validate days: must be a positive whole number 1-30
        try {
            days = Integer.parseInt(daysText);
        } catch (NumberFormatException ex) {
            lblBorrowStatus.setForeground(UITheme.DANGER);
            lblBorrowStatus.setText("Invalid days value! Please enter a whole number (e.g. 3). No decimals or special characters."); return;
        }
        if (days < 1 || days > 30) {
            lblBorrowStatus.setForeground(UITheme.DANGER);
            lblBorrowStatus.setText("Return days must be between 1 and 30. Negative values and zero are not allowed."); return;
        }

        if (purpose.isEmpty()) {
            lblBorrowStatus.setForeground(UITheme.DANGER);
            lblBorrowStatus.setText("Please enter a purpose."); return;
        }

        Date returnDate = DataStore.daysFromNow(days);
        String result = db.submitRequest(currentStudent.getUserId(), equipId, returnDate, purpose);

        if (result.startsWith("SUCCESS:")) {
            lblBorrowStatus.setForeground(UITheme.ACCENT2);
            lblBorrowStatus.setText("✓ Request submitted! ID: " + result.split(":")[1]
                    + "  (Awaiting advisor approval)");
            tfEquipId.setText(""); tfPurpose.setText("");
            refreshEquipTable();
        } else {
            lblBorrowStatus.setForeground(UITheme.DANGER);
            lblBorrowStatus.setText("✗ " + result);
        }
    }

    // ---- Requests Tab ----
    private JPanel buildRequestsTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(UITheme.BG_DARK);
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        String[] cols = {"Request ID", "Equipment", "Requested On", "Due Date", "Purpose", "Status"};
        reqTableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(reqTableModel);
        styleTable(table);

        JButton btnRefresh = UITheme.makeButton("↻ Refresh", UITheme.ACCENT, UITheme.BG_DARK);
        btnRefresh.addActionListener(e -> refreshReqTable());

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.setBackground(UITheme.BG_DARK);
        top.add(UITheme.makeLabel("My Borrow Requests", UITheme.FONT_H2, UITheme.TEXT_MAIN));
        top.add(btnRefresh);

        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        refreshReqTable();
        return panel;
    }

    private void refreshReqTable() {
        reqTableModel.setRowCount(0);
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
        for (BorrowRequest req : db.getRequestsByStudent(currentStudent.getUserId())) {
            reqTableModel.addRow(new Object[]{
                    req.getRequestId(), req.getEquipmentName(),
                    sdf.format(req.getRequestDate()),
                    sdf.format(req.getExpectedReturnDate()),
                    req.getPurpose(), req.getStatus()
            });
        }
    }

    // ---- Records Tab ----
    private JPanel buildRecordsTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(UITheme.BG_DARK);
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        String[] cols = {"Record ID", "Equipment", "Borrowed On", "Due Date", "Return Date", "Condition", "Status"};
        recTableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(recTableModel);
        styleTable(table);

        JButton btnRefresh = UITheme.makeButton("↻ Refresh", UITheme.ACCENT, UITheme.BG_DARK);
        btnRefresh.addActionListener(e -> refreshRecTable());

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.setBackground(UITheme.BG_DARK);
        top.add(UITheme.makeLabel("My Borrow Records", UITheme.FONT_H2, UITheme.TEXT_MAIN));
        top.add(btnRefresh);

        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        refreshRecTable();
        return panel;
    }

    private void refreshRecTable() {
        recTableModel.setRowCount(0);
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
        for (BorrowRecord rec : db.getRecords()) {
            if (!rec.getStudentId().equals(currentStudent.getUserId())) continue;
            recTableModel.addRow(new Object[]{
                    rec.getRecordId(), rec.getEquipmentName(),
                    sdf.format(rec.getBorrowDate()),
                    sdf.format(rec.getDueDate()),
                    rec.getReturnDate() == null ? "—" : sdf.format(rec.getReturnDate()),
                    rec.getConditionScore() + "/10",
                    rec.getRecordStatus()
            });
        }
    }

    // ---- Fines Tab ----
    private JPanel buildFinesTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(UITheme.BG_DARK);
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        String[] cols = {"Fine ID", "Record ID", "Overdue Days", "Amount (PKR)", "Status", "Issued On"};
        fineTableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(fineTableModel);
        styleTable(table);

        JButton btnRefresh = UITheme.makeButton("↻ Refresh", UITheme.ACCENT, UITheme.BG_DARK);
        btnRefresh.addActionListener(e -> refreshFineTable());

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.setBackground(UITheme.BG_DARK);
        top.add(UITheme.makeLabel("My Fines", UITheme.FONT_H2, UITheme.TEXT_MAIN));
        top.add(btnRefresh);

        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        refreshFineTable();
        return panel;
    }

    private void refreshFineTable() {
        fineTableModel.setRowCount(0);
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
        for (Fine fine : db.getFinesByStudent(currentStudent.getUserId())) {
            fineTableModel.addRow(new Object[]{
                    fine.getFineId(), fine.getRecordId(),
                    fine.getOverdueDays(), fine.getFineAmount(),
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
        table.setSelectionBackground(UITheme.ACCENT);
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
                "Please enter a positive whole number within the allowed range.\n" +
                "Negative values, decimals, and special characters are not allowed.",
                "Invalid Input", JOptionPane.WARNING_MESSAGE);
        }
    }
}
