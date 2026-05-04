package gui;

import data.DataStore;
import model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Login Screen - role-based access.
 * Credentials for demo:
 *   Student  : S001 / ali123
 *   Advisor  : A001 / coach1
 */
public class LoginScreen extends JFrame {

    private JTextField     tfUserId;
    private JPasswordField pfPassword;
    private JLabel         lblStatus;
    private DataStore      db = DataStore.getInstance();

    public LoginScreen() {
        setTitle("Sports Equipment Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(480, 560);
        setLocationRelativeTo(null);
        setResizable(false);
        buildUI();
    }

    private void buildUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(UITheme.BG_DARK);

        // ---- Top banner ----
        JPanel bannerPanel = new JPanel(new GridBagLayout());
        bannerPanel.setBackground(UITheme.BG_PANEL);
        bannerPanel.setBorder(new EmptyBorder(30, 20, 20, 20));

        JLabel iconLbl = UITheme.makeLabel("🏆", new Font("Segoe UI Emoji", Font.PLAIN, 52), UITheme.ACCENT);
        JLabel titleLbl = UITheme.makeLabel("Sports Equipment", UITheme.FONT_TITLE, UITheme.TEXT_MAIN);
        JLabel subLbl   = UITheme.makeLabel("Management System", UITheme.FONT_H2,    UITheme.TEXT_DIM);

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0; gc.gridy = 0; gc.insets = new Insets(0,0,8,0);
        bannerPanel.add(iconLbl,  gc);
        gc.gridy = 1; gc.insets = new Insets(0,0,2,0);
        bannerPanel.add(titleLbl, gc);
        gc.gridy = 2;
        bannerPanel.add(subLbl,   gc);

        // ---- Login form ----
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(UITheme.BG_DARK);
        formPanel.setBorder(new EmptyBorder(30, 50, 30, 50));

        formPanel.add(makeFieldRow("User ID:", tfUserId = UITheme.makeTextField(20)));
        formPanel.add(Box.createVerticalStrut(14));
        formPanel.add(makeFieldRow("Password:", pfPassword = UITheme.makePasswordField(20)));
        formPanel.add(Box.createVerticalStrut(24));

        JButton btnLogin = UITheme.makeButton("  LOGIN  ", UITheme.ACCENT, UITheme.BG_DARK);
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLogin.setAlignmentX(CENTER_ALIGNMENT);
        btnLogin.addActionListener(this::doLogin);
        formPanel.add(btnLogin);
        formPanel.add(Box.createVerticalStrut(16));

        lblStatus = UITheme.makeLabel("", UITheme.FONT_SMALL, UITheme.DANGER);
        lblStatus.setAlignmentX(CENTER_ALIGNMENT);
        formPanel.add(lblStatus);

        // ---- Hint panel — full credentials table ----
        JPanel hintPanel = new JPanel();
        hintPanel.setBackground(UITheme.BG_PANEL);
        hintPanel.setBorder(new EmptyBorder(10, 20, 14, 20));
        hintPanel.setLayout(new BoxLayout(hintPanel, BoxLayout.Y_AXIS));

        hintPanel.add(UITheme.makeLabel("━━━  Registered Users  ━━━", UITheme.FONT_SMALL, UITheme.TEXT_DIM));
        hintPanel.add(Box.createVerticalStrut(4));

        // Header row
        JPanel header = new JPanel(new GridLayout(1, 4, 8, 0));
        header.setBackground(UITheme.BG_PANEL);
        header.add(UITheme.makeLabel("Role",     UITheme.FONT_SMALL, UITheme.ACCENT));
        header.add(UITheme.makeLabel("Name",     UITheme.FONT_SMALL, UITheme.ACCENT));
        header.add(UITheme.makeLabel("User ID",  UITheme.FONT_SMALL, UITheme.ACCENT));
        header.add(UITheme.makeLabel("Password", UITheme.FONT_SMALL, UITheme.ACCENT));
        hintPanel.add(header);
        hintPanel.add(Box.createVerticalStrut(2));

        // Data rows helper
        String[][] creds = {
            {"Student",  "Qazi Abdur Rahman", "24p-0570", "std01"},
            {"Student",  "Abdul Rehman",      "24p-0518", "std02"},
            {"Student",  "Yahya Bin Zia",     "24p-0668", "std03"},
            {"Faculty",  "Dr. Imran Shah", "F001",     "imran00"},
            {"Advisor",  "Coach Tariq",    "T-0011",   "coach1"},
        };
        java.awt.Color[] rowColors = {
            UITheme.ACCENT2, UITheme.ACCENT2, UITheme.ACCENT2,
            new java.awt.Color(100, 200, 255),
            UITheme.WARNING
        };
        for (int i = 0; i < creds.length; i++) {
            JPanel row = new JPanel(new GridLayout(1, 4, 8, 0));
            row.setBackground(UITheme.BG_PANEL);
            for (String cell : creds[i]) {
                row.add(UITheme.makeLabel(cell, UITheme.FONT_SMALL, rowColors[i]));
            }
            hintPanel.add(row);
        }

        mainPanel.add(bannerPanel, BorderLayout.NORTH);
        mainPanel.add(formPanel,   BorderLayout.CENTER);
        mainPanel.add(hintPanel,   BorderLayout.SOUTH);

        setContentPane(mainPanel);

        getRootPane().setDefaultButton(btnLogin);
    }

    private JPanel makeFieldRow(String label, JComponent field) {
        JPanel row = new JPanel(new BorderLayout(0, 5));
        row.setBackground(UITheme.BG_DARK);
        JLabel lbl = UITheme.makeLabel(label, UITheme.FONT_BODY, UITheme.TEXT_DIM);
        row.add(lbl,   BorderLayout.NORTH);
        row.add(field, BorderLayout.CENTER);
        return row;
    }

    private void doLogin(ActionEvent e) {
        String userId   = tfUserId.getText().trim();
        String password = new String(pfPassword.getPassword()).trim();

        if (userId.isEmpty()) {
            lblStatus.setText("Please enter your User ID.");
            tfUserId.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            lblStatus.setText("Please enter your Password.");
            pfPassword.requestFocus();
            return;
        }

        if (!db.userExists(userId)) {
            lblStatus.setText("Invalid User ID. Or Incorrect Password");
            tfUserId.setText("");
            pfPassword.setText("");
            tfUserId.requestFocus();
            return;
        }

        User user = db.login(userId, password);
        if (user == null) {
            lblStatus.setText("Invalid User ID. Or Incorrect Password");
            pfPassword.setText("");
            pfPassword.requestFocus();
            return;
        }

        lblStatus.setText("");
        dispose();

        SwingUtilities.invokeLater(() -> {
            if (user.getRole().equals("ADVISOR") || user.getRole().equals("FACULTY")) {
                new AdvisorPanel().setVisible(true);
            } else {
                new StudentDashboard().setVisible(true);
            }
        });
    }
}
