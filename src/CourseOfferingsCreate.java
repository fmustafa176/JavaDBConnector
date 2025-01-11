import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;

public class CourseOfferingsCreate extends JFrame {
    private JTextField courseOfferingIDField;
    private JComboBox<String> courseComboBox;
    private JTextField locationField;
    private JTextField startDateField;
    private JTextField endDateField;
    private JComboBox<String> timeslotComboBox;
    private JComboBox<String> lecturerComboBox;
    private JTextField statusField;

    public CourseOfferingsCreate(Connection connection) {
        setTitle("Create Course Offering");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new GridBagLayout());
        setLocationRelativeTo(null);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Labels and fields
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(new JLabel("Course Offering ID:"), gbc);
        courseOfferingIDField = new JTextField();
        gbc.gridx = 1;
        add(courseOfferingIDField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        add(new JLabel("Course:"), gbc);
        courseComboBox = new JComboBox<>();
        populateCourseComboBox(connection); // Populate drop-down with course IDs
        gbc.gridx = 1;
        add(courseComboBox, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        add(new JLabel("Location:"), gbc);
        locationField = new JTextField();
        gbc.gridx = 1;
        add(locationField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        add(new JLabel("Start Date (YYYY-MM-DD):"), gbc);
        startDateField = new JTextField();
        gbc.gridx = 1;
        add(startDateField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        add(new JLabel("End Date (YYYY-MM-DD):"), gbc);
        endDateField = new JTextField();
        gbc.gridx = 1;
        add(endDateField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        add(new JLabel("Timeslot:"), gbc);
        timeslotComboBox = new JComboBox<>();
        populateTimeslotComboBox(connection); // Populate drop-down with timeslots
        gbc.gridx = 1;
        add(timeslotComboBox, gbc);

        gbc.gridx = 0;
        gbc.gridy = 6;
        add(new JLabel("Lecturer:"), gbc);
        lecturerComboBox = new JComboBox<>();
        populateLecturerComboBox(connection); // Populate drop-down with lecturers
        gbc.gridx = 1;
        add(lecturerComboBox, gbc);

        gbc.gridx = 0;
        gbc.gridy = 7;
        add(new JLabel("Status:"), gbc);
        statusField = new JTextField();
        gbc.gridx = 1;
        add(statusField, gbc);

        // Submit button
        JButton submitButton = new JButton("Submit");
        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.gridwidth = 2;
        add(submitButton, gbc);

        // Submit button action (functionality can be implemented later)
        submitButton.addActionListener(e -> {
            String courseOfferingID = courseOfferingIDField.getText();
            String selectedCourseID = (String) courseComboBox.getSelectedItem();
            String location = locationField.getText();
            String startDate = startDateField.getText();
            String endDate = endDateField.getText();
            String selectedTimeslot = (String) timeslotComboBox.getSelectedItem();
            String selectedLecturerID = (String) lecturerComboBox.getSelectedItem();
            String status = statusField.getText();

            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            String query = "INSERT INTO CourseOfferings (courseOfferingID, courseID, location, startDate, endDate, timeslot, lecturerID, status) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                // Set values in the prepared statement
                stmt.setString(1, courseOfferingID);
                stmt.setString(2, selectedCourseID.split(" - ")[0]); // Extract courseID
                stmt.setString(3, location);
                stmt.setDate(4, new java.sql.Date(format.parse(startDate).getTime()));
                stmt.setDate(5, new java.sql.Date(format.parse(endDate).getTime()));
                stmt.setString(6, selectedTimeslot.split(" - ")[0]); // Extract timeslotID
                stmt.setString(7, selectedLecturerID.split(" - ")[0]); // Extract lecturerID
                stmt.setString(8, status);

                // Execute the query
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "Course Offering created successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to create Course Offering.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException | java.text.ParseException ex) {
                JOptionPane.showMessageDialog(this, "Error inserting data: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        setVisible(true);
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

    // Populate timeslotComboBox with values from the database
    private void populateTimeslotComboBox(Connection connection) {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM timeslot")) {
            while (resultSet.next()) {
                String timeSlotID = resultSet.getString("timeSlotID");
                String startTime = resultSet.getString("startTime");
                String endTime = resultSet.getString("endTime");
                String dayOfWeek = resultSet.getString("dayOfWeek");

                // Format timeslot display as: timeSlotID - startTime - endTime - dayOfWeek
                String timeslotDisplay = timeSlotID + " - " + startTime + " - " + endTime + " - " + dayOfWeek;
                timeslotComboBox.addItem(timeslotDisplay);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error fetching timeslots: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Populate lecturerComboBox with values from the database
    private void populateLecturerComboBox(Connection connection) {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT teacherID, firstname, lastname FROM teacher")) {
            while (resultSet.next()) {
                String teacherID = resultSet.getString("teacherID");
                String firstName = resultSet.getString("firstname");
                String lastName = resultSet.getString("lastname");
                lecturerComboBox.addItem(teacherID + " - " + firstName + " " + lastName);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error fetching lecturers: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
