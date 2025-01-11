import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
// import java.util.ArrayList;

public class UnitCreate extends JFrame {
    private JTextField unitIDField;
    private JTextField nameField;
    private JComboBox<String> classComboBox;
    private JTextField creditPointsField;
    private JTextField contactHoursField;
    private JComboBox<String> courseComboBox;
    private JTextField startDateField;
    private JTextField endDateField;
    private JCheckBox isAssessedCheckBox;
    private JTextField createdAtField;
    private JPanel assessmentPanel; // Panel to hold assessment checkboxes

    public UnitCreate(Connection connection) {
        setTitle("Create Unit");
        setSize(600, 600); // Increased size to accommodate assessments
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        setLocationRelativeTo(null);

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Labels and fields
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Unit ID:"), gbc);
        unitIDField = new JTextField();
        gbc.gridx = 1;
        formPanel.add(unitIDField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Name:"), gbc);
        nameField = new JTextField();
        gbc.gridx = 1;
        formPanel.add(nameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("Class:"), gbc);
        classComboBox = new JComboBox<>();
        populateClassComboBox(connection); // Populate drop-down with class IDs
        gbc.gridx = 1;
        formPanel.add(classComboBox, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(new JLabel("Credit Points:"), gbc);
        creditPointsField = new JTextField();
        gbc.gridx = 1;
        formPanel.add(creditPointsField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        formPanel.add(new JLabel("Contact Hours:"), gbc);
        contactHoursField = new JTextField();
        gbc.gridx = 1;
        formPanel.add(contactHoursField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        formPanel.add(new JLabel("Course:"), gbc);
        courseComboBox = new JComboBox<>();
        populateCourseComboBox(connection); // Populate drop-down with course IDs
        gbc.gridx = 1;
        formPanel.add(courseComboBox, gbc);

        gbc.gridx = 0;
        gbc.gridy = 6;
        formPanel.add(new JLabel("Start Date (YYYY-MM-DD):"), gbc);
        startDateField = new JTextField();
        gbc.gridx = 1;
        formPanel.add(startDateField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 7;
        formPanel.add(new JLabel("End Date (YYYY-MM-DD):"), gbc);
        endDateField = new JTextField();
        gbc.gridx = 1;
        formPanel.add(endDateField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 8;
        formPanel.add(new JLabel("Is Assessed:"), gbc);
        isAssessedCheckBox = new JCheckBox();
        gbc.gridx = 1;
        formPanel.add(isAssessedCheckBox, gbc);

        gbc.gridx = 0;
        gbc.gridy = 9;
        formPanel.add(new JLabel("Created At (YYYY-MM-DD):"), gbc);
        createdAtField = new JTextField();
        gbc.gridx = 1;
        formPanel.add(createdAtField, gbc);

        // Panel for assessments
        gbc.gridx = 0;
        gbc.gridy = 10;
        formPanel.add(new JLabel("Assessments:"), gbc);
        assessmentPanel = new JPanel();
        assessmentPanel.setLayout(new BoxLayout(assessmentPanel, BoxLayout.Y_AXIS)); // Display checkboxes vertically
        populateAssessmentCheckboxes(connection); // Populate checkboxes with assessments
        gbc.gridx = 1;
        formPanel.add(assessmentPanel, gbc);

        add(formPanel, BorderLayout.CENTER);

        // Submit button
        JButton submitButton = new JButton("Submit");
        submitButton.addActionListener(e -> {
            String unitID = unitIDField.getText();
            String name = nameField.getText();
            String selectedClassID = (String) classComboBox.getSelectedItem();
            String creditPoints = creditPointsField.getText();
            String contactHours = contactHoursField.getText();
            String selectedCourseID = (String) courseComboBox.getSelectedItem();
            String startDate = startDateField.getText();
            String endDate = endDateField.getText();
            boolean isAssessed = isAssessedCheckBox.isSelected();
            String createdAt = createdAtField.getText();

            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            String query = "INSERT INTO Units (unitID, name, classID, creditPoints, contactHours, courseID, startDate, endDate, isAssessed, createdAt) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                // Set values in the prepared statement
                stmt.setString(1, unitID);
                stmt.setString(2, name);
                stmt.setString(3, selectedClassID.split(" - ")[0]); // Extract classID
                stmt.setInt(4, Integer.parseInt(creditPoints));
                stmt.setInt(5, Integer.parseInt(contactHours));
                stmt.setString(6, selectedCourseID.split(" - ")[0]); // Extract courseID
                stmt.setDate(7, new java.sql.Date(format.parse(startDate).getTime()));
                stmt.setDate(8, new java.sql.Date(format.parse(endDate).getTime()));
                stmt.setBoolean(9, isAssessed);
                stmt.setDate(10, new java.sql.Date(format.parse(createdAt).getTime()));

                // Execute the query
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "Unit created successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    // Insert selected assessments into UnitAssessment table
                    insertSelectedAssessments(connection, unitID);
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to create Unit.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException | java.text.ParseException ex) {
                JOptionPane.showMessageDialog(this, "Error inserting data: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(submitButton);
        add(buttonPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    // Populate classComboBox with values from the database
    private void populateClassComboBox(Connection connection) {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT classID, className FROM class")) {
            while (resultSet.next()) {
                String classID = resultSet.getString("classID");
                String className = resultSet.getString("className");
                classComboBox.addItem(classID + " - " + className);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error fetching classes: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Populate courseComboBox with values from the database
    private void populateCourseComboBox(Connection connection) {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT courseID, courseName FROM course_")) {
            while (resultSet.next()) {
                String courseID = resultSet.getString("courseID");
                String courseName = resultSet.getString("courseName");
                courseComboBox.addItem(courseID + " - " + courseName);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error fetching courses: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Populate assessmentPanel with checkboxes for assessments
    private void populateAssessmentCheckboxes(Connection connection) {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT assessmentID, assessmentDetails FROM assessment")) {
            while (resultSet.next()) {
                String assessmentID = resultSet.getString("assessmentID");
                String assessmentDetails = resultSet.getString("assessmentDetails");
                JCheckBox assessmentCheckBox = new JCheckBox(assessmentDetails);
                assessmentCheckBox.setActionCommand(assessmentID);
                assessmentPanel.add(assessmentCheckBox);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error fetching assessments: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Insert selected assessments into UnitAssessment table
    private void insertSelectedAssessments(Connection connection, String unitID) {
        for (Component comp : assessmentPanel.getComponents()) {
            if (comp instanceof JCheckBox && ((JCheckBox) comp).isSelected()) {
                String assessmentID = ((JCheckBox) comp).getActionCommand();
                String query = "INSERT INTO UnitAssessment (unitAssessmentID, unitID, assessmentID) VALUES (?, ?, ?)";
                try (PreparedStatement stmt = connection.prepareStatement(query)) {
                    String unitAssessmentID = unitID + "_" + assessmentID;
                    stmt.setString(1, unitAssessmentID);
                    stmt.setString(2, unitID);
                    stmt.setString(3, assessmentID);
                    stmt.executeUpdate();
                } catch (SQLException e) {
                    JOptionPane.showMessageDialog(this, "Error inserting assessments: " + e.getMessage(),
                            "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
}
