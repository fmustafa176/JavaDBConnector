import javax.swing.*;
import java.awt.*;
// import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;

public class CourseCreate extends JFrame {
    private JTextField courseIDField, courseNameField, creditHoursField, semesterField, departmentField,
                       versionField, rulesField, createdAtField, semestersOfferedField, descriptionField;
    private JComboBox<String> programIDComboBox, prerequisitesComboBox;
    private JPanel assessmentsPanel;
    private ArrayList<JCheckBox> assessmentCheckBoxes;
    private Connection connection;

    public CourseCreate(Connection connection) {
        this.connection = connection;

        setTitle("Course Form");
        setLayout(new BorderLayout(10, 10));
        

        JPanel formPanel = new JPanel(new GridLayout(13, 2, 10, 10));
        
        // Text fields for attributes
        formPanel.add(new JLabel("Course ID:"));
        courseIDField = new JTextField();
        formPanel.add(courseIDField);

        formPanel.add(new JLabel("Course Name:"));
        courseNameField = new JTextField();
        formPanel.add(courseNameField);

        formPanel.add(new JLabel("Credit Hours:"));
        creditHoursField = new JTextField();
        formPanel.add(creditHoursField);

        formPanel.add(new JLabel("Semester:"));
        semesterField = new JTextField();
        formPanel.add(semesterField);

        formPanel.add(new JLabel("Department:"));
        departmentField = new JTextField();
        formPanel.add(departmentField);

        formPanel.add(new JLabel("Version:"));
        versionField = new JTextField();
        formPanel.add(versionField);

        formPanel.add(new JLabel("Rules:"));
        rulesField = new JTextField();
        formPanel.add(rulesField);

        formPanel.add(new JLabel("Created At (YYYY-MM-DD):"));
        createdAtField = new JTextField();
        formPanel.add(createdAtField);

        formPanel.add(new JLabel("Semesters Offered:"));
        semestersOfferedField = new JTextField();
        formPanel.add(semestersOfferedField);

        formPanel.add(new JLabel("Description:"));
        descriptionField = new JTextField();
        formPanel.add(descriptionField);

        // Dropdown for programID
        formPanel.add(new JLabel("Program ID:"));
        programIDComboBox = new JComboBox<>();
        populateComboBox(programIDComboBox, "SELECT programid, programname FROM program");
        formPanel.add(programIDComboBox);

        // Dropdown for prerequisites
        formPanel.add(new JLabel("Prerequisites:"));
        prerequisitesComboBox = new JComboBox<>();
        prerequisitesComboBox.addItem("None");
        populateComboBox(prerequisitesComboBox, "SELECT courseid, coursename FROM course_");
        formPanel.add(prerequisitesComboBox);

        add(formPanel, BorderLayout.CENTER);

        JPanel assessmentsContainer = new JPanel(new BorderLayout());
        assessmentsContainer.add(new JLabel("Assessments:"), BorderLayout.NORTH);
        assessmentsPanel = new JPanel();
        assessmentsPanel.setLayout(new BoxLayout(assessmentsPanel, BoxLayout.Y_AXIS));
        assessmentCheckBoxes = new ArrayList<>();
        populateAssessmentCheckBoxes();
        JScrollPane scrollPane = new JScrollPane(assessmentsPanel);
        assessmentsContainer.add(scrollPane, BorderLayout.CENTER);
        add(assessmentsContainer, BorderLayout.EAST);


        // Submit button
        JButton submitButton = new JButton("Submit");
        submitButton.addActionListener(e -> handleSubmit());
        add(submitButton, BorderLayout.SOUTH);

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null); // Center the window on the screen
        setVisible(true);
    }

    private void populateComboBox(JComboBox<String> comboBox, String query) {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                comboBox.addItem(rs.getString(1) + " - " + rs.getString(2));
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error fetching data: " + ex.getMessage());
        }
    }

    private void populateAssessmentCheckBoxes() {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT assessmentid, assessmentdetails, weightage FROM assessment")) {
            while (rs.next()) {
                String assessmentInfo = rs.getString(1) + " - " + rs.getString(2) + " (" + rs.getString(3) + "%)";
                JCheckBox checkBox = new JCheckBox(assessmentInfo);
                assessmentCheckBoxes.add(checkBox);
                assessmentsPanel.add(checkBox);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error fetching assessments: " + ex.getMessage());
        }
    }

    private void handleSubmit() {
        try {
            connection.setAutoCommit(false);
    
            // Insert course data
            String courseSql = "INSERT INTO course_ (courseID, courseName, creditHours, semester, department, version, rules, createdAt, semestersOffered, programID, description, prerequisites) " +
                               "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = connection.prepareStatement(courseSql)) {
                pstmt.setString(1, courseIDField.getText());

                pstmt.setString(2, courseNameField.getText());
                pstmt.setInt(3, Integer.parseInt(creditHoursField.getText()));
                pstmt.setString(4, semesterField.getText());
                pstmt.setString(5, departmentField.getText());
                pstmt.setString(6, versionField.getText());
                pstmt.setString(7, rulesField.getText());
                pstmt.setDate(8, java.sql.Date.valueOf(createdAtField.getText()));
                pstmt.setInt(9, Integer.parseInt(semestersOfferedField.getText()));
                String programID = programIDComboBox.getSelectedItem().toString().split(" - ")[0];
                pstmt.setString(10, programID);
                pstmt.setString(11, descriptionField.getText());
                String prerequisites = prerequisitesComboBox.getSelectedItem().toString();
                pstmt.setString(12, prerequisites.equals("None") ? null : prerequisites.split(" - ")[0]);
    
                pstmt.executeUpdate();
            }
    
            // Insert assessments
            String assessmentSql = "INSERT INTO courseassessment (courseAssessmentID, courseID, assessmentID) VALUES (?, ?, ?)";
            try (PreparedStatement pstmt = connection.prepareStatement(assessmentSql)) {
                for (JCheckBox checkBox : assessmentCheckBoxes) {
                    if (checkBox.isSelected()) {
                        String assessmentID = checkBox.getText().split(" - ")[0];
                        String courseAssessmentID = courseIDField.getText() + "_" + assessmentID;
                        pstmt.setString(1, courseAssessmentID);
                        pstmt.setString(2, courseIDField.getText());
                        pstmt.setString(3, assessmentID);
                        pstmt.addBatch();
                    }
                }
                pstmt.executeBatch();
            }
    
            connection.commit();
            JOptionPane.showMessageDialog(this, "Course and assessments added successfully!");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error inserting data: " + ex.getMessage());
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                JOptionPane.showMessageDialog(this, "Rollback failed: " + rollbackEx.getMessage());
            }
        }
    }
}
