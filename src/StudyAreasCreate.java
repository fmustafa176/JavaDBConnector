import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;

public class StudyAreasCreate extends JFrame {
    private JTextField studyAreaIDField;
    private JComboBox<String> courseIDComboBox;
    private JTextField nameField;
    private JTextField typeField;
    private JTextField creditPointsField;
    private JTextField disciplineCodeField;
    private JTextField structureField;
    private JTextField rulesField;
    private JTextField statusField;
    private JTextField createdAtField;

    public StudyAreasCreate(Connection connection) {
        setTitle("Create Study Area");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new GridLayout(11, 2, 10, 10));
        setLocationRelativeTo(null);

        // Labels and fields
        add(new JLabel("Study Area ID:"));
        studyAreaIDField = new JTextField();
        add(studyAreaIDField);

        add(new JLabel("Course of Study Area:"));
        courseIDComboBox = new JComboBox<>();
        populateCourseIDComboBox(connection); // Populate drop-down
        add(courseIDComboBox);

        add(new JLabel("Name:"));
        nameField = new JTextField();
        add(nameField);

        add(new JLabel("Type:"));
        typeField = new JTextField();
        add(typeField);

        add(new JLabel("Credit Points:"));
        creditPointsField = new JTextField();
        add(creditPointsField);

        add(new JLabel("Discipline Code:"));
        disciplineCodeField = new JTextField();
        add(disciplineCodeField);

        add(new JLabel("Structure:"));
        structureField = new JTextField();
        add(structureField);

        add(new JLabel("Rules:"));
        rulesField = new JTextField();
        add(rulesField);

        add(new JLabel("Status:"));
        statusField = new JTextField();
        add(statusField);

        add(new JLabel("Created At (YYYY-MM-DD):"));
        createdAtField = new JTextField();
        add(createdAtField);

        // Submit button
        JButton submitButton = new JButton("Submit");
        add(submitButton);

        // Placeholder for layout symmetry
        add(new JLabel());

        // Submit button action (functionality can be implemented later)
        submitButton.addActionListener(e -> {
            String studyAreaID = studyAreaIDField.getText();
            String selectedCourseID = (String) courseIDComboBox.getSelectedItem();
            String name = nameField.getText();
            String type = typeField.getText();
            String creditPointsText = creditPointsField.getText();
            String disciplineCode = disciplineCodeField.getText();
            String structure = structureField.getText();
            String rules = rulesField.getText();
            String status = statusField.getText();
            String createdAt = createdAtField.getText();
        
            // Input validation
            if (studyAreaID.isEmpty() || name.isEmpty() || type.isEmpty() || creditPointsText.isEmpty() ||
                disciplineCode.isEmpty() || structure.isEmpty() || rules.isEmpty() || status.isEmpty() || createdAt.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in all fields.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
        
            // Validate credit points (must be an integer)
            int creditPoints;
            try {
                creditPoints = Integer.parseInt(creditPointsText);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Credit Points must be a valid number.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
        
            // Validate the date format (YYYY-MM-DD)
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            format.setLenient(false);  // Set to false to strictly check the date format
            java.sql.Date sqlDate = null;
            try {
                java.util.Date parsedDate = format.parse(createdAt);
                sqlDate = new java.sql.Date(parsedDate.getTime());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid date format. Please use YYYY-MM-DD.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
        
            String query = "INSERT INTO StudyAreas (studyAreaID, courseID, name, type, creditPoints, disciplineCode, structure, rules, status, createdAt) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                // Set values in the prepared statement
                stmt.setString(1, studyAreaID);
                stmt.setString(2, selectedCourseID.split(" - ")[0]); // Extract courseID
                stmt.setString(3, name);
                stmt.setString(4, type);
                stmt.setInt(5, creditPoints);
                stmt.setString(6, disciplineCode);
                stmt.setString(7, structure);
                stmt.setString(8, rules);
                stmt.setString(9, status);
                stmt.setDate(10, sqlDate);
        
                // Execute the query
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "Study Area created successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to create Study Area.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error inserting data: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        

        setVisible(true);
    }

    // Populate courseIDComboBox with values from the database
    private void populateCourseIDComboBox(Connection connection) {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT courseID, courseName FROM Course_")) {
            while (resultSet.next()) {
                String courseID = resultSet.getString("courseID");
                String courseName = resultSet.getString("courseName");
                courseIDComboBox.addItem(courseID + " - " + courseName);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error fetching courses: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
