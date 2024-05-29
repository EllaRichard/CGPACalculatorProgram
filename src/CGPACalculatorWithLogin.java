import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class CGPACalculatorWithLogin extends JFrame {
    private JTextField nameField;
    private JTextField schoolField;
    private JTextField departmentField;
    private JTextField levelField;
    private JButton loginButton;
    private JButton createAccountButton;
    private Connection connection;

    public CGPACalculatorWithLogin() {
        // Set up the frame
        setTitle("Login Page");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Create UI components for login and account creation
        nameField = new JTextField(15);
        schoolField = new JTextField(15);
        departmentField = new JTextField(15);
        levelField = new JTextField(15);
        loginButton = new JButton("Login");
        createAccountButton = new JButton("Create Account");

        // Add action listeners
        loginButton.addActionListener(new LoginButtonListener());
        createAccountButton.addActionListener(new CreateAccountButtonListener());

        // Set up colorful layout for the login page
        JPanel inputPanel = new JPanel(new GridLayout(5, 2, 5, 5));
        inputPanel.setBackground(Color.LIGHT_GRAY);
        inputPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        inputPanel.add(new JLabel("Name:"));
        inputPanel.add(nameField);
        inputPanel.add(new JLabel("School:"));
        inputPanel.add(schoolField);
        inputPanel.add(new JLabel("Department:"));
        inputPanel.add(departmentField);
        inputPanel.add(new JLabel("Level:"));
        inputPanel.add(levelField);
        inputPanel.add(loginButton);
        inputPanel.add(createAccountButton);

        getContentPane().add(inputPanel, BorderLayout.CENTER);

        try {
            // Connect to the database
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/login", "root", "a1.b2,c3?d4!");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to connect to the database.", "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private class LoginButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String name = nameField.getText();
            String school = schoolField.getText();
            String department = departmentField.getText();
            String level = levelField.getText();

            // Check if all fields are filled
            if (name.isEmpty() || school.isEmpty() || department.isEmpty() || level.isEmpty()) {
                JOptionPane.showMessageDialog(CGPACalculatorWithLogin.this, "Please fill in all fields.", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                try {
                    // Check if user exists in the database
                    PreparedStatement statement = connection.prepareStatement("SELECT * FROM users WHERE name = ?");
                    statement.setString(1, name);
                    ResultSet resultSet = statement.executeQuery();
                    if (resultSet.next()) {
                        // User exists, proceed to CGPA calculation
                        CGPACalculator cgpaCalculator = new CGPACalculator(name);
                        cgpaCalculator.setVisible(true);
                        CGPACalculatorWithLogin.this.dispose();
                    } else {
                        JOptionPane.showMessageDialog(CGPACalculatorWithLogin.this,
                                "User does not exist. Please create an account.",
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                    resultSet.close();
                    statement.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(CGPACalculatorWithLogin.this, "Database error.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private class CreateAccountButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String name = nameField.getText();
            String school = schoolField.getText();
            String department = departmentField.getText();
            String level = levelField.getText();

            // Check if all fields are filled
            if (name.isEmpty() || school.isEmpty() || department.isEmpty() || level.isEmpty()) {
                JOptionPane.showMessageDialog(CGPACalculatorWithLogin.this, "Please fill in all fields.", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                try {
                    // Register the user
                    registerUser(name, school, department, level);
                    JOptionPane.showMessageDialog(CGPACalculatorWithLogin.this,
                            "Account creation successful. Please login.",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(CGPACalculatorWithLogin.this, "Database error.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void registerUser(String name, String school, String department, String level) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("INSERT INTO users (name, school, department, level) VALUES (?, ?, ?, ?)");
        statement.setString(1, name);
        statement.setString(2, school);
        statement.setString(3, department);
        statement.setString(4, level);
        statement.executeUpdate();
        statement.close();
    }

    private class CGPACalculator extends JFrame {
        private JTextField yearsField;
        private JTextArea resultArea;
        private JButton startButton;
        private int totalUnitLoad = 0;
        private int overallUnitLoad = 0;
        private double overallTotal = 0;
        private double total = 0;
        private String userName;

        public CGPACalculator(String userName) {
            this.userName = userName;

            // Set up the frame
            setTitle("CGPA Calculator");
            setSize(600, 400);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLocationRelativeTo(null);

            // Create UI components for CGPA calculation
            yearsField = new JTextField(5);
            resultArea = new JTextArea(15, 40);
            resultArea.setEditable(false);
            startButton = new JButton("Start Calculation");

            // Add action listener to the start button
            startButton.addActionListener(new StartButtonListener());

            // Set up layout for CGPA calculation
            JPanel inputPanel = new JPanel();
            inputPanel.setBackground(Color.CYAN);
            inputPanel.add(new JLabel("Enter number of years:"));
            inputPanel.add(yearsField);
            inputPanel.add(startButton);

            JPanel resultPanel = new JPanel();
            resultPanel.setBackground(Color.PINK);
            resultPanel.add(new JScrollPane(resultArea));

            getContentPane().add(inputPanel, BorderLayout.NORTH);
            getContentPane().add(resultPanel, BorderLayout.CENTER);
        }

        private class StartButtonListener implements ActionListener {
            public void actionPerformed(ActionEvent e) {
                try {
                    // Get the number of years from the input field
                    int numberOfYears = Integer.parseInt(yearsField.getText());
                    calculateCGPA(numberOfYears);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(CGPACalculator.this, "Invalid input. Please enter a numeric value.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }

        private void calculateCGPA(int numberOfYears) {
            resultArea.setText("Calculating CGPA for " + numberOfYears + " years...\n");

            for (int year = 1; year <= numberOfYears; year++) {
                resultArea.append("Year " + year + ":\n");
                for (int semester = 1; semester <= 2; semester++) {
                    resultArea.append("  Semester " + semester + ":\n");
                    int numberOfCourses = 0;
                    boolean validCourses = false;

                    // Loop to ensure valid input for number of courses
                    while (!validCourses) {
                        try {
                            numberOfCourses = Integer.parseInt(JOptionPane.showInputDialog("Enter number of courses for Year " + year + ", Semester " + semester + ":"));
                            validCourses = true;
                        } catch (NumberFormatException ex) {
                            JOptionPane.showMessageDialog(CGPACalculator.this, "Invalid input. Please enter a numeric value for number of courses.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }

                    // Loop through each course to get the course title, unit load, and grade
                    for (int courseCount = 0; courseCount < numberOfCourses; courseCount++) {
                        boolean validInput = false;
                        while (!validInput) {
                            try {
                                String input = JOptionPane.showInputDialog("Enter course title, unit load, and grade for course " + (courseCount + 1) + " (separated by space):");
                                String[] inputs = input.split(" ");
                                if (inputs.length != 3) {
                                    throw new IllegalArgumentException("Please enter course title, unit load, and grade separated by spaces.");
                                }
                                String courseTitle = inputs[0];
                                int unitLoad = Integer.parseInt(inputs[1]);
                                String grade = inputs[2];

                                // Calculate total for the course
                                total += grading(unitLoad, grade);
                                totalUnitLoad += unitLoad;

                                // Append course information to the result area
                                resultArea.append("    " + courseTitle + " (Unit Load: " + unitLoad + ", Grade: " + grade + ")\n");
                                validInput = true;
                            } catch (Exception ex) {
                                JOptionPane.showMessageDialog(CGPACalculator.this, "Invalid input: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    }

                    // Update overall totals
                    overallUnitLoad += totalUnitLoad;
                    overallTotal += total;

                    // Calculate GPA for the semester
                    double gpa = total / totalUnitLoad;
                    resultArea.append("  GPA for Year " + year + ", Semester " + semester + ": " + String.format("%.2f", gpa) + "\n");

                    // Reset totals for the next semester
                    total = 0;
                    totalUnitLoad = 0;
                }
            }

            // Calculate final CGPA
            double cgpa = overallTotal / overallUnitLoad;
            resultArea.append("\nYour CGPA at the end of " + numberOfYears + " years is " + String.format("%.2f", cgpa) + "\n");
            String output;

            // Determine the class of degree based on CGPA
            if (cgpa < 2.5)
                output = "Pass";
            else if (cgpa < 3.5)
                output = "Second Class Lower or two-two";
            else if (cgpa < 4.5)
                output = "Second Class Upper or two-one";
            else
                output = "First Class";

            resultArea.append("Congrats, You graduated with " + output + "\n");

            // Store CGPA data in the users table
            try {
                PreparedStatement statement = connection.prepareStatement("UPDATE users SET cgpa = ? WHERE name = ?");
                statement.setDouble(1, cgpa);
                statement.setString(2, userName);
                statement.executeUpdate();
                statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(CGPACalculator.this, "Failed to store CGPA data.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        public double grading(int unitLoad, String grade) {
            // Determine the grade score based on the grade character
            int gradeScores = switch (grade) {
                case "A", "a" -> 5;
                case "B", "b" -> 4;
                case "C", "c" -> 3;
                case "D", "d" -> 2;
                case "E", "e" -> 1;
                case "F", "f" -> 0;
                default -> throw new IllegalArgumentException("Please input a valid grade character.");
            };
            return gradeScores * unitLoad;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CGPACalculatorWithLogin().setVisible(true));
    }
}
