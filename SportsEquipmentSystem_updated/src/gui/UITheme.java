package gui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * Central place for colours / fonts so every panel looks consistent.
 */
public class UITheme {

    // ---- Colour Palette ----
    public static final Color BG_DARK    = new Color(15,  23,  42);   // deep navy
    public static final Color BG_PANEL   = new Color(30,  41,  59);   // slate
    public static final Color BG_CARD    = new Color(51,  65,  85);   // card surface
    public static final Color ACCENT     = new Color(56, 189, 248);   // sky blue
    public static final Color ACCENT2    = new Color(99, 235, 162);   // mint green
    public static final Color DANGER     = new Color(248,  113, 113); // red
    public static final Color WARNING    = new Color(251, 191,  36);  // amber
    public static final Color TEXT_MAIN  = new Color(241, 245, 249);
    public static final Color TEXT_DIM   = new Color(148, 163, 184);
    public static final Color BORDER_COL = new Color(71,  85, 105);

    // ---- Fonts ----
    public static final Font FONT_TITLE  = new Font("Segoe UI", Font.BOLD,  24);
    public static final Font FONT_H2     = new Font("Segoe UI", Font.BOLD,  16);
    public static final Font FONT_BODY   = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL  = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_MONO   = new Font("Consolas",  Font.PLAIN, 12);

    // ---- Borders ----
    public static Border cardBorder() {
        return BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COL, 1, true),
                new EmptyBorder(10, 12, 10, 12));
    }

    public static Border sectionBorder(String title) {
        return BorderFactory.createTitledBorder(
                new LineBorder(ACCENT, 1, true), title,
                javax.swing.border.TitledBorder.LEFT,
                javax.swing.border.TitledBorder.TOP,
                FONT_BODY, ACCENT);
    }

    // ---- Styled Components ----
    public static JLabel makeLabel(String text, Font font, Color color) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(font);
        lbl.setForeground(color);
        return lbl;
    }

    public static JButton makeButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BODY);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static JTextField makeTextField(int cols) {
        JTextField tf = new JTextField(cols);
        tf.setFont(FONT_BODY);
        tf.setBackground(BG_CARD);
        tf.setForeground(TEXT_MAIN);
        tf.setCaretColor(TEXT_MAIN);
        tf.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COL),
                new EmptyBorder(5, 8, 5, 8)));
        return tf;
    }

    public static JPasswordField makePasswordField(int cols) {
        JPasswordField pf = new JPasswordField(cols);
        pf.setFont(FONT_BODY);
        pf.setBackground(BG_CARD);
        pf.setForeground(TEXT_MAIN);
        pf.setCaretColor(TEXT_MAIN);
        pf.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COL),
                new EmptyBorder(5, 8, 5, 8)));
        return pf;
    }

    public static JComboBox<String> makeCombo(String[] items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setFont(FONT_BODY);
        cb.setBackground(BG_CARD);
        cb.setForeground(TEXT_MAIN);
        return cb;
    }

    public static JTextArea makeTextArea(int rows, int cols) {
        JTextArea ta = new JTextArea(rows, cols);
        ta.setFont(FONT_MONO);
        ta.setBackground(BG_CARD);
        ta.setForeground(ACCENT2);
        ta.setCaretColor(TEXT_MAIN);
        ta.setBorder(new EmptyBorder(6, 8, 6, 8));
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        return ta;
    }

    // Apply dark defaults globally
    public static void applyGlobalDefaults() {
        UIManager.put("Panel.background",          BG_DARK);
        UIManager.put("OptionPane.background",     BG_PANEL);
        UIManager.put("OptionPane.messageForeground", TEXT_MAIN);
        UIManager.put("Button.background",         BG_CARD);
        UIManager.put("Button.foreground",         TEXT_MAIN);
        UIManager.put("Label.foreground",          TEXT_MAIN);
        UIManager.put("TextField.background",      BG_CARD);
        UIManager.put("TextField.foreground",      TEXT_MAIN);
        UIManager.put("TextArea.background",       BG_CARD);
        UIManager.put("TextArea.foreground",       ACCENT2);
        UIManager.put("ComboBox.background",       BG_CARD);
        UIManager.put("ComboBox.foreground",       TEXT_MAIN);
        UIManager.put("Table.background",          BG_CARD);
        UIManager.put("Table.foreground",          TEXT_MAIN);
        UIManager.put("Table.gridColor",           BORDER_COL);
        UIManager.put("TableHeader.background",    BG_PANEL);
        UIManager.put("TableHeader.foreground",    ACCENT);
        UIManager.put("ScrollPane.background",     BG_DARK);
        UIManager.put("Viewport.background",       BG_DARK);
    }
}
