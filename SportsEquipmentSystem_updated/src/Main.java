import gui.LoginScreen;
import gui.UITheme;

import javax.swing.*;

/**
 * ==========================================
 *  Sports Equipment Management System
 *  Entry Point — launches the Login Screen
 * ==========================================
 *
 * Demo Accounts:
 *   Student  → S001 / ali123
 *   Student  → S002 / sara456
 *   Advisor  → A001 / coach1
 */
public class Main {
    public static void main(String[] args) {
        // Apply dark theme defaults before any window opens
        UITheme.applyGlobalDefaults();

        SwingUtilities.invokeLater(() -> {
            LoginScreen login = new LoginScreen();
            login.setVisible(true);
        });
    }
}
