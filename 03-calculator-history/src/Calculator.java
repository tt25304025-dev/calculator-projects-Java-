import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;
import java.util.List;

public class Calculator extends JFrame implements ActionListener {
    private final JTextField display;
    private String operator = "";
    private double num1 = 0;
    private boolean startNewNumber = true;

    public Calculator() {
        setTitle("Simple Calculator");
        setSize(320, 420);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        display = new JTextField();
        display.setEditable(false);
        display.setFont(new Font("Arial", Font.BOLD, 24));
        display.setHorizontalAlignment(SwingConstants.RIGHT);
        display.setPreferredSize(new Dimension(320, 60));
        add(display, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(4, 4, 5, 5));
        grid.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        String[] buttons = {
            "7", "8", "9", "/",
            "6", "5", "4", "*",
            "3", "2", "1", "-",
            "0", "C", "=", "+"
        };

        for (String text : buttons) {
            JButton btn = new JButton(text);
            btn.setFont(new Font("Arial", Font.BOLD, 20));
            btn.addActionListener(this);
            grid.add(btn);
        }

        add(grid, BorderLayout.CENTER);

        // Bottom bar with a single History button 
        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setBorder(BorderFactory.createEmptyBorder(6, 8, 8, 8));
        JButton historyBtn = new JButton("History");
        historyBtn.setFont(new Font("Arial", Font.BOLD, 16));
        historyBtn.addActionListener(e -> showHistory());
        bottomBar.add(historyBtn, BorderLayout.CENTER);
        add(bottomBar, BorderLayout.SOUTH);

        setResizable(false);
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();

        // Digit or decimal point (support decimal)
        if ((command.charAt(0) >= '0' && command.charAt(0) <= '9') || command.equals(".")) {
            if (startNewNumber) {
                if (command.equals(".")) display.setText("0.");
                else display.setText(command);
                startNewNumber = false;
            } else {
                if (command.equals(".") && display.getText().contains(".")) return;
                display.setText(display.getText() + command);
            }
            return;
        }

        // Clear
        if (command.equals("C")) {
            display.setText("");
            operator = "";
            num1 = 0;
            startNewNumber = true;
            return;
        }

        // Operator (+ - * /)
        if (command.equals("+") || command.equals("-") || command.equals("*") || command.equals("/")) {
            String txt = display.getText();
            if (txt == null || txt.isEmpty()) {
                num1 = 0;
            } else {
                try {
                    num1 = Double.parseDouble(txt);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Invalid number.");
                    return;
                }
            }
            operator = command;
            startNewNumber = true;
            return;
        }

        // Equals
        if (command.equals("=")) {
            String txt = display.getText();
            if (txt == null || txt.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Enter a number first.");
                return;
            }

            double num2;
            try {
                num2 = Double.parseDouble(txt);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid number.");
                return;
            }

            double result = 0;
            boolean valid = true;
            switch (operator) {
                case "+":
                    result = num1 + num2;
                    break;
                case "-":
                    result = num1 - num2;
                    break;
                case "*":
                    result = num1 * num2;
                    break;
                case "/":
                    if (num2 == 0) {
                        JOptionPane.showMessageDialog(this, "Error: Cannot divide by zero.");
                        valid = false;
                    } else {
                        result = num1 / num2;
                    }
                    break;
                default:
                    // no operator chosen — treat as single number
                    result = num2;
                    break;
            }

            if (valid) {
                String out = formatDouble(result);
                display.setText(out);
                logToFile(formatForLog(num1, operator, num2, result));
                startNewNumber = true;
                operator = "";
                num1 = 0;
            }
        }
    }

    private String formatDouble(double v) {
        if (v == (long) v) return String.format("%d", (long) v);
        return String.valueOf(v);
    }

    private String formatForLog(double a, String op, double b, double r) {
        if (op == null || op.isEmpty()) {
            return formatDouble(b) + " = " + formatDouble(r);
        } else {
            return formatDouble(a) + " " + op + " " + formatDouble(b) + " = " + formatDouble(r);
        }
    }

    private void logToFile(String text) {
        Path path = Paths.get("history.txt");
        try {
            Files.write(path, (text + System.lineSeparator()).getBytes(),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException ex) {
            System.err.println("Could not write history: " + ex.getMessage());
        }
    }

    private void showHistory() {
        Path path = Paths.get("history.txt");
        if (!Files.exists(path)) {
            JOptionPane.showMessageDialog(this, "No history found.");
            return;
        }

        try {
            List<String> lines = Files.readAllLines(path);
            JTextArea area = new JTextArea();
            area.setEditable(false);
            for (String line : lines) {
                area.append(line);
                area.append("\n");
            }
            area.setCaretPosition(0);
            JScrollPane scroll = new JScrollPane(area);
            scroll.setPreferredSize(new Dimension(380, 300));
            JOptionPane.showMessageDialog(this, scroll, "Calculation History", JOptionPane.PLAIN_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Could not read history: " + ex.getMessage());
        }
    }
}
